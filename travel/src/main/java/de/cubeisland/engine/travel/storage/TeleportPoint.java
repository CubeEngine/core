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
package de.cubeisland.engine.travel.storage;

import java.util.Set;

import org.bukkit.Location;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;

public abstract class TeleportPoint
{
    protected final TeleportPointModel parent;
    protected final Travel module;
    protected final TelePointManager telePointManager;
    protected final InviteManager inviteManager;

    protected Permission permission;
    protected Set<UInteger> invited;

    protected String ownerName = null;

    public TeleportPoint(TeleportPointModel teleportPoint, TelePointManager telePointManager, InviteManager inviteManager, Travel module)
    {
        this.parent = teleportPoint;
        this.telePointManager = telePointManager;
        this.inviteManager = inviteManager;
        this.module = module;
    }

    public void update()
    {
        parent.update();
    }

    public Location getLocation()
    {
        Location location = parent.getLocation();
        if (location.getWorld() == null)
        {
            this.module.getLog().warn("Tried to get location from TeleportPoint in deleted world!");
            return null;
        }
        return location;
    }

    public void setLocation(Location location)
    {
        parent.setLocation(location);
    }

    public User getOwner()
    {
        return this.module.getCore().getUserManager().getUser(parent.getOwnerKey());
    }

    public void setOwner(User owner)
    {
        this.parent.setOwnerKey(owner.getEntity().getKey());
    }

    public boolean isOwner(User user)
    {
        return parent.getOwnerKey().equals(user.getEntity().getKey());
    }

    public void invite(User user)
    {
        if (this.invited == null)
        {
            this.invited = inviteManager.getInvited(parent);
        }
        this.invited.add(user.getEntity().getKey());
        inviteManager.invite(this.getModel(), user);
    }

    public void unInvite(User user)
    {
        if (this.invited == null)
        {
            this.invited = inviteManager.getInvited(parent);
        }
        this.invited.remove(user.getEntity().getKey());
        inviteManager.updateInvited(this.parent, this.invited);
    }

    public boolean isInvited(User user)
    {
        return this.getInvited().contains(user.getEntity().getKey()) || this.isPublic();
    }

    public void setVisibility(short visibility)
    {
        parent.setVisibility(visibility);
    }

    public short getVisibility()
    {
        return parent.getVisibility();
    }

    public Set<UInteger> getInvited()
    {
        if (this.invited == null)
        {
            this.invited = inviteManager.getInvited(parent);
        }
        return this.invited;
    }

    public Set<User> getInvitedUsers()
    {
        return inviteManager.getInvitedUsers(parent);
    }

    public TeleportPointModel getModel()
    {
        return parent;
    }

    public Long getKey()
    {
        return this.parent.getKey().longValue();
    }

    public String getOwnerName()
    {
        if (this.ownerName == null)
        {
            this.ownerName = this.module.getCore().getUserManager().getUserName(parent.getOwnerKey());
        }
        return this.ownerName;
    }

    public String getName()
    {
        return parent.getName();
    }

    public void setName(String name)
    {
        parent.setName(name);
    }

    public String getWelcomeMsg()
    {
        if (parent.getWelcomemsg() == null || parent.getWelcomemsg().isEmpty())
        {
            return null;
        }
        return parent.getWelcomemsg();
    }

    public void setWelcomeMsg(String welcomeMsg)
    {
        parent.setWelcomemsg(welcomeMsg);
    }

    public boolean isPublic()
    {
        return this.getVisibility() == VISIBILITY_PUBLIC;
    }

    public boolean canAccess(User user)
    {
        return this.isPublic() ? this.permission.isAuthorized(user) : (this.isInvited(user) || this.isOwner(user));
    }

    public String getStorageName()
    {
        return parent.getOwnerKey().longValue() + ":" + this.getName();
    }
}
