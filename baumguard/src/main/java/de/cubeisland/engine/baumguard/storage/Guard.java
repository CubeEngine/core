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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.Door;

import de.cubeisland.engine.baumguard.GuardAttachment;
import de.cubeisland.engine.baumguard.GuardPerm;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.InventoryGuardFactory;
import org.jooq.Record1;
import org.jooq.Result;

import static de.cubeisland.engine.baumguard.storage.AccessListModel.ACCESS_ADMIN;
import static de.cubeisland.engine.baumguard.storage.GuardType.PUBLIC;
import static de.cubeisland.engine.baumguard.storage.TableAccessList.TABLE_ACCESS_LIST;

public class Guard
{
    private GuardManager manager;
    protected final GuardModel model;
    protected final ArrayList<Location> locations = new ArrayList<>();

    private Integer taskId = null; // for autoclosing doors

    /**
     * EntityGuard
     *
     * @param manager
     * @param model
     */
    public Guard(GuardManager manager, GuardModel model)
    {
        this.manager = manager;
        this.model = model;
        this.checkGuardType();
    }

    /**
     * BlockGuard
     *
     * @param manager
     * @param model
     * @param guardLocs
     */
    public Guard(GuardManager manager, GuardModel model, Result<GuardLocationModel> guardLocs)
    {
        this(manager, model);
        for (GuardLocationModel guardLoc : guardLocs)
        {
            this.locations.add(this.getLocation(guardLoc));
        }
        this.isValidType = false;
    }

    public Guard(GuardManager manager, GuardModel model, List<Location> locations)
    {
        this(manager, model);
        this.locations.addAll(locations);
        this.isValidType = false;
    }

    private Location getLocation(GuardLocationModel model)
    {
        return new Location(this.manager.wm.getWorld(model.getWorldId().longValue()), model.getX(), model.getY(), model.getZ());
    }

    public boolean isBlockGuard()
    {
        return !this.locations.isEmpty();
    }

    public boolean isSingleBlockGuard()
    {
        return this.locations.size() == 1;
    }

    public Location getLocation()
    {
        return this.locations.get(0);
    }

    public Location getLocation2()
    {
        return this.locations.get(1);
    }

    public ArrayList<Location> getLocations()
    {
        return this.locations;
    }

