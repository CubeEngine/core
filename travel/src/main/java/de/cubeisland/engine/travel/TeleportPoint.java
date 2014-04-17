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
package de.cubeisland.engine.travel;

import java.util.Set;

import org.bukkit.Location;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.storage.TeleportPointModel;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;

public abstract class TeleportPoint
{
    protected final TeleportPointModel model;
    protected final Travel module;
    protected final InviteManager iManager;

    protected Permission permission;
    protected Set<UInteger> invited;

    protected String ownerName = null;

    public TeleportPoint(TeleportPointModel model, Travel module)
    {
        this.model = model;
        this.iManager = module.getInviteManager();
        this.module = module;
    }

    public void update()
    {
        model.update();
    }

    public Location getLocation()
    {
        Location location = model.getLocation();
        if (location.getWorld() == null)
        {
            this.module.getLog().warn("Tried to get location from TeleportPoint in deleted world!");
            return null;
        }
        return location;
    }

    public void setLocation(Location location)
    {
        model.setLocation(location);
    }

    public User getOwner()
    {
        return this.module.getCore().getUserManager().getUser(model.getOwnerKey());
    }

    public void setOwner(User owner)
    {
        this.model.setOwnerKey(owner.getEntity().getKey());
    }

    public boolean isOwner(CommandSender user)
    {
        if (user instanceof User)
        {
            return model.getOwnerKey().equals(((User)user).getEntity().getKey());
        }
        return false;
    }

    public void invite(User user)
    {
        if (this.invited == null)
        {
            this.invited = iManager.getInvited(model);
        }
        this.invited.add(user.getEntity().getKey());
        iManager.invite(this.getModel(), user);
    }

    public void unInvite(User user)
    {
        if (this.invited == null)
        {
            this.invited = iManager.getInvited(model);
        }
        this.invited.remove(user.getEntity().getKey());
        iManager.updateInvited(this.model, this.invited);
    }

    public boolean isInvited(User user)
    {
        return this.getInvited().contains(user.getEntity().getKey()) || this.isPublic();
    }

    public void setVisibility(short visibility)
    {
        model.setVisibility(visibility);
    }

    public short getVisibility()
    {
        return model.getVisibility();
    }

    public Set<UInteger> getInvited()
    {
        if (this.invited == null)
        {
            this.invited = iManager.getInvited(model);
        }
        return this.invited;
    }

    public TeleportPointModel getModel()
    {
        return model;
    }

    public String getOwnerName()
    {
        if (this.ownerName == null)
        {
            this.ownerName = this.module.getCore().getUserManager().getUserName(model.getOwnerKey());
        }
        return this.ownerName;
    }

    public String getName()
    {
        return model.getName();
    }

    public void setName(String name)
    {
        model.setName(name);
    }

    public String getWelcomeMsg()
    {
        if (model.getWelcomemsg() == null || model.getWelcomemsg().isEmpty())
        {
            return null;
        }
        return model.getWelcomemsg();
    }

    public void setWelcomeMsg(String welcomeMsg)
    {
        model.setWelcomemsg(welcomeMsg);
    }

    public boolean isPublic()
    {
        return this.getVisibility() == VISIBILITY_PUBLIC;
    }

    public boolean canAccess(User user)
    {
        return this.isPublic() ? this.permission.isAuthorized(user) : (this.isInvited(user) || this.isOwner(user));
    }

    protected abstract Permission generatePublicPerm();
}
