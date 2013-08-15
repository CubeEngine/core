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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.InventoryGuardFactory;
import org.jooq.Record1;
import org.jooq.Result;

import static de.cubeisland.engine.cguard.storage.AccessListModel.ACCESS_ADMIN;
import static de.cubeisland.engine.cguard.storage.TableAccessList.TABLE_ACCESS_LIST;
import static de.cubeisland.engine.cguard.storage.TableGuards.*;

public class Guard
{
    private GuardManager manager;
    protected final GuardModel model;
    protected final ArrayList<Location> locations = new ArrayList<>();

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
    }

    public Guard(GuardManager manager, GuardModel model, List<Location> locations)
    {
        this(manager, model);
        this.locations.addAll(locations);
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

    public void handleBlockDoorUse(Cancellable event, User user)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey())) return; // Its the owner
        switch (this.model.getGuardType())
        {
            case GUARDTYPE_PUBLIC: return; // Allow everything
            case GUARDTYPE_PRIVATE: // block changes
                break;
            case GUARDTYPE_GUARDED:
            case GUARDTYPE_DONATION:
            case GUARDTYPE_FREE:
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
        // TODO handle autoclose ??
        // TODO message to user that door is protected(allow to disable the message)
        user.sendTranslated("&eThis door is protected by &2%s", this.getOwner().getName());
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
        switch (this.model.getGuardType())
        {
            default: throw new IllegalStateException();
            case GUARDTYPE_PUBLIC: return; // Allow everything
            case GUARDTYPE_PRIVATE: // block changes
            case GUARDTYPE_GUARDED:
                in = false;
                out = false;
                break;
            case GUARDTYPE_DONATION:
                in = true;
                out = false;
                break;
            case GUARDTYPE_FREE:
                in = false;
                out = true;
        }
        AccessListModel access = this.getAccess(user);
        if (access == null && this.model.getGuardType() == GUARDTYPE_PRIVATE)
        {
            event.setCancelled(true); // private & no access
            // TODO perm show protection owner
            user.sendTranslated("&cA magical lock prevents you from accessing this inventory!");
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
            // TODO message to user that inventory is protected and how (allow to disable the message)
            // TODO change message for entity stuffs
            user.sendTranslated("&eThis %s is protected by &2%s", "container", this.getOwner().getName());
        }
    }

    public void handleEntityInteract(Cancellable event, Entity entity, User user)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey())) return; // Its the owner
        switch (this.model.getGuardType())
        {
            case GUARDTYPE_PUBLIC: return; // Allow everything
            case GUARDTYPE_PRIVATE:
                break;// block changes
            case GUARDTYPE_GUARDED:
            case GUARDTYPE_DONATION:
            case GUARDTYPE_FREE:
            default: throw new IllegalStateException();
        }
        AccessListModel access = this.getAccess(user);
        if (access == null && this.model.getGuardType() == GUARDTYPE_PRIVATE)
        {
            event.setCancelled(true); // private & no access
            // TODO perm show protection owner
            user.sendTranslated("&cMagic repelled your attempts to reach this entity!");
        }
        else // has access
        {
            // TODO messages
        }
    }

    public void handleBlockBreak(BlockBreakEvent event, User user)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey()))
        {
            this.delete(user);
            return;
        }
        event.setCancelled(true);
        user.sendTranslated("&cMagic prevents you from breaking this inventory!");
    }


    public void handleEntityDamage(Cancellable event, User user)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey())) return;
        AccessListModel access = this.getAccess(user);
        if (access == null && this.model.getGuardType() == GUARDTYPE_PRIVATE)
        {
            event.setCancelled(true); // private & no access
            // TODO perm show protection owner
            user.sendTranslated("&cMagic repelled your attempts to hurt this entity!");
        }
        else // has access
        {
            // TODO messages
        }
    }

    public void handleEntityDestroy(User user)
    {
        this.delete(user);
    }

    public void delete(User user)
    {
        this.manager.removeGuard(this);
        if (user != null) user.sendTranslated("&aRemoved Guard!");
    }

    private boolean checkFlag()
    {
        return false; // TODO
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

    public void notifyKeyUsage(User user)
    {
        User owner = this.manager.um.getUser(this.model.getOwnerId().longValue());
        owner.sendTranslated("&2%s&e just used a KeyBook one of your protections!", user.getName()); // TODO do not spam
    }

    public User getOwner()
    {
        return this.manager.module.getCore().getUserManager().getUser(this.model.getOwnerId().longValue());
    }

    public boolean isPublic()
    {
        return this.model.getGuardType().equals(GUARDTYPE_PUBLIC);
    }
}


