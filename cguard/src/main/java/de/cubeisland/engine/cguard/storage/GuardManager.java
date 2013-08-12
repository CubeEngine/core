/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.cguard.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import de.cubeisland.engine.cguard.Cguard;
import de.cubeisland.engine.cguard.commands.CommandListener;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.world.WorldManager;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.cguard.storage.TableGuardLocations.TABLE_GUARD_LOCATION;
import static de.cubeisland.engine.cguard.storage.TableGuards.TABLE_GUARD;

public class GuardManager implements Listener
{
    protected final DSLContext dsl;
    protected final Cguard module;

    protected WorldManager wm;
    protected UserManager um;

    public final CommandListener commandListener;

    private final Map<Location, Guard> loadedGuards = new HashMap<>();
    private final Map<Chunk, Set<Guard>> loadedGuardsInChunk = new HashMap<>();
    private final Map<UUID, Guard> loadedEntityGuards = new HashMap<>();

    public GuardManager(Cguard module)
    {
        this.commandListener = new CommandListener(module, this);
        this.module = module;
        this.wm = module.getCore().getWorldManager();
        this.um = module.getCore().getUserManager();
        this.module.getCore().getEventManager().registerListener(module, this.commandListener);
        this.module.getCore().getEventManager().registerListener(module, this);
        this.dsl = module.getCore().getDB().getDSL();
        for (World world : module.getCore().getWorldManager().getWorlds())
        {
            for (Chunk chunk : world.getLoadedChunks())
            {
                this.loadChunk(chunk);
            }
        }
    }

    /**
     * Returns the Guard at given location
     *
     * @param location
     * @return the guard or null if there is no guard OR the chunk is not loaded
     */
    public Guard getGuardAtLocation(Location location)
    {
        return this.loadedGuards.get(location);
    }

    public Guard getGuardForEntityUID(UUID uniqueId)
    {
        Guard guard = this.loadedEntityGuards.get(uniqueId);
        if (guard == null)
        {
            GuardModel model = this.dsl.selectFrom(TABLE_GUARD).where(TABLE_GUARD.ENTITY_UID_LEAST.eq(uniqueId.getLeastSignificantBits()),
                                                   TABLE_GUARD.ENTITY_UID_MOST.eq(uniqueId.getMostSignificantBits())).fetchOne();
            if (model != null)
            {
                guard = new Guard(this, model);
                this.loadedEntityGuards.put(uniqueId, guard);
            }
        }
        return guard;
        // TODO handle unloading & garbage collection e.g. when entities got removed by WE
    }

    /**
     * Gets the guard at given location
     *
     * @param location
     * @return the guard or null if there is no guard
     */
    public Guard loadGuardAtLocation(Location location)
    {
        Guard guard = this.loadedGuards.get(location);
        if (guard == null)
        {
            UInteger world_id = UInteger.valueOf(this.wm.getWorldId(location.getWorld()));
            GuardModel model = this.dsl.selectFrom(TABLE_GUARD).where(TABLE_GUARD.ID.eq(
                this.dsl.select(TABLE_GUARD_LOCATION.GUARD_ID).from(TABLE_GUARD_LOCATION).where(
                    TABLE_GUARD_LOCATION.WORLD_ID.eq(world_id),
                    TABLE_GUARD_LOCATION.X.eq(location.getBlockX()),
                    TABLE_GUARD_LOCATION.Y.eq(location.getBlockY()),
                    TABLE_GUARD_LOCATION.Z.eq(location.getBlockZ())))).fetchOne();
            guard = new Guard(this, model, this.dsl.selectFrom(TABLE_GUARD_LOCATION).where(TABLE_GUARD_LOCATION.GUARD_ID.eq(model.getId())).fetch());
        }
        return guard;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event)
    {
        this.loadChunk(event.getChunk());
    }

    private void loadChunk(Chunk chunk)
    {
        UInteger world_id = UInteger.valueOf(this.wm.getWorldId(chunk.getWorld()));
        Result<GuardModel> models = this.dsl.selectFrom(TABLE_GUARD).where(TABLE_GUARD.ID.in(
            this.dsl.select(TABLE_GUARD_LOCATION.GUARD_ID).from(TABLE_GUARD_LOCATION).
                where(TABLE_GUARD_LOCATION.WORLD_ID.eq(world_id),
                      TABLE_GUARD_LOCATION.CHUNKX.eq(chunk.getX()),
                      TABLE_GUARD_LOCATION.CHUNKZ.eq(chunk.getZ())))).fetch();
        Map<UInteger, Result<GuardLocationModel>> locations = this.dsl.selectFrom(TABLE_GUARD_LOCATION)
                                                                      .where(TABLE_GUARD_LOCATION.GUARD_ID.in(models.getValues(TABLE_GUARD.ID)))
                                                                      .fetch().intoGroups(TABLE_GUARD_LOCATION.GUARD_ID);
        for (GuardModel model : models)
        {
            Result<GuardLocationModel> guardLoc = locations.get(model.getId());
            this.addLoadedLocationGuard(new Guard(this, model, guardLoc));
        }
    }

