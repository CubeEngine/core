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
package de.cubeisland.engine.baumguard.storage;

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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;

import de.cubeisland.engine.baumguard.Baumguard;
import de.cubeisland.engine.baumguard.BlockGuardConfiguration;
import de.cubeisland.engine.baumguard.EntityGuardConfiguration;
import de.cubeisland.engine.baumguard.GuardPerm;
import de.cubeisland.engine.baumguard.commands.CommandListener;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.world.WorldManager;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.baumguard.storage.AccessListModel.ACCESS_ALL;
import static de.cubeisland.engine.baumguard.storage.AccessListModel.ACCESS_FULL;
import static de.cubeisland.engine.baumguard.storage.GuardType.PUBLIC;
import static de.cubeisland.engine.baumguard.storage.ProtectedType.getProtectedType;
import static de.cubeisland.engine.baumguard.storage.TableAccessList.TABLE_ACCESS_LIST;
import static de.cubeisland.engine.baumguard.storage.TableGuardLocations.TABLE_GUARD_LOCATION;
import static de.cubeisland.engine.baumguard.storage.TableGuards.TABLE_GUARD;

public class GuardManager implements Listener
{
    protected final DSLContext dsl;
    protected final Baumguard module;

    protected WorldManager wm;
    protected UserManager um;

    public final CommandListener commandListener;

    private final Map<Location, Guard> loadedGuards = new HashMap<>();
    private final Map<Chunk, Set<Guard>> loadedGuardsInChunk = new HashMap<>();
    private final Map<UUID, Guard> loadedEntityGuards = new HashMap<>();

    public final MessageDigest messageDigest;

