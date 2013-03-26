package de.cubeisland.cubeengine.travel.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.travel.Travel;
import org.bukkit.Location;

import java.util.Locale;
import java.util.Set;

public class Home
{
    private final TeleportPoint parent;
    private final TelePointManager telePointManager;
    private final InviteManager inviteManager;
    private final Permission permission;
    private Set<String> invited;

    public Home(TeleportPoint teleportPoint, TelePointManager telePointManager, InviteManager inviteManager, Travel module)
    {
        this.parent = teleportPoint;
        this.telePointManager = telePointManager;
        this.inviteManager = inviteManager;
        this.invited = inviteManager.getInvited(parent);
        this.permission = module.getModulePermission().
            createAbstractChild("homes").
            createAbstractChild("access").
            createChild(parent.name.toLowerCase(Locale.ENGLISH),
                        this.parent.visibility.equals(TeleportPoint.Visibility.PRIVATE) ?
                                                                PermDefault.OP : PermDefault.TRUE);
        module.getCore().getPermissionManager().registerPermission(module, this.permission);
    }

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
        parent.x = location.getX();
        parent.y = location.getY();
        parent.z = location.getZ();
        parent.yaw = location.getYaw();
        parent.pitch = location.getPitch();
    }

    public User getOwner()
    {
        return parent.owner;
    }

    public void setOwner(User owner)
    {
        parent.owner = owner;
        parent.ownerKey = owner.getKey();
    }

    public boolean isOwner(User user)
    {
        return parent.owner.equals(user);
    }

    public void invite(User user)
    {
        this.invited.add(user.getName());
        telePointManager.putHomeToUser(this, user);
        inviteManager.invite(this.getModel(), user);
    }

    public void unInvite(User user)
    {
        this.invited.remove(user);
        telePointManager.removeHomeFromUser(this, user);
        inviteManager.updateInvited(this.parent, this.invited);
    }

    public boolean isInvited(User user)
    {
        return this.invited.contains(user.getName()) || this.isPublic();
    }

    public TeleportPoint.Visibility getVisibility()
    {
        return parent.visibility;
    }

    public void setVisibility(TeleportPoint.Visibility visibility)
    {
        parent.visibility = visibility;
        parent.visibilityId = visibility.ordinal();
        telePointManager.removeHomeFromUser(this, parent.owner);
        for (String name : this.invited)
        {
            User user = CubeEngine.getUserManager().findOnlineUser(name);
            if (user != null)
            {
                telePointManager.removeHomeFromUser(this, user);
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
        if (parent.welcomeMsg.isEmpty())
        {
            return null;
        }
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

    public boolean canAccess(User user)
    {
        return this.isInvited(user) || this.isOwner(user) ;
    }

    public String getStorageName()
    {
        return this.getOwner().getName() + ":" + this.getName();
    }

    public Set<String> getInvited()
    {
        return this.invited;
    }

    public Set<User> getInvitedUsers()
    {
        return inviteManager.getInvitedUsers(parent);
    }

    public TeleportPoint getModel()
    {
        return parent;
    }

    public Long getKey() {
        return this.parent.getKey();
    }
}
