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
package de.cubeisland.cubeengine.travel.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.user.User;

import de.cubeisland.cubeengine.travel.Travel;
import org.bukkit.Location;

import java.util.Locale;
import java.util.Set;

public class Warp
{
    private final TeleportPoint parent;
    private final TelePointManager telePointManager;
    private final InviteManager inviteManager;
    private final Permission permission;
    private Set<String> invited;

    public Warp(TeleportPoint teleportPoint, TelePointManager telePointManager, InviteManager inviteManager, Travel module)
    {
        this.parent = teleportPoint;
        this.telePointManager = telePointManager;
        this.inviteManager = inviteManager;
        this.permission = module.getBasePermission().
                createAbstractChild("warps").
                createAbstractChild("access").
                createChild(parent.name.toLowerCase(Locale.ENGLISH), this.parent.visibility
                        .equals(TeleportPoint.Visibility.PRIVATE) ? PermDefault.OP : PermDefault.TRUE);
        module.getCore().getPermissionManager().registerPermission(module, this.permission);
    }

    /**
     * Updates the variables in the parent entity to reflect the variables in the home
     */
    public void update()
    {
        parent.ownerKey = parent.getOwner().getId();
        parent.ownerName = parent.getOwner().getName();
        parent.owner = null;
        parent.x = parent.location.getX();
        parent.y = parent.location.getY();
        parent.z = parent.location.getZ();
        parent.pitch = parent.location.getPitch();
        parent.yaw = parent.location.getYaw();
        parent.worldKey = CubeEngine.getCore().getWorldManager().getWorldId(parent.location.getWorld());
        parent.typeId = parent.type.ordinal();
        parent.visibilityId = parent.visibility.ordinal();
        telePointManager.update(parent);
    }

    public Location getLocation()
    {
        return parent.getLocation();
    }

    public void setLocation(Location location)
    {
        parent.x = location.getX();
        parent.y = location.getY();
        parent.z = location.getZ();
        parent.pitch = location.getPitch();
        parent.yaw = location.getYaw();
        parent.worldKey = CubeEngine.getCore().getWorldManager().getWorldId(location.getWorld());
        parent.location = null;
    }

    public User getOwner()
    {
        return parent.getOwner();
    }

    public void setOwner(User owner)
    {
        parent.ownerKey = owner.getId();
        parent.ownerName = owner.getName();
        parent.owner = null;
    }

    public boolean isOwner(User user)
    {
        return parent.getOwner().equals(user);
    }

    public void invite(User user)
    {
        if (this.invited == null)
        {
            this.invited = inviteManager.getInvited(parent);
        }
        this.invited.add(user.getName());
        telePointManager.putWarpToUser(this, user);
        inviteManager.store(new TeleportInvite(parent.key, user.getId()));
    }

    public void unInvite(User user)
    {
        if (this.invited == null)
        {
            this.invited = inviteManager.getInvited(parent);
        }
        this.invited.remove(user.getName());
        telePointManager.removeWarpFromUser(this, user);
        inviteManager.updateInvited(parent, this.invited);
    }

    public boolean isInvited(User user)
    {
        return this.getInvited().contains(user.getName()) || this.isPublic();
    }

    public TeleportPoint.Visibility getVisibility()
    {
        return parent.visibility;
    }

    public void setVisibility(TeleportPoint.Visibility visibility)
    {
        parent.visibility = visibility;
        parent.visibilityId = visibility.ordinal();
        telePointManager.removeWarpFromUser(this, parent.owner);
        for (String name : this.invited)
        {
            User user = CubeEngine.getUserManager().findOnlineUser(name);
            if (user != null)
            {
                telePointManager.removeWarpFromUser(this, user);
            }
        }
    }

    public String getName()
    {
        return parent.name;
    }

    public void setName(String name)
    {
        parent.name = name;
    }

    public String getWelcomeMsg()
    {
        return parent.welcomeMsg;
    }

    public void setWelcomeMsg(String welcomeMsg)
    {
        parent.welcomeMsg = welcomeMsg;
    }

    public boolean isPublic()
    {
        return this.getVisibility().equals(TeleportPoint.Visibility.PUBLIC);
    }

    public boolean isPrivate()
    {
        return this.getVisibility().equals(TeleportPoint.Visibility.PRIVATE);
    }

    public String getStorageName()
    {
        if (isPublic())
        {
            return "public:" + this.getName();
        }
        else
        {
            return parent.ownerName + ":" + this.getName();
        }
    }

    public boolean canAccess(User user)
    {
        return this.isPublic() || this.isInvited(user);
    }

    public Long getKey()
    {
        return parent.getId();
    }

    public TeleportPoint getModel()
    {
        return parent;
    }

    public Set<User> getInvitedUsers()
    {
        return inviteManager.getInvitedUsers(parent);
    }

    public Set<String> getInvited()
    {
        if (this.invited == null)
        {
            this.invited = inviteManager.getInvited(parent);
        }
        return this.invited;
    }
}
