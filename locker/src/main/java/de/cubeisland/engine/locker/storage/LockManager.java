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
package de.cubeisland.engine.locker.storage;

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
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.locker.BlockLockerConfiguration;
import de.cubeisland.engine.locker.EntityLockerConfiguration;
import de.cubeisland.engine.locker.LockerAttachment;
import de.cubeisland.engine.locker.LockerPerm;
import de.cubeisland.engine.locker.commands.CommandListener;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.locker.storage.AccessListModel.ACCESS_ALL;
import static de.cubeisland.engine.locker.storage.AccessListModel.ACCESS_FULL;
import static de.cubeisland.engine.locker.storage.LockType.PUBLIC;
import static de.cubeisland.engine.locker.storage.ProtectedType.getProtectedType;
import static de.cubeisland.engine.locker.storage.TableAccessList.TABLE_ACCESS_LIST;
import static de.cubeisland.engine.locker.storage.TableLockLocations.TABLE_GUARD_LOCATION;
import static de.cubeisland.engine.locker.storage.TableLocks.TABLE_GUARD;

public class LockManager implements Listener
{
    protected final DSLContext dsl;
    protected final de.cubeisland.engine.locker.Locker module;

    protected WorldManager wm;
    protected UserManager um;

    public final CommandListener commandListener;

    private final Map<Location, Lock> loadedLocks = new HashMap<>();
    private final Map<Chunk, Set<Lock>> loadedLocksInChunk = new HashMap<>();
    private final Map<UUID, Lock> loadedEntityLocks = new HashMap<>();

    public final MessageDigest messageDigest;

    public LockManager(de.cubeisland.engine.locker.Locker module)
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
     * Returns the Lock at given location
     *
     * @param location
     * @return the lock or null if there is no lock OR the chunk is not loaded
     */
    public Lock getLockAtLocation(Location location)
    {
        return getLockAtLocation(location, true);
    }

    public Lock getLockAtLocation(Location location, boolean access)
    {
        Lock lock = this.loadedLocks.get(location);
        if (lock != null && access)
        {
            lock.validateTypeAt(location);
            if ((this.module.getConfig().protectWhenOnlyOffline && lock.getOwner().isOnline())
             || (this.module.getConfig().protectWhenOnlyOnline && !lock.getOwner().isOnline()))
            {
                return null;
            }
            lock.model.setLastAccess(new Timestamp(System.currentTimeMillis()));
        }

        return lock;
    }

    public Lock getLockForEntityUID(UUID uniqueId, boolean access)
    {
        Lock lock = this.loadedEntityLocks.get(uniqueId);
        if (lock == null)
        {
            LockModel model = this.dsl.selectFrom(TABLE_GUARD).where(TABLE_GUARD.ENTITY_UID_LEAST.eq(uniqueId.getLeastSignificantBits()),
                                                                      TABLE_GUARD.ENTITY_UID_MOST.eq(uniqueId.getMostSignificantBits())).fetchOne();
            if (model != null)
            {
                lock = new Lock(this, model);
                this.loadedEntityLocks.put(uniqueId, lock);
            }
        }
        if (lock != null && access)
        {
            if ((this.module.getConfig().protectWhenOnlyOffline && lock.getOwner().isOnline())
                || (this.module.getConfig().protectWhenOnlyOnline && !lock.getOwner().isOnline()))
            {
                return null;
            }
            lock.model.setLastAccess(new Timestamp(System.currentTimeMillis()));
        }
        return lock;
        // TODO handle unloading & garbage collection e.g. when entities got removed by WE
    }

    public Lock getLockForEntityUID(UUID uniqueId)
    {
        return this.getLockForEntityUID(uniqueId, true);
    }

    public void extendLock(Lock lock, Location location)
    {
        lock.locations.add(location);
        LockLocationModel model = this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(lock.model, location);
        model.insert();
        this.loadedLocks.put(location.clone(), lock);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event)
    {
        this.loadChunk(event.getChunk());
    }

    private void loadChunk(Chunk chunk)
    {
        UInteger world_id = UInteger.valueOf(this.wm.getWorldId(chunk.getWorld()));
        Result<LockModel> models = this.dsl.selectFrom(TABLE_GUARD).where(
                    TABLE_GUARD.ID.in(this.dsl
                     .select(TABLE_GUARD_LOCATION.GUARD_ID)
                     .from(TABLE_GUARD_LOCATION)
                     .where(TABLE_GUARD_LOCATION.WORLD_ID.eq(world_id), TABLE_GUARD_LOCATION.CHUNKX.eq(chunk
                                                                                                           .getX()), TABLE_GUARD_LOCATION
                                .CHUNKZ.eq(chunk.getZ())))).fetch();
        Map<UInteger, Result<LockLocationModel>> locations = this.dsl.selectFrom(TABLE_GUARD_LOCATION)
                                                                      .where(TABLE_GUARD_LOCATION.GUARD_ID.in(models.getValues(TABLE_GUARD.ID)))
                                                                      .fetch().intoGroups(TABLE_GUARD_LOCATION.GUARD_ID);
        for (LockModel model : models)
        {
            Result<LockLocationModel> lockLoc = locations.get(model.getId());
            this.addLoadedLocationLock(new Lock(this, model, lockLoc));
        }
    }