    private void addLoadedLocationGuard(Guard guard)
    {
        if (!guard.isSingleBlockGuard())
        {
            this.addLoadedLocationGuard(guard.getLocation2(), guard);
        }
        this.addLoadedLocationGuard(guard.getLocation(), guard);
    }

    private void addLoadedLocationGuard(Location loc, Guard guard)
    {
        if (loc.getChunk().isLoaded())
        {
            Set<Guard> guards = this.loadedGuardsInChunk.get(loc.getChunk());
            if (guards == null)
            {
                guards = new HashSet<>();
                this.loadedGuardsInChunk.put(loc.getChunk(), guards);
            }
            guards.add(guard);
        }
        this.loadedGuards.put(loc, guard);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event)
    {
        Set<Guard> remove = this.loadedGuardsInChunk.remove(event.getChunk());
        if (remove == null) return; // nothing to remove
        for (Guard guard : remove) // remove from chunks
        {
            if (guard.isSingleBlockGuard())
            {
                this.loadedGuards.remove(guard.getLocation()); // remove loc
            }
            else
            {
                if (guard.getLocation().getChunk() == guard.getLocation().getChunk()) // same chunk remove both loc
                {
                    this.loadedGuards.remove(guard.getLocation());
                    this.loadedGuards.remove(guard.getLocation2());
                }
                else // different chunks
                {
                    Chunk c1 = guard.getLocation().getChunk();
                    Chunk c2 = guard.getLocation2().getChunk();
                    Chunk chunk = event.getChunk();
                    if ((!c1.isLoaded() && c2 == chunk)
                      ||(!c2.isLoaded() && c1 == chunk))
                    {// Both chunks will be unloaded remove both loc
                        this.loadedGuards.remove(guard.getLocation());
                        this.loadedGuards.remove(guard.getLocation2());
                    }
                    // else the other chunk is still loaded -> do not remove!
                }
            }
        }
    }

    protected void delete(Guard guard)
    {
        guard.model.delete();
        if (guard.isBlockGuard())
        {
            Location loc = guard.getLocation();
            this.loadedGuards.remove(loc);
            Set<Guard> guards = this.loadedGuardsInChunk.get(loc.getChunk());
            if (guards != null)
            {
                guards.remove(guard);
            }
            if (!guard.isSingleBlockGuard())
            {
                loc = guard.getLocation2();
                this.loadedGuards.remove(loc);
                guards = this.loadedGuardsInChunk.get(loc.getChunk());
                if (guards != null)
                {
                    guards.remove(guard);
                }
            }
        }
        else
        {
            this.loadedEntityGuards.remove(guard.model.getUUID());
        }
    }

    public Guard createGuard(Material material, Location location, User user, byte guardType)
    {
        GuardModel model = this.dsl.newRecord(TABLE_GUARD).newGuard(user, guardType, this.getProtecedType(material));
        GuardLocationModel loc1 = null;
        GuardLocationModel loc2 = null;
        model.insert();
        switch (material) // Double Block Protections
        {
            case CHEST:
            if (location.getBlock().getState() instanceof Chest)
            {
                Chest chest = (Chest)location.getBlock().getState();
                if (chest.getInventory().getHolder() instanceof DoubleChest)
                {
                    DoubleChest dc = (DoubleChest)chest.getInventory().getHolder();
                    loc1 = this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, ((BlockState)dc.getLeftSide()).getLocation());
                    loc2 = this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, ((BlockState)dc.getRightSide()).getLocation());
                }
            }
            break;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                // TODO
        }
        if (loc1 == null)
        {
            loc1 = this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, location);
        }
        loc1.insert();
        if (loc2 != null) loc2.insert();
        Guard guard = new Guard(this, model, loc1, loc2);
        this.addLoadedLocationGuard(guard);
        return guard;
    }

    private byte getProtecedType(Material material)
    {
        switch (material)
        {
            case CHEST:
            case TRAPPED_CHEST:
            case DISPENSER:
            case DROPPER:
            case FURNACE:
            case BURNING_FURNACE:
            case BREWING_STAND:
            // TODO missing ?
                return TYPE_CONTAINER;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case FENCE_GATE:
            case TRAP_DOOR:
                return TYPE_DOOR;
            default:
                if (material.getId() < 256) return TYPE_BLOCK;
        }
        return 0;
    }

    private byte getProtectedType(Entity entity)
    {
        return 0; // TODO
    }

    public static final byte TYPE_CONTAINER = 1;
    public static final byte TYPE_DOOR = 2;
    public static final byte TYPE_BLOCK = 3;
    public static final byte TYPE_ENTITY_CONTAINER = 4;
    public static final byte TYPE_ENTITY_LIVING = 5;
    public static final byte TYPE_ENTITY = 6;
    // TODO more ?

    public Guard createGuard(Entity entity, User user, byte guardType)
    {
return null; // TODO
    }
}