    public void handleBlockDoorUse(Cancellable event, User user, Location clickedDoor)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey())) return; // Its the owner
        switch (this.getGuardType())
        {
            case PUBLIC:
                this.doorUse(user, clickedDoor);
                return; // Allow everything
            case PRIVATE: // block changes
                break;
            case GUARDED:
            case DONATION:
            case FREE:
            default: // Not Allowed for doors
                throw new IllegalStateException();
        }
        AccessListModel access = this.getAccess(user);
        if (access == null || !(access.canIn() && access.canOut())) // No access Or not full access
        {
            event.setCancelled(true);
            user.sendTranslated("&cA magical lock prevents you from using this door!");
            return;
        } // else has access
        user.sendTranslated("&eThis door is protected by &2%s", this.getOwner().getName()); // TODO better
        this.doorUse(user, clickedDoor);
    }

    private AccessListModel getAccess(User user)
    {
        return this.manager.dsl.selectFrom(TABLE_ACCESS_LIST).
            where(TABLE_ACCESS_LIST.GUARD_ID.eq(this.model.getId()),
                  TABLE_ACCESS_LIST.USER_ID.eq(user.getEntity().getKey())).fetchOne();
    }

    public void handleInventoryOpen(Cancellable event, Inventory guardedInventory, User user)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey())) return; // Its the owner
        boolean in;
        boolean out;
        switch (this.getGuardType())
        {
            default: throw new IllegalStateException();
            case PUBLIC: return; // Allow everything
            case PRIVATE: // block changes
            case GUARDED:
                in = false;
                out = false;
                break;
            case DONATION:
                in = true;
                out = false;
                break;
            case FREE:
                in = false;
                out = true;
        }
        AccessListModel access = this.getAccess(user);
        if (access == null && this.getGuardType() == GuardType.PRIVATE)
        {
            event.setCancelled(true); // private & no access
            user.sendTranslated("&cA magical lock prevents you from accessing this inventory!"); // TODO better
        }
        else // Has access access -> new InventoryGuard
        {
            if (access != null)
            {
                in = in || access.canIn();
                out = out || access.canOut();
            }
            if (in && out) return; // Has full access
            if (guardedInventory == null) return; // Just checking else do guard
            InventoryGuardFactory inventoryGuardFactory = InventoryGuardFactory.prepareInventory(guardedInventory, user);
            if (!in)
            {
                inventoryGuardFactory.blockPutInAll();
            }
            if (!out)
            {
                inventoryGuardFactory.blockTakeOutAll();
            }
            inventoryGuardFactory.submitInventory(this.manager.module, false);
            user.sendTranslated("&eThis %s is protected by &2%s", "container", this.getOwner().getName()); // TODO better
        }
    }

    public void handleEntityInteract(Cancellable event, Entity entity, User user)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey())) return; // Its the owner
        if (this.getGuardType() == PUBLIC) return;
        AccessListModel access = this.getAccess(user);
        if (access == null && this.getGuardType() == GuardType.PRIVATE)
        {
            event.setCancelled(true); // private & no access
            user.sendTranslated("&cMagic repelled your attempts to reach this entity!"); // TODO better
        }
        else // has access
        {
            // TODO messages
        }
    }

    private void checkGuardType()
    {
        if (this.getGuardType().supportedTypes.contains(this.getProtectedType())) return;
        throw new IllegalStateException("GuardType is not supported for " + this.getProtectedType().name() + ":" + this.getGuardType().name());
    }

    public ProtectedType getProtectedType()
    {
        return ProtectedType.forByte(this.model.getType());
    }

    public GuardType getGuardType()
    {
        return GuardType.forByte(this.model.getGuardType());
    }

    public void handleBlockBreak(BlockBreakEvent event, User user)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey()) || GuardPerm.CMD_REMOVE_OTHER.isAuthorized(user))
        {
            this.delete(user);
            return;
        }
        event.setCancelled(true);
        user.sendTranslated("&cMagic prevents you from breaking this protection!");
    }

    public void handleEntityDamage(Cancellable event, User user)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey()))
        {
            user.sendTranslated("&eThe magic surrounding this entity quivers as you hit it!");
        }
        else
        {
            event.setCancelled(true); // private & no access
            user.sendTranslated("&cMagic repelled your attempts to hurt this entity!");
        }
    }

    public void handleEntityDeletion(User user)
    {
        this.delete(user);
    }

    /**
     * Deletes a protection and informs the given user
     *
     * @param user
     */
    public void delete(User user)
    {
        this.manager.removeGuard(this, user, true);
    }

    public boolean isOwner(User user)
    {
        return this.model.getOwnerId().equals(user.getEntity().getKey());
    }

    public boolean hasAdmin(User user)
    {
        Record1<Short> record1 = this.manager.dsl.select(TABLE_ACCESS_LIST.LEVEL).from(TABLE_ACCESS_LIST)
                                                  .where(TABLE_ACCESS_LIST.USER_ID.eq(user.getEntity().getKey()),
                                                         TABLE_ACCESS_LIST.GUARD_ID.eq(this.model.getId())).fetchOne();
        return record1 != null && (record1.value1() & ACCESS_ADMIN) == ACCESS_ADMIN;
    }

    public String getColorPass()
    {
        return this.model.getColorPass();
    }

    public Long getId()
    {
        return this.model.getId().longValue();
    }

    public boolean hasPass()
    {
        return this.model.getPassword().length > 4;
    }

    private Map<String, Long> lastKeyNotify;

    public void notifyKeyUsage(User user)
    {
        if (lastKeyNotify == null)
        {
            this.lastKeyNotify = new HashMap<>();
        }
        User owner = this.manager.um.getUser(this.model.getOwnerId().longValue());
        Long last = this.lastKeyNotify.get(owner.getName());
        if (last == null || TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - last) > 60) // 60 sec config ?
        {
            this.lastKeyNotify.put(owner.getName(), System.currentTimeMillis());
            owner.sendTranslated("&2%s&e used a KeyBook to access one of your protections!", user.getName());
        }
    }

    public User getOwner()
    {
        return this.manager.module.getCore().getUserManager().getUser(this.model.getOwnerId().longValue());
    }

    public boolean isPublic()
    {
        return this.getGuardType() == PUBLIC;
    }

    public boolean hasFlag(ProtectionFlags flag)
    {
        return flag.flagValue == (this.model.getFlags() & flag.flagValue);
    }

    public void showInfo(User user)
    {
        if (this.isOwner(user) || this.hasAdmin(user) || GuardPerm.CMD_INFO_OTHER.isAuthorized(user))
        {
            user.sendMessage("");
            user.sendTranslated("&aProtection: &6#%d&a Type: &6%s&a by &6%s", this.getId(), this.getGuardType().name(), this.getOwner().getName());
            user.sendTranslated("&aprotects &6%s&a since &6%s", this.getProtectedType().name(), this.model.getCreated().toString());
            user.sendTranslated("&alast access was &6%s", this.model.getLastAccess().toString());
            if (this.hasPass())
            {
                if (user.attachOrGet(GuardAttachment.class, this.manager.module).hasUnlocked(this))
                {
                    user.sendTranslated("&aHas a password and is currently unlocked");
                }
                else
                {
                    user.sendTranslated("&aHas a password and is currently locked");
                }
            }

            if (!this.locations.isEmpty())
            {
                user.sendTranslated("&aThis protections covers &6%d&a blocks!", this.locations.size());
            }
            // TODO active flags
            // TODO droptransfer status
            // TODO all accessors /w accesslevel
        }
        else
        {
            user.sendTranslated("&aProtectionType: &6%s", this.getId(), this.getGuardType().name());
            AccessListModel access = this.getAccess(user);
            if (this.hasPass())
            {
                if (user.attachOrGet(GuardAttachment.class, this.manager.module).hasUnlocked(this))
                {
                    user.sendTranslated("&aAs you memorize the pass-phrase the magic aura protecting this allows you to interact");
                }
                else
                {
                    user.sendTranslated("&aYou sense that the strong magic aura protecting this wont let you through without the right pass-phrase");
                }
            }
            else
            {
                user.sendTranslated("&aYou sense a strong magic aura protecting this");
            }
            if (access != null)
            {

                if (access.canIn() && access.canOut())
                {
                    if (this.getProtectedType() == ProtectedType.CONTAINER
                        || this.getProtectedType() == ProtectedType.ENTITY_CONTAINER
                        || this.getProtectedType() == ProtectedType.ENTITY_CONTAINER_LIVING)
                    {
                        user.sendTranslated("&abut is does not hinder you to put/take items in/out");
                    }
                    else
                    {
                        user.sendTranslated("&abut it lets you interact as if you were not there");
                    }
                }
                // TODO other access-level combinations
            }
        }
    }

    public void unlock(User user, Location soundLoc, String pass)
    {
        if (this.hasPass())
        {
            if (this.manager.checkPass(this, pass))
            {
                user.sendTranslated("&aUpon hearing the right pass-phrase the magic surrounding the container gets thinner and lets you pass!");
                user.playSound(soundLoc, Sound.PISTON_EXTEND, 1, 2);
                user.playSound(soundLoc, Sound.PISTON_EXTEND, 1, (float)1.5);
                user.attachOrGet(GuardAttachment.class, this.manager.module).addUnlock(this);
            }
            else
            {
                user.sendTranslated("&eSudden pain makes you realize this was not the right pass-phrase!");
                user.damage(0);
            }
        }
        else
        {
            user.sendTranslated("&eYou try to open the container with a pass-phrase but nothing changes!");
        }
    }

    /**
     * If this guard protects a double-door this will open/close the second door too.
     * Also this will schedule auto-closing the door according to the configuration
     *
     * @param user
     */
    public void doorUse(User user, Location doorClicked)
    {
        Block block = doorClicked.getBlock();
        if (block.getType() == Material.IRON_DOOR_BLOCK && !this.manager.module.getConfig().openIronDoorWithClick)
        {
            user.sendTranslated("&eYou cannot open the heavy door!");
            return;
        }
        if (block.getState().getData() instanceof Door)
        {
            Door door;
            if (((Door)block.getState().getData()).isTopHalf())
            {
                block = block.getRelative(BlockFace.DOWN);
                door = (Door)block.getState().getData();
            }
            else
            {
                door = (Door)block.getState().getData();
            }
            Sound sound;
            if (door.isOpen())
            {
                sound = Sound.DOOR_CLOSE;
            }
            else
            {
                sound = Sound.DOOR_OPEN;
            }
            Door door2 = null;
            Location loc2 = null;
            for (Location location : locations)
            {
                if (location.getBlockY() == block.getY() && !location.equals(block.getLocation(doorClicked)))
                {
                    door2 = (Door)location.getBlock().getState().getData();
                    loc2 = location;
                    break;
                }
            }
            if (door2 == null)
            {
                if (door.getItemType() == Material.IRON_DOOR_BLOCK)
                {
                    doorClicked.getWorld().playSound(doorClicked, sound, 1, 1);
                    door.setOpen(!door.isOpen());
                    block.setData(door.getData());
                }
                if (taskId != null) this.manager.module.getCore().getTaskManager().cancelTask(this.manager.module, taskId);
                if (sound == Sound.DOOR_OPEN) this.scheduleAutoClose(door, block.getState(), null, null);
                return;
            }
            boolean old = door.isOpen();
            door2.setOpen(!door.isOpen()); // Flip
            if (old != door2.isOpen())
            {

                doorClicked.getWorld().playSound(loc2, sound, 1, 1);
                loc2.getBlock().setData(door2.getData());
                if (door.getItemType() == Material.IRON_DOOR_BLOCK)
                {
                    doorClicked.getWorld().playSound(doorClicked, sound, 1, 1);
                    door.setOpen(door2.isOpen());
                    block.setData(door.getData());
                }
            }
            if (taskId != null) this.manager.module.getCore().getTaskManager().cancelTask(this.manager.module, taskId);
            if (sound == Sound.DOOR_OPEN) this.scheduleAutoClose(door, block.getState(), door2, loc2.getBlock().getState());
        }
    }

    private void scheduleAutoClose(final Door door1, final BlockState state1, final Door door2, final BlockState state2)
    {
        if (!this.manager.module.getConfig().autoCloseEnable) return;
        Runnable run;
        if (door2 == null)
        {
            run = new Runnable()
            {
                @Override
                public void run()
                {
                    door1.setOpen(false);
                    state1.setData(door1);
                    if (state1.update())
                    {
                        Location location = state1.getLocation();
                        location.getWorld().playSound(location, Sound.DOOR_CLOSE, 1, 1);
                    }
                }
            };
        }
        else
        {
            run = new Runnable()
            {
                @Override
                public void run()
                {
                    door1.setOpen(false);
                    state1.setData(door1);
                    if (state1.update())
                    {
                        Location location = state1.getLocation();
                        location.getWorld().playSound(location, Sound.DOOR_CLOSE, 1, 1);
                    }
                    door2.setOpen(false);
                    state2.setData(door2);
                    if (state2.update())
                    {
                        Location location = state2.getLocation();
                        location.getWorld().playSound(location, Sound.DOOR_CLOSE, 1, 1);
                    }
                }
            };
        }
        taskId = this.manager.module.getCore().getTaskManager()
                            .runTaskDelayed(this.manager.module, run, this.manager.module.getConfig().autoCloseSeconds * 20);
    }

    /**
     * Always true for EntityGuards
     */
    private boolean isValidType = true;

    public void validateTypeAt(Location location)
    {
        if (ProtectedType.getProtectedType(location.getBlock().getType()) == this.getProtectedType())
        {
            this.isValidType = true;
        }
        else
        {
            this.manager.module.getLog().warn("ProtectedTypes do not match for Guard at {}" ,location.toString());
        }
    }

    public boolean isValidType()
    {
        return isValidType;
    }
}