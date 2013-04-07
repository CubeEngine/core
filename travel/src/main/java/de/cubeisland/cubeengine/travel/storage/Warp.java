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
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;

import java.util.Set;

public class Warp
{
    private final TeleportPoint parent;
    private final TelePointManager telePointManager;
    private final InviteManager inviteManager;
    private Set<String> invited;

    public Warp(TeleportPoint teleportPoint, TelePointManager telePointManager, InviteManager inviteManager)
    {
        this.parent = teleportPoint;
        this.telePointManager = telePointManager;
        this.inviteManager = inviteManager;
        this.invited = inviteManager.getInvited(parent);
    }

    /**
     * Updates the variables in the parent entity to reflect the variables in the home
     */
    public void update()
    {
        parent.ownerKey = parent.owner.getKey();
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
        return parent.location;
    }

    public void setLocation(Location location)
    {
        parent.location = location;
    }

    public User getOwner()
    {
        return parent.owner;
    }

    public void setOwner(User owner)
    {
        parent.owner = owner;
    }

    public boolean isOwner(User user)
    {
        return parent.owner == user;
    }

    public void invite(User user)
    {
        this.invited.add(user.getName());
        telePointManager.putWarpToUser(this, user);
        inviteManager.store(new TeleportInvite(parent.key, user.getKey()));
    }

    public void unInvite(User user)
    {
        this.invited.remove(user);
        telePointManager.removeWarpFromUser(this, user);
        inviteManager.updateInvited(parent, this.invited);
    }

    public boolean isInvited(User user)
    {
        return this.invited.contains(user) || this.isPublic();
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
            return this.getOwner().getName() + ":" + this.getName();
        }
    }

    public boolean canAccess(User user)
    {
        return this.isPublic() || this.isInvited(user);
    }

    public Long getKey()
    {
        return parent.getKey();
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
        return this.invited;
    }
}