    public GuardManager(Baumguard module)
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
        return getGuardAtLocation(location, true);
    }

    public Guard getGuardAtLocation(Location location, boolean access)
    {
        Guard guard = this.loadedGuards.get(location);
        if (access && guard != null) guard.model.setLastAccess(new Timestamp(System.currentTimeMillis()));
        return guard;
    }

    public Guard getGuardForEntityUID(UUID uniqueId, boolean access)
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
        if (access && guard != null) guard.model.setLastAccess(new Timestamp(System.currentTimeMillis()));
        return guard;
        // TODO handle unloading & garbage collection e.g. when entities got removed by WE
    }

    public Guard getGuardForEntityUID(UUID uniqueId)
    {
        return this.getGuardForEntityUID(uniqueId, true);
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
        for (Location location : guard.getLocations())
        {
            this.addLoadedLocationGuard(location, guard);
        }
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

    public void removeGuard(Guard guard, User user, boolean destroyed)
    {
        if (!destroyed)
        {
            if (!(guard.isOwner(user) || GuardPerm.CMD_REMOVE_OTHER.isAuthorized(user)))
            {
                user.sendTranslated("&cThis protection is not yours!");
                return;
            }
        }
        guard.model.delete();
        if (guard.isBlockGuard())
        {
            for (Location location : guard.getLocations())
            {
                this.loadedGuards.remove(location);
                Set<Guard> guards = this.loadedGuardsInChunk.get(location.getChunk());
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
        if (user != null) user.sendTranslated("&aRemoved Guard!");
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

    public Guard createGuard(Material material, Location location, User user, GuardType guardType, String password, boolean createKeyBook)
    {
        GuardModel model = this.dsl.newRecord(TABLE_GUARD).newGuard(user, guardType, getProtectedType(material));
        this.createPassword(model, password);
        model.insert();
        List<Location> locations = new ArrayList<>();
        Block block = location.getBlock();
        switch (material) // Double Block Protections
        {
            case CHEST:
            if (block.getState() instanceof Chest)
            {
                Chest chest = (Chest)block.getState();
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
                locations.add(location);
                if (block.getState().getData() instanceof Door)
                {
                    Block botBlock;
                    if (((Door)block.getState().getData()).isTopHalf())
                    {
                        locations.add(location.clone().add(0, -1, 0));
                        botBlock = locations.get(1).getBlock();
                        locations.add(locations.get(1).clone().add(0, -1, 0));
                    }
                    else
                    {
                        botBlock = location.getBlock();
                        locations.add(location.clone().add(0, 1, 0));
                        locations.add(location.clone().add(0, -1, 0));
                    }
                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(0)).insert();
                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(1)).insert();
                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(2)).insert();
                    for (BlockFace blockFace : BlockUtil.CARDINAL_DIRECTIONS)
                    {
                        if (botBlock.getRelative(blockFace).getType().equals(block.getType())) // same door type
                        {
                            Door relativeBot = (Door)botBlock.getRelative(blockFace).getState().getData();
                            if (!relativeBot.isTopHalf())
                            {
                                Door botDoor = (Door)botBlock.getState().getData();
                                Door topDoor = (Door)botBlock.getRelative(BlockFace.UP).getState().getData();
                                Door relativeTop = (Door)botBlock.getRelative(blockFace).getRelative(BlockFace.UP).getState().getData();
                                if (botDoor.getFacing() == relativeBot.getFacing() && topDoor.getData() != relativeTop.getData()) // Facing same & opposite hinge
                                {
                                    locations.add(botBlock.getRelative(blockFace).getLocation());
                                    locations.add(locations.get(3).clone().add(0,1,0));
                                    locations.add(locations.get(3).clone().add(0,-1,0));
                                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(3)).insert();
                                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(4)).insert();
                                    this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, locations.get(5)).insert();
                                    break;
                                }

                            } // else ignore
                        }
                    }
                }
        }
        if (locations.isEmpty())
        {
            locations.add(location);
            this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, location).insert();
        }
        Guard guard = new Guard(this, model, locations);
        this.addLoadedLocationGuard(guard);
        this.showCreatedMessage(guard, user);
        this.attemptCreatingKeyBook(guard, user, createKeyBook);
        return guard;
    }

    public Guard createGuard(Entity entity, User user, GuardType guardType, String password, boolean createKeyBook)
    {
        GuardModel model = this.dsl.newRecord(TABLE_GUARD).newGuard(user, guardType, getProtectedType(entity.getType()), entity.getUniqueId());
        this.createPassword(model, password);
        model.insert();
        Guard guard = new Guard(this, model);
        this.loadedEntityGuards.put(entity.getUniqueId(), guard);
        this.showCreatedMessage(guard, user);
        this.attemptCreatingKeyBook(guard, user, createKeyBook);
        return guard;
    }

    private void showCreatedMessage(Guard guard, User user)
    {
        switch (guard.getGuardType())
        {
            case PRIVATE:
                user.sendTranslated("&cPrivate Protection created!");
                break;
            case PUBLIC:
                user.sendTranslated("&cPublic Protection created!");
                break;
            case GUARDED:
                user.sendTranslated("&cGuarded Protection created!");
                break;
            case DONATION:
                user.sendTranslated("&cDonation Protection created!");
                break;
            case FREE:
                user.sendTranslated("&cFree Protection created!");
                break;
        }
    }

    public void attemptCreatingKeyBook(Guard guard, User user, Boolean third)
    {
        if (guard.getGuardType().equals(PUBLIC)) return; // ignore
        if (!this.module.getConfig().allowKeyBooks)
        {
            user.sendTranslated("&aKeyBooks are not enabled!");
            return;
        }
        if (third)
        {
            if (user.getItemInHand().getType().equals(Material.BOOK))
            {
                int amount = user.getItemInHand().getAmount() -1;
                ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK, 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(guard.getColorPass() + ChatFormat.parseFormats("&r&6KeyBook &8#" + guard.getId()));
                itemMeta.setLore(Arrays.asList(user.translate(ChatFormat.parseFormats("&eThis book can")),
                                               user.translate(ChatFormat.parseFormats("&eunlock a magically")),
                                               user.translate(ChatFormat.parseFormats("&elocked container"))));
                itemStack.setItemMeta(itemMeta);
                user.setItemInHand(itemStack);
                HashMap<Integer, ItemStack> full = user.getInventory().addItem(new ItemStack(Material.BOOK, amount));
                for (ItemStack stack : full.values())
                {
                    Location location = user.getLocation();
                    location.getWorld().dropItem(location, stack);
                }
                user.updateInventory();
            }
            else
            {
                user.sendTranslated("&cCould not create KeyBook! You need to hold a book in your hand in order to do this!");
            }
        }
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
        for (BlockGuardConfiguration blockprotection : this.module.getConfig().blockprotections)
        {
            if (blockprotection.isType(type))
            {
                return true;
            }
        }
        return false;
    }

    public boolean canProtect(EntityType type)
    {
        for (EntityGuardConfiguration entityProtection : this.module.getConfig().entityProtections)
        {
            if (entityProtection.isType(type))
            {
                return true;
            }
        }
        return false;
    }

    public void modifyGuard(Guard guard, User user, String usersString, Boolean adminAccess)
    {
        // TODO separate in & out for containers
        if (guard.isOwner(user) || guard.hasAdmin(user) || GuardPerm.CMD_MODIFY_OTHER.isAuthorized(user))
        {
            if (!guard.isPublic())
            {
                String[] explode = StringUtils.explode(",", usersString);
                for (String name : explode)
                {
                    boolean add = true;
                    if (name.startsWith("-"))
                    {
                        name = name.substring(1);
                        add = false;
                    }
                    User modifyUser = this.module.getCore().getUserManager().getUser(name, false);
                    if (modifyUser == null) throw new IllegalArgumentException(); // This is prevented by checking first in the cmd execution
                    short accessType = ACCESS_FULL;
                    if (add)
                    {
                        if (adminAccess)
                        {
                            accessType = ACCESS_ALL; // + AdminAccess
                        }
                    }
                    if (this.setAccess(guard, modifyUser, add, accessType))
                    {
                        if (add)
                        {
                            if (adminAccess)
                            {
                                user.sendTranslated("&aGranted &2%s&a admin access to this protection!", modifyUser.getName());
                            }
                            else
                            {
                                user.sendTranslated("&aGranted &2%s&a access to this protection!", modifyUser.getName());
                            }
                        }
                        else
                        {
                            user.sendTranslated("&aRemoved &2%s's&a access to this protection!", modifyUser.getName());
                        }
                    }
                    else
                    {
                        if (add)
                        {
                            if (adminAccess)
                            {
                                user.sendTranslated("&aUdated &2%s's&a access to admin access!", modifyUser.getName());
                            }
                            else
                            {
                                user.sendTranslated("&aUdated &2%s's&a access to normal access!", modifyUser.getName());
                            }
                        }
                        else
                        {
                            user.sendTranslated("&2%s&a had no access to this protection!", modifyUser.getName());
                        }
                    }
                }
                return;
            }
            user.sendTranslated("&eThis protection is public and so accessible to everyone");
            return;
        }
        user.sendTranslated("&cYou are not allowed to modify the access-list of this protection!");
    }
}
