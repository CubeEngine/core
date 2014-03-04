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
import java.sql.Timestamp;
import java.util.ArrayList;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Door;

import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.task.FutureCallback;
import de.cubeisland.engine.core.task.ListenableFuture;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.locker.BlockLockerConfiguration;
import de.cubeisland.engine.locker.EntityLockerConfiguration;
import de.cubeisland.engine.locker.Locker;
import de.cubeisland.engine.locker.commands.CommandListener;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.contract.Contract.expect;
import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.util.LocationUtil.getChunkKey;
import static de.cubeisland.engine.core.util.LocationUtil.getLocationKey;
import static de.cubeisland.engine.locker.storage.AccessListModel.ACCESS_ALL;
import static de.cubeisland.engine.locker.storage.AccessListModel.ACCESS_FULL;
import static de.cubeisland.engine.locker.storage.ProtectedType.getProtectedType;
import static de.cubeisland.engine.locker.storage.TableAccessList.TABLE_ACCESS_LIST;
import static de.cubeisland.engine.locker.storage.TableLockLocations.TABLE_LOCK_LOCATION;
import static de.cubeisland.engine.locker.storage.TableLocks.TABLE_LOCK;

public class LockManager implements Listener
{
    protected final DSLContext dsl;
    protected final Locker module;

    private final Database database;
    protected WorldManager wm;
    protected UserManager um;

    public final CommandListener commandListener;

    private final Map<Long, Map<Long, Lock>> loadedLocks = new HashMap<>();
    private final Map<Long, Map<Long, Set<Lock>>> loadedLocksInChunk = new HashMap<>();
    private final Map<UUID, Lock> loadedEntityLocks = new HashMap<>();
    private final Map<Long, Lock> locksById = new HashMap<>();

    public final MessageDigest messageDigest;