    private void addLoadedLocationLock(Lock lock)
    {
        for (Location location : lock.getLocations())
        {
            this.addLoadedLocationLock(location, lock);
        }
    }

    private void addLoadedLocationLock(Location loc, Lock lock)
    {
        if (loc.getChunk().isLoaded())
        {
            Set<Lock> locks = this.loadedLocksInChunk.get(loc.getChunk());
            if (locks == null)
            {
                locks = new HashSet<>();
                this.loadedLocksInChunk.put(loc.getChunk(), locks);
            }
            locks.add(lock);
        }
        this.loadedLocks.put(loc, lock);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event)
    {
        Set<Lock> remove = this.loadedLocksInChunk.remove(event.getChunk());
        if (remove == null) return; // nothing to remove
        for (Lock lock : remove) // remove from chunks
        {
            if (lock.isSingleBlockLock())
            {
                this.loadedLocks.remove(lock.getLocation()); // remove loc
            }
            else
            {
                if (lock.getLocation().getChunk() == lock.getLocation().getChunk()) // same chunk remove both loc
                {
                    this.loadedLocks.remove(lock.getLocation());
                    this.loadedLocks.remove(lock.getLocation2());
                }
                else // different chunks
                {
                    Chunk c1 = lock.getLocation().getChunk();
                    Chunk c2 = lock.getLocation2().getChunk();
                    Chunk chunk = event.getChunk();
                    if ((!c1.isLoaded() && c2 == chunk)
                      ||(!c2.isLoaded() && c1 == chunk))
                    {// Both chunks will be unloaded remove both loc
                        this.loadedLocks.remove(lock.getLocation());
                        this.loadedLocks.remove(lock.getLocation2());
                    }
                    // else the other chunk is still loaded -> do not remove!
                }
            }
            lock.model.update(); // updates if changed (last_access timestamp)
        }
    }

    public void removeLock(Lock lock, User user, boolean destroyed)
    {
        if (!destroyed && (!(lock.isOwner(user) || LockerPerm.CMD_REMOVE_OTHER.isAuthorized(user))))
        {
            user.sendTranslated("&cThis protection is not yours!");
            return;
        }
        lock.model.delete();
        if (lock.isBlockLock())
        {
            for (Location location : lock.getLocations())
            {
                this.loadedLocks.remove(location);
                Set<Lock> locks = this.loadedLocksInChunk.get(location.getChunk());
                if (locks != null)
                {
                    locks.remove(lock);
                }
            }
        }
        else
        {
            this.loadedEntityLocks.remove(lock.model.getUUID());
        }
        if (user != null) user.sendTranslated("&aRemoved Lock!");
    }

    public void invalidateKeyBooks(Lock lock)
    {
        this.createPassword(lock.model, null);
        lock.model.update();
    }

    /**
     * Sets a new password for given lock-model
     *
     * @param model
     * @param pass
     */
    private void createPassword(LockModel model, String pass)
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

