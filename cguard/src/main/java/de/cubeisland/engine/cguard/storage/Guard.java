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

import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.InventoryGuardFactory;
import org.jooq.Result;

import static de.cubeisland.engine.cguard.storage.TableAccessList.TABLE_ACCESS_LIST;
import static de.cubeisland.engine.cguard.storage.TableGuards.*;

public class Guard
{
    private GuardManager manager;
    protected final GuardModel model;
    protected final Location location;
    protected final Location location2;

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
        this.location = null;
        this.location2 = null;
    }

    /**
     * BlockGuard
     *
     * @param manager
     * @param model
     * @param guardLoc
     */
    public Guard(GuardManager manager, GuardModel model, Result<GuardLocationModel> guardLoc)
    {
        this.manager = manager;
        this.model = model;
        this.location = this.getLocation(guardLoc.get(0));
        if (guardLoc.size() == 2)
        {
            this.location2 = this.getLocation(guardLoc.get(1));
        }
        else this.location2 = null;
    }

    public Guard(GuardManager manager, GuardModel model, GuardLocationModel loc1, GuardLocationModel loc2)
    {
        this.manager = manager;
        this.model = model;
        this.location = this.getLocation(loc1);
        this.location2 = loc2 == null ? null : this.getLocation(loc2);
    }

    private Location getLocation(GuardLocationModel model)
    {
        return new Location(this.manager.wm.getWorld(model.getWorldId().longValue()), model.getX(), model.getY(), model.getZ());
    }

    public boolean isBlockGuard()
    {
        return location != null;
    }

    public boolean isSingleBlockGuard()
    {
        return this.isBlockGuard() && location2 == null;
    }

    public Location getLocation()
    {
        return location;
    }

    public Location getLocation2()
    {
        return location2;
    }

    public void handleInventoryOpen(InventoryOpenEvent event, User user)
    {
        // TODO password protected ? check for PW-Key_book in hand
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
        AccessListModel access = this.manager.dsl.selectFrom(TABLE_ACCESS_LIST).
            where(TABLE_ACCESS_LIST.GUARD_ID.eq(this.model.getId()),
                    TABLE_ACCESS_LIST.USER_ID.eq(user.getEntity().getKey())).fetchOne();
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
            InventoryGuardFactory inventoryGuardFactory = InventoryGuardFactory.prepareInventory(event.getInventory(), user);
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

    public void delete(User user)
    {
        this.manager.delete(this);
        user.sendTranslated("&aRemoved Guard!");
    }

    private boolean checkFlag()
    {
        return false; // TODO
    }


}