    public LockManager(Locker module)
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
        this.database = module.getCore().getDB();
        ListenableFuture future = null;
        for (World world : module.getCore().getWorldManager().getWorlds())
        {
            for (Chunk chunk : world.getLoadedChunks())
            {
                future = this.loadFromChunk(chunk);
            }
        }
        if (future != null)
        {
            future.addCallback(new FutureCallback()
            {
                @Override
                public void onSuccess(Object o)
                {
                    LockManager.this.module.getLog().info("Finished Loading Locks");
                }
            });
        }
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event)
    {
        this.loadFromChunk(event.getChunk());
    }

    private ListenableFuture loadFromChunk(Chunk chunk)
    {
        UInteger world_id = UInteger.valueOf(this.wm.getWorldId(chunk.getWorld()));
        return this.database.fetchLater(this.database.getDSL().selectFrom(TABLE_LOCK).where(
            TABLE_LOCK.ID.in(this.database.getDSL().select(TABLE_LOCK_LOCATION.GUARD_ID)
                                     .from(TABLE_LOCK_LOCATION)
                                     .where(TABLE_LOCK_LOCATION.WORLD_ID.eq(world_id),
                                            TABLE_LOCK_LOCATION.CHUNKX.eq(chunk.getX()),
                                            TABLE_LOCK_LOCATION.CHUNKZ.eq(chunk.getZ()))))).
                         addCallback(new FutureCallback<Result<LockModel>>()
                         {
                             @Override
                             public void onSuccess(Result<LockModel> models)
                             {
                                 Map<UInteger, Result<LockLocationModel>> locations = LockManager.
                                     this.database.getDSL().selectFrom(TABLE_LOCK_LOCATION)
                                      .where(TABLE_LOCK_LOCATION.GUARD_ID.in(models.getValues(TABLE_LOCK.ID)))
                                      .fetch().intoGroups(TABLE_LOCK_LOCATION.GUARD_ID);
                                 for (LockModel model : models)
                                 {
                                     Result<LockLocationModel> lockLoc = locations.get(model.getId());
                                     addLoadedLocationLock(new Lock(LockManager.this, model, lockLoc));
                                 }
                             }

                             @Override
                             public void onFailure(Throwable t)
                             {
                                 module.getLog().error("Error while getting locks from database", t);
                             }
                         });
    }

    private void addLoadedLocationLock(Lock lock)
    {
        this.locksById.put(lock.getId(), lock);
        Long worldId = null;
        for (Location loc : lock.getLocations())
        {
            if (worldId == null)
            {
                worldId = module.getCore().getWorldManager().getWorldId(loc.getWorld());
            }
            Map<Long, Set<Lock>> locksInChunkMap = this.getChunkLocksMap(worldId);
            long chunkKey = getChunkKey(loc);
            Set<Lock> locks = locksInChunkMap.get(chunkKey);
            if (locks == null)
            {
                locks = new HashSet<>();
                locksInChunkMap.put(chunkKey, locks);
            }
            locks.add(lock);
            this.getLocLockMap(worldId).put(getLocationKey(loc), lock);
        }
    }

    private Map<Long, Set<Lock>> getChunkLocksMap(long worldId)
    {
        Map<Long, Set<Lock>> locksInChunkMap = this.loadedLocksInChunk.get(worldId);
        if (locksInChunkMap == null)
        {
            locksInChunkMap = new HashMap<>();
            this.loadedLocksInChunk.put(worldId, locksInChunkMap);
        }
        return locksInChunkMap;
    }

    private Map<Long, Lock> getLocLockMap(long worldId)
    {
        Map<Long, Lock> locksAtLocMap = this.loadedLocks.get(worldId);
        if (locksAtLocMap == null)
        {
            locksAtLocMap = new HashMap<>();
            this.loadedLocks.put(worldId, locksAtLocMap);
        }
        return locksAtLocMap;
    }

    @EventHandler
    private void onChunkUnload(ChunkUnloadEvent event)
    {
        long worldId = module.getCore().getWorldManager().getWorldId(event.getWorld());
        Set<Lock> remove = this.getChunkLocksMap(worldId).remove(getChunkKey(event.getChunk().getX(), event.getChunk().getZ()));
        if (remove == null) return; // nothing to remove
        Map<Long, Lock> locLockMap = this.getLocLockMap(worldId);
        for (Lock lock : remove) // remove from chunks
        {
            Location firstLoc = lock.getFirstLocation();
            this.locksById.remove(lock.getId());
            Chunk c1 = firstLoc.getChunk();
            for (Location location : lock.getLocations())
            {
                if (location.getChunk() != c1) // different chunks
                {
                    Chunk c2 = location.getChunk();
                    Chunk chunk = event.getChunk();
                    if ((!c1.isLoaded() && c2 == chunk)
                        ||(!c2.isLoaded() && c1 == chunk))
                    {// Both chunks will be unloaded remove both loc
                        for (Location loc : lock.getLocations())
                        {
                            locLockMap.remove(getLocationKey(loc));
                        }
                        lock.model.update();
                    }
                    // else the other chunk is still loaded -> do not remove!
                    return;
                }
            }
            for (Location loc : lock.getLocations())
            {
                locLockMap.remove(getLocationKey(loc));
            }
            lock.model.update(); // updates if changed (last_access timestamp)
        }
    }

    /**
     * Returns the Lock at given location if the lock there is active
     *
     * @param location the location of the lock
     * @param user the user to get the lock for (can be null)
     * @return the lock or null if there is no lock OR the chunk is not loaded OR the lock is disabled
     */
    public Lock getLockAtLocation(Location location, User user)
    {
        return getLockAtLocation(location, user, true);
    }

    /**
     * Returns the Lock at given Location
     *
     * @param location the location of the lock
     * @param access whether to access the lock or just get information from it
     * @return the lock or null if there is no lock OR the chunk is not loaded
     */
    public Lock getLockAtLocation(Location location, User user, boolean access, boolean repairExpand)
    {
        long worldId = module.getCore().getWorldManager().getWorldId(location.getWorld());
        Lock lock = this.getLocLockMap(worldId).get(getLocationKey(location));
        if (repairExpand && lock != null && lock.isSingleBlockLock())
        {
            Block block = lock.getFirstLocation().getBlock();
            if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST)
            {
                for (BlockFace cardinalDirection : BlockUtil.CARDINAL_DIRECTIONS)
                {
                    Block relative = block.getRelative(cardinalDirection);
                    if (relative.getType() == block.getType())
                    {
                        if (this.getLockAtLocation(relative.getLocation(),null, false,false)== null)
                        {
                            this.extendLock(lock, relative.getLocation());
                            if (user != null)
                            {
                                user.sendTranslated("&aProtection repaired & expanded!");
                            }
                        }
                        else if (user != null)
                        {
                            user.sendTranslated("&4Broken Protection detected! Try /cremove on nearby blocks!");
                            user.sendTranslated("&eIf this message keeps coming please contact an administrator!");
                        }
                        break;
                    }
                }
            }
        }
        if (lock != null && access)
        {
            if (!lock.validateTypeAt(location))
            {
                lock.delete(user);
                if (user != null)
                {
                    user.sendTranslated("&eDetected invalid BlockProtection is now deleted!");
                }
            }
            return this.handleLockAccess(lock, access);
        }
        return lock;
    }

    public Lock getLockAtLocation(Location location, User user, boolean access)
    {
        return this.getLockAtLocation(location, user, access, true);
    }

    /**
     * Returns the Lock for given entityUID
     *
     * @param uniqueId the entities unique id
     * @return the entity-lock or null if there is no lock OR the lock is disabled
     */
    public Lock getLockForEntityUID(UUID uniqueId)
    {
        return this.getLockForEntityUID(uniqueId, true);
    }

    /**
     * Returns the Lock for given entityUID
     *
     * @param uniqueId the entities unique id
     * @param access whether to access the lock or just get information from it
     * @return the entity-lock or null if there is no lock
     */
    public Lock getLockForEntityUID(UUID uniqueId, boolean access)
    {
        Lock lock = this.loadedEntityLocks.get(uniqueId);
        if (lock == null)
        {
            LockModel model = this.dsl.selectFrom(TABLE_LOCK).where(TABLE_LOCK.ENTITY_UID_LEAST.eq(uniqueId.getLeastSignificantBits()),
                                                                      TABLE_LOCK.ENTITY_UID_MOST.eq(uniqueId.getMostSignificantBits())).fetchOne();
            if (model != null)
            {
                lock = new Lock(this, model);
                this.loadedEntityLocks.put(uniqueId, lock);
            }
        }
        return this.handleLockAccess(lock, access);
    }

    private Lock handleLockAccess(Lock lock, boolean access)
    {
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
    }

    /**
     * Extends a location lock onto an other location
     *
     * @param lock the lock to extend
     * @param location the location to extend to
     */
    public void extendLock(Lock lock, Location location)
    {
        expectNotNull(lock, "The lock must not be null!");
        expect(this.getLockAtLocation(location, null, false, false) == null , "Cannot extend Lock onto another!");
        lock.locations.add(location);
        LockLocationModel model = this.dsl.newRecord(TABLE_LOCK_LOCATION).newLocation(lock.model, location);
        model.insert();
        long worldId = module.getCore().getWorldManager().getWorldId(location.getWorld());
        this.getLocLockMap(worldId).put(getLocationKey(location), lock);
    }

    /**
     * Removes a Lock if th
     * e user is authorized or the lock destroyed
     *
     * @param lock the lock to remove
     * @param user the user removing the lock (can be null)
     * @param destroyed true if the Lock is already destroyed
     */
    public void removeLock(Lock lock, User user, boolean destroyed)
    {
        if (destroyed || lock.isOwner(user) || module.perms().CMD_REMOVE_OTHER.isAuthorized(user))
        {
            this.locksById.remove(lock.getId());
            lock.model.delete();
            if (lock.isBlockLock())
            {
                for (Location location : lock.getLocations())
                {
                    long chunkKey = getChunkKey(location);
                    long worldId = module.getCore().getWorldManager().getWorldId(location.getWorld());
                    this.getLocLockMap(worldId).remove(getLocationKey(location));
                    Set<Lock> locks = this.getChunkLocksMap(worldId).get(chunkKey);
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
            if (user != null)
            {
                user.sendTranslated("&aRemoved Lock!");
            }
            return;
        }
        user.sendTranslated("&cThis protection is not yours!");
    }

    /**
     * Creates a new Lock at given Location
     *
     * @param material the material at given location (can missmatch if block is just getting placed)
     * @param location the location to create the lock for
     * @param user the user creating the lock
     * @param lockType the lockType
     * @param password the password
     * @param createKeyBook whether to attempt to create a keyBook
     * @return the created Lock
     */
    public Lock createLock(Material material, Location location, User user, LockType lockType, String password, boolean createKeyBook)
    {
        LockModel model = this.dsl.newRecord(TABLE_LOCK).newLock(user, lockType, getProtectedType(material));
        model.createPassword(this, password).insert();
        List<Location> locations = new ArrayList<>();
        Block block = location.getBlock();
        // Handle MultiBlock Protections
        if (material == Material.CHEST)
        {
            if (block.getState() instanceof Chest && ((Chest)block.getState()).getInventory().getHolder() instanceof DoubleChest)
            {
                DoubleChest dc = (DoubleChest)((Chest)block.getState()).getInventory().getHolder();
                locations.add(((BlockState)dc.getLeftSide()).getLocation());
                locations.add(((BlockState)dc.getRightSide()).getLocation());
            }
        }
        else if (material == Material.WOODEN_DOOR || material == Material.IRON_DOOR_BLOCK)
        {
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
            this.dsl.newRecord(TABLE_LOCK_LOCATION).newLocation(model, loc).insert();
        }
        Lock lock = new Lock(this, model, locations);
        this.addLoadedLocationLock(lock);
        lock.showCreatedMessage(user);
        lock.attemptCreatingKeyBook(user, createKeyBook);
        for (BlockLockerConfiguration blockprotection : this.module.getConfig().blockprotections)
        {
            if (blockprotection.isType(material))
            {
                short flags = blockprotection.getFlags();
                if (flags != 0)
                {
                    lock.setFlags((short)(lock.getFlags() | flags));
                    lock.model.update();
                }
                break;
            }
        }
        return lock;
    }

    /**
     * Creates a new Lock for given Entity
     *
     * @param entity the entity to protect
     * @param user user the user creating the lock
     * @param lockType the lockType
     * @param password the password
     * @param createKeyBook whether to attempt to create a keyBook
     * @return the created Lock
     */
    public Lock createLock(Entity entity, User user, LockType lockType, String password, boolean createKeyBook)
    {
        LockModel model = this.dsl.newRecord(TABLE_LOCK).newLock(user, lockType, getProtectedType(entity.getType()), entity.getUniqueId());
        model.createPassword(this, password);
        model.insert();
        Lock lock = new Lock(this, model);
        this.loadedEntityLocks.put(entity.getUniqueId(), lock);
        this.locksById.put(lock.getId(), lock);
        lock.showCreatedMessage(user);
        lock.attemptCreatingKeyBook(user, createKeyBook);
        for (EntityLockerConfiguration entityProtection : this.module.getConfig().entityProtections)
        {
            if (entityProtection.isType(entity.getType()))
            {
                short flags = entityProtection.getFlags();
                if (flags != 0)
                {
                    lock.setFlags((short)(lock.getFlags() | flags));
                    lock.model.update();
                }
                break;
            }
        }
        return lock;
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

    public void saveAll()
    {
        for (Lock lock : this.loadedEntityLocks.values())
        {
            lock.model.update();
        }
        for (Map<Long, Lock> lockMap : this.loadedLocks.values())
        {
            for (Lock lock :lockMap.values())
            {
                lock.model.update();
            }
        }
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
            lock = this.getLockAtLocation(lockLoc, null);
        }
        return lock;
    }

    /**
     * The returned Lock should not be saved for later use!
     *
     * @param lockID the locks id
     * @return a copy of the Lock with given id
     */
    public Lock getLockById(long lockID)
    {
        Lock lock = this.locksById.get(lockID);
        if (lock != null)
        {
            return lock;
        }
        LockModel lockModel = this.dsl.selectFrom(TABLE_LOCK).where(TABLE_LOCK.ID.eq(UInteger.valueOf(lockID))).fetchOne();
        if (lockModel != null)
        {
            Result<LockLocationModel> fetch = this.dsl.selectFrom(TABLE_LOCK_LOCATION)
                                                      .where(TABLE_LOCK_LOCATION.GUARD_ID.eq(lockModel.getId()))
                                                      .fetch();
            if (fetch.isEmpty())
            {
                return new Lock(this, lockModel);
            }
            return new Lock(this, lockModel, fetch);
        }
        return null;
    }

    public void setGlobalAccess(User sender, String string)
    {
        String[] explode = StringUtils.explode(",", string);
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
            User modifyUser = this.um.getUser(name, false);
            if (modifyUser == null) throw new IllegalArgumentException(); // This is prevented by checking first in the cmd execution
            short accessType = ACCESS_FULL;
            if (add && admin)
            {
                accessType = ACCESS_ALL; // with AdminAccess
            }
            AccessListModel accessListModel = this.dsl.selectFrom(TABLE_ACCESS_LIST).where(
                TABLE_ACCESS_LIST.USER_ID.eq(modifyUser.getEntity().getKey()),
                TABLE_ACCESS_LIST.OWNER_ID.eq(sender.getEntity().getKey())).fetchOne();
            if (add)
            {
                if (accessListModel == null)
                {
                    accessListModel = this.dsl.newRecord(TABLE_ACCESS_LIST).newGlobalAccess(sender, modifyUser, accessType);
                    accessListModel.insert();
                    sender.sendTranslated("&aGlobal access for &2%s&a set!", modifyUser.getName());
                }
                else
                {
                    accessListModel.setLevel(accessType);
                    accessListModel.update();
                    sender.sendTranslated("&aUpdated global access-level for &2%s&a!", modifyUser.getName());
                }
            }
            else
            {
                if (accessListModel == null)
                {
                    sender.sendTranslated("&2%s&e had no global access!", modifyUser.getName());
                }
                else
                {
                    accessListModel.delete();
                    sender.sendTranslated("&aRemoved global access from &2%s", modifyUser.getName());
                }
            }
        }
    }

    public void purgeLocksFrom(User user)
    {
        this.dsl.delete(TABLE_LOCK).where(TABLE_LOCK.OWNER_ID.eq(user.getEntity().getKey())).execute();
    }

}
