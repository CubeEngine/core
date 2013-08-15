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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Door;

import de.cubeisland.engine.cguard.Cguard;
import de.cubeisland.engine.cguard.commands.CommandListener;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.world.WorldManager;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.cguard.storage.TableAccessList.TABLE_ACCESS_LIST;
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

    public final MessageDigest messageDigest;

    public GuardManager(Cguard module)
    {
        try
        {
            messageDigest = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-1 hash algorithm not available!");
        }
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
        Guard guard = this.loadedGuards.get(location);
        if (guard != null) guard.model.setLastAccess(new Timestamp(System.currentTimeMillis()));
        return guard;
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
        if (guard != null) guard.model.setLastAccess(new Timestamp(System.currentTimeMillis()));
        return guard;
        // TODO handle unloading & garbage collection e.g. when entities got removed by WE
    }

    public void extendGuard(Guard guard, Location location)
    {
        guard.locations.add(location);
        GuardLocationModel model = this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(guard.model, location);
        model.insert();
        this.loadedGuards.put(location.clone(), guard);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event)
    {
        this.loadChunk(event.getChunk());
    }

    private void loadChunk(Chunk chunk)
    {
        UInteger world_id = UInteger.valueOf(this.wm.getWorldId(chunk.getWorld()));
        Result<GuardModel> models = this.dsl.selectFrom(TABLE_GUARD).where(
                    TABLE_GUARD.ID.in(this.dsl
                     .select(TABLE_GUARD_LOCATION.GUARD_ID)
                     .from(TABLE_GUARD_LOCATION)
                     .where(TABLE_GUARD_LOCATION.WORLD_ID.eq(world_id), TABLE_GUARD_LOCATION.CHUNKX.eq(chunk
                                                                                                           .getX()), TABLE_GUARD_LOCATION
                                .CHUNKZ.eq(chunk.getZ())))).fetch();
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
            guard.model.update(); // updates if changed (last_access timestamp)
        }
    }

    public void removeGuard(Guard guard)
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

    public void invalidateKeyBooks(Guard guard)
    {
        this.createPassword(guard.model, null);
        guard.model.update();
    }

    /**
     * Sets a new password for given guard-model
     *
     * @param model
     * @param pass
     */
    private void createPassword(GuardModel model, String pass)
    {
        if (pass != null)
        {
            synchronized (this.messageDigest)
            {
                this.messageDigest.reset();
                model.setPassword(this.messageDigest.digest(pass.getBytes()));
            }
        }
        else
        {
            model.setPassword(StringUtils.randomString(new SecureRandom(), 4, "0123456789abcdefklmnor").getBytes());
        }
    }

    public Guard createGuard(Material material, Location location, User user, byte guardType, String password)
    {
        GuardModel model = this.dsl.newRecord(TABLE_GUARD).newGuard(user, guardType, this.getProtectedType(material));
        this.createPassword(model, password);
        model.insert();
        List<Location> locations = new ArrayList<>();
        switch (material) // Double Block Protections
        {
            case CHEST:
            if (location.getBlock().getState() instanceof Chest)
            {
                Chest chest = (Chest)location.getBlock().getState();
                if (chest.getInventory().getHolder() instanceof DoubleChest)
                {
                    DoubleChest dc = (DoubleChest)chest.getInventory().getHolder();
                    locations.add(((BlockState)dc.getLeftSide()).getLocation());
                    locations.add(((BlockState)dc.getRightSide()).getLocation());
                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(0)).insert();
                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(1)).insert();
                }
            }
            break;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                // TODO doubleDoor detection
                locations.add(location);
                if (location.getBlock().getState().getData() instanceof Door)
                {
                    if (((Door)location.getBlock().getState().getData()).isTopHalf())
                    {
                        locations.add(location.clone().add(0, -1, 0));
                        locations.add(locations.get(1).clone().add(0, -1, 0));
                    }
                    else
                    {
                        locations.add(location.clone().add(0, 1, 0));
                        locations.add(location.clone().add(0, -1, 0));
                    }
                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(0)).insert();
                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(1)).insert();
                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(2)).insert();

                }
        }
        if (locations.isEmpty())
        {
            locations.add(location);
            this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, location).insert();
        }
        Guard guard = new Guard(this, model, locations);
        this.addLoadedLocationGuard(guard);
        return guard;
    }

    private byte getProtectedType(Material material)
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
        throw new IllegalStateException("Material of block is an item!?");
    }

    private byte getProtectedType(Entity entity)
    {
        switch (entity.getType())
        {
        case MINECART_CHEST:
        case MINECART_HOPPER:
            return TYPE_ENTITY_CONTAINER;
        case HORSE:
            if (entity instanceof InventoryHolder) return TYPE_ENTITY_CONTAINER;
        default:
            if (entity.getType().isAlive()) return TYPE_ENTITY_LIVING;
            if (entity instanceof Vehicle) return TYPE_ENTITY_VEHICLE;
            return TYPE_ENTITY;
        }
    }

    public static final byte TYPE_CONTAINER = 1;
    public static final byte TYPE_DOOR = 2;
    public static final byte TYPE_BLOCK = 3;
    public static final byte TYPE_ENTITY_CONTAINER = 4;
    public static final byte TYPE_ENTITY_LIVING = 5;
    public static final byte TYPE_ENTITY_VEHICLE = 6;
    public static final byte TYPE_ENTITY = 7;

    // TODO more ?

    public Guard createGuard(Entity entity, User user, byte guardType, String password)
    {
        GuardModel model = this.dsl.newRecord(TABLE_GUARD).newGuard(user, guardType, this.getProtectedType(entity), entity.getUniqueId());
        this.createPassword(model, password);
        model.insert();
        Guard guard = new Guard(this, model);
        this.loadedEntityGuards.put(entity.getUniqueId(), guard);
        return guard;
    }

    /**
     *
     * @param guard
     * @param modifyUser
     * @param add
     * @param level
     * @return false when updating or not deleting <p>true when inserting or deleting
     */
    public boolean setAccess(Guard guard, User modifyUser, boolean add, short level)
    {
        AccessListModel model =this.dsl.selectFrom(TABLE_ACCESS_LIST).
            where(TABLE_ACCESS_LIST.GUARD_ID.eq(guard.model.getId()),
                  TABLE_ACCESS_LIST.USER_ID.eq(modifyUser.getEntity().getKey())).fetchOne();
        if (add)
        {
            if (model == null)
            {
                model = this.dsl.newRecord(TABLE_ACCESS_LIST).newAccess(guard.model, modifyUser);
                model.setLevel(level);
                model.insert();
            }
            else
            {
                model.setLevel(level);
                model.update();
                return false;
            }
        }
        else // remove guard
        {
            if (model == null) return false;
            model.delete();
        }
        return true;
    }

    public boolean checkPass(Guard guard, String pass)
    {
        if (!guard.hasPass()) return false;

        synchronized (this.messageDigest)
        {
            this.messageDigest.reset();
            return Arrays.equals(this.messageDigest.digest(pass.getBytes()),guard.model.getPassword());
        }
    }

    public boolean canProtect(Material type)
    {
        return true; // TODO config can protect?
    }

    public boolean canProtect(EntityType type)
    {
        return true;
    }
}