    public Lock createLock(Material material, Location location, User user, LockType lockType, String password, boolean createKeyBook)
    {
        LockModel model = this.dsl.newRecord(TABLE_GUARD).newLock(user, lockType, getProtectedType(material));
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
                    }
                    else
                    {
                        botBlock = location.getBlock();
                        locations.add(location.clone().add(0, 1, 0));
                    }
                    for (BlockFace blockFace : BlockUtil.CARDINAL_DIRECTIONS)
                    {
                        if (botBlock.getRelative(blockFace).getType() == block.getType()) // same door type
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
                                    locations.add(locations.get(2).clone().add(0, 1, 0));
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
        }
        for (Location loc : locations)
        {
            this.dsl.newRecord(TABLE_GUARD_LOCATION).newLocation(model, loc).insert();
        }
        Lock lock = new Lock(this, model, locations);
        this.addLoadedLocationLock(lock);
        this.showCreatedMessage(lock, user);
        this.attemptCreatingKeyBook(lock, user, createKeyBook);
        return lock;
    }

    public Lock createLock(Entity entity, User user, LockType lockType, String password, boolean createKeyBook)
    {
        LockModel model = this.dsl.newRecord(TABLE_GUARD).newLock(user, lockType, getProtectedType(entity.getType()), entity.getUniqueId());
        this.createPassword(model, password);
        model.insert();
        Lock lock = new Lock(this, model);
        this.loadedEntityLocks.put(entity.getUniqueId(), lock);
        this.showCreatedMessage(lock, user);
        this.attemptCreatingKeyBook(lock, user, createKeyBook);
        return lock;
    }

    private void showCreatedMessage(Lock lock, User user)
    {
        switch (lock.getLockType())
        {
            case PRIVATE:
                user.sendTranslated("&aPrivate Lock created!");
                break;
            case PUBLIC:
                user.sendTranslated("&aPublic Lock created!");
                break;
            case GUARDED:
                user.sendTranslated("&aGuarded Lock created!");
                break;
            case DONATION:
                user.sendTranslated("&aDonation Lock created!");
                break;
            case FREE:
                user.sendTranslated("&aFree Lock created!");
                break;
        }
    }

    public void attemptCreatingKeyBook(Lock lock, User user, Boolean third)
    {
        if (lock.getLockType() == PUBLIC) return; // ignore
        if (!this.module.getConfig().allowKeyBooks)
        {
            user.sendTranslated("&aKeyBooks are not enabled!");
            return;
        }
        if (third)
        {
            if (user.getItemInHand().getType() == Material.BOOK)
            {
                int amount = user.getItemInHand().getAmount() -1;
                ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK, 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(lock.getColorPass() + ChatFormat.parseFormats("&r&6KeyBook &8#" + lock.getId()));
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
     * @param lock
     * @param modifyUser
     * @param add
     * @param level
     * @return false when updating or not deleting <p>true when inserting or deleting
     */
    public boolean setAccess(Lock lock, User modifyUser, boolean add, short level)
    {
        AccessListModel model =this.dsl.selectFrom(TABLE_ACCESS_LIST).
            where(TABLE_ACCESS_LIST.LOCK_ID.eq(lock.model.getId()),
                  TABLE_ACCESS_LIST.USER_ID.eq(modifyUser.getEntity().getKey())).fetchOne();
        if (add)
        {
            if (model == null)
            {
                model = this.dsl.newRecord(TABLE_ACCESS_LIST).newAccess(lock.model, modifyUser);
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
        else // remove lock
        {
            if (model == null) return false;
            model.delete();
        }
        return true;
    }

    public boolean checkPass(Lock lock, String pass)
    {
        if (!lock.hasPass()) return false;

        synchronized (this.messageDigest)
        {
            this.messageDigest.reset();
            return Arrays.equals(this.messageDigest.digest(pass.getBytes()), lock.model.getPassword());
        }
    }

    public boolean canProtect(Material type)
    {
        for (BlockLockerConfiguration blockprotection : this.module.getConfig().blockprotections)
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
        for (EntityLockerConfiguration entityProtection : this.module.getConfig().entityProtections)
        {
            if (entityProtection.isType(type))
            {
                return true;
            }
        }
        return false;
    }

    public void modifyLock(Lock lock, User user, String usersString)
    {
        // TODO separate in & out for containers
        if (lock.isOwner(user) || lock.hasAdmin(user) || LockerPerm.CMD_MODIFY_OTHER.isAuthorized(user))
        {
            if (!lock.isPublic())
            {
                String[] explode = StringUtils.explode(",", usersString);
                for (String name : explode)
                {
                    boolean add = true;
                    boolean admin = false;
                    if (name.startsWith("@"))
                    {
                        name = name.substring(1);
                        admin = true;
                    }
                    if (name.startsWith("-"))
                    {
                        name = name.substring(1);
                        add = false;
                    }
                    User modifyUser = this.module.getCore().getUserManager().getUser(name, false);
                    if (modifyUser == null) throw new IllegalArgumentException(); // This is prevented by checking first in the cmd execution
                    short accessType = ACCESS_FULL;
                    if (add && admin)
                    {
                        accessType = ACCESS_ALL; // with AdminAccess
                    }
                    if (this.setAccess(lock, modifyUser, add, accessType))
                    {
                        if (add)
                        {
                            if (admin)
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
                            if (admin)
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

    public void saveAll()
    {
        for (Lock lock : this.loadedEntityLocks.values())
        {
            lock.model.update();
        }
        for (Lock lock : this.loadedLocks.values())
        {
            lock.model.update();
        }
    }

    /**
     * Returns true if the chest could open
     * <p>null if the chest cannot be opened with the KeyBook
     * <p>false if the user has no KeyBook in hand
     *
     * @param lock
     * @param user
     * @param effectLocation
     * @return
     */
    public Boolean checkForKeyBook(Lock lock, User user, Location effectLocation)
    {
        if (user.getItemInHand().getType() == Material.ENCHANTED_BOOK && user.getItemInHand().getItemMeta().getDisplayName().contains("KeyBook"))
        {
            String keyBookName = user.getItemInHand().getItemMeta().getDisplayName();
            try
            {
                long id = Long.valueOf(keyBookName.substring(keyBookName.indexOf('#')+1, keyBookName.length()));
                if (lock.getId().equals(id)) // Id matches ?
                {
                    // Validate book
                    if (keyBookName.startsWith(lock.getColorPass()))
                    {
                        if (effectLocation != null) user.sendTranslated("&aAs you approach with your KeyBook the magic lock disappears!");
                        user.playSound(effectLocation, Sound.PISTON_EXTEND, 1, 2);
                        user.playSound(effectLocation, Sound.PISTON_EXTEND, 1, (float)1.5);
                        if (effectLocation != null) lock.notifyKeyUsage(user);
                        return true;
                    }
                    else
                    {
                        user.sendTranslated("&cYou try to open the container with your KeyBook\n" +
                                                "but forcefully get pushed away!");
                        ItemStack itemInHand = user.getItemInHand();
                        ItemMeta itemMeta = itemInHand.getItemMeta();
                        itemMeta.setDisplayName(ChatFormat.parseFormats("&4Broken KeyBook"));
                        itemMeta.setLore(Arrays.asList(ChatFormat.parseFormats(user.translate("&eThis KeyBook")),
                                                       ChatFormat.parseFormats(user.translate("&elooks old and")),
                                                       ChatFormat.parseFormats(user.translate("&eused up. It")),
                                                       ChatFormat.parseFormats(user.translate("&ewont let you")),
                                                       ChatFormat.parseFormats(user.translate("&eopen any containers!"))));
                        itemInHand.setItemMeta(itemMeta);
                        itemInHand.setType(Material.PAPER);
                        user.updateInventory();
                        user.playSound(effectLocation, Sound.GHAST_SCREAM, 1, 1);
                        final Vector userDirection = user.getLocation().getDirection();
                        user.damage(1);
                        user.setVelocity(userDirection.multiply(-3));
                        return null;
                    }
                }
                else
                {
                    user.sendTranslated("&eYou try to open the container with your KeyBook but nothing happens!");
                    user.playSound(effectLocation, Sound.BLAZE_HIT, 1, 1);
                    user.playSound(effectLocation, Sound.BLAZE_HIT, 1, (float)0.8);
                    return null;
                }
            }
            catch (NumberFormatException|IndexOutOfBoundsException ignore) // invalid book / we do not care
            {}
        }
        return false;
    }

    public boolean handleAccess(Lock lock, User user, Location soundLocation, Cancellable event)
    {
        if (lock == null) return true;
        if (lock.isOwner(user)) return true;
        Boolean keyBookUsed = this.checkForKeyBook(lock, user, soundLocation);
        if (keyBookUsed == null)
        {
            event.setCancelled(true);
            return false;
        }
        return keyBookUsed || this.checkForUnlocked(lock, user);
    }

    public boolean checkForUnlocked(Lock lock, User user)
    {
        LockerAttachment lockerAttachment = user.get(LockerAttachment.class);
        return lockerAttachment != null && lockerAttachment.hasUnlocked(lock);
    }

    /**
     * Returns the lock for given inventory it exists, also sets the location to the holders location if not null
     *
     * @param inventory
     * @param holderLoc a location object to hold the LockLocation
     * @return the lock for given inventory
     */
    public Lock getLockOfInventory(Inventory inventory, Location holderLoc)
    {
        InventoryHolder holder = inventory.getHolder();
        Lock lock;
        if (holderLoc == null)
        {
            holderLoc = new Location(null, 0, 0, 0);
        }
        if (holder instanceof Entity)
        {
            lock = this.getLockForEntityUID(((Entity)holder).getUniqueId());
            ((Entity)holder).getLocation(holderLoc);
        }
        else
        {
            Location lockLoc;
            if (holder instanceof BlockState)
            {
                lockLoc = ((BlockState)holder).getLocation(holderLoc);
            }
            else if (holder instanceof DoubleChest)
            {
                lockLoc = ((BlockState)((DoubleChest)holder).getRightSide()).getLocation(holderLoc);
            }
            else return null;
            lock = this.getLockAtLocation(lockLoc);
        }
        return lock;
    }
}
