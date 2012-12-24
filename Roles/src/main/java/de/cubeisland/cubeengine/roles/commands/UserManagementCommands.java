package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;
import de.cubeisland.cubeengine.roles.role.RolePermission;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.World;

public class UserManagementCommands extends ContainerCommand
{
    private RoleManager manager;

    public UserManagementCommands(Roles module)
    {
        super(module, "user", "Manage users.");//TODO alias manuser
        this.manager = module.getManager();
    }

    private User getUser(CommandContext context, int pos)
    {
        User user;
        if (context.hasIndexed(pos))
        {
            user = context.getUser(pos);
        }
        else
        {
            user = context.getSenderAsUser("roles", "&cYou have to specify a player.");
        }
        if (user == null)
        {
            paramNotFound(context, "roles", "&cUser %s not found!", context.getString(pos));
        }
        return user;
    }

    private UserSpecificRole getUserRole(User user, World world)
    {
        long worldId = this.getWorldId(world);
        if (user == null)
        {
            return null;
        }
        TLongObjectHashMap<UserSpecificRole> roleContainer = user.getAttribute(this.getModule(), "roleContainer");
        if (roleContainer == null)
        {
            if (user.isOnline())
            {
                throw new IllegalStateException("User has no rolecontainer!");
            }
            else
            {
                this.manager.preCalculateRoles(user.getName(), true);
                roleContainer = user.getAttribute(this.getModule(), "roleContainer");
            }
        }
        return roleContainer.get(worldId);
    }

    private long getWorldId(World world)
    {
        return this.getModule().getCore().getWorldManager().getWorldId(world);
    }

    /**
     * Returns the world defined with named param "in" or the users world
     *
     * @param context
     * @param user
     * @return
     */
    private World getWorld(CommandContext context, User user)
    {
        //TODO consider setting for default world always show which world is set
        World world = context.hasNamed("in") ? context.getNamed("in", World.class) : user.getWorld();
        if (world == null)
        {
            paramNotFound(context, "roles", "&cWorld %s not found!", context.getString("in"));
        }
        return world;
    }

    @Alias(names = "listuroles")
    @Command(desc = "Lists roles of a user [in world]",
             usage = "[player] [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2)
    public void list(CommandContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context, user);
        Role role = this.getUserRole(user, world);
        // List all assigned roles
        Collection<Role> roles = role.getParentRoles();
        context.sendMessage("roles", "&eRoles of &2%s&e in &6%s&e:", user.getName(), world.getName());
        for (Role pRole : roles)
        {
            if (pRole.isGlobal())
            {
                context.sendMessage("&6global&e: " + pRole.getName());
            }
            else
            {
                context.sendMessage("&6" + world.getName() + "&e: " + pRole.getName());
            }
        }
    }

    @Alias(names = "checkuperm")
    @Command(names =
    {
        "checkperm", "checkpermission"
    },
             desc = "Checks for permissions of a user [in world]",
             usage = "<permission> [player] [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 1)
    public void checkpermission(CommandContext context)
    {
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        // Search for permission
        String permission = context.getString(0);
        RolePermission myPerm = role.getPerms().get(permission);
        if (myPerm == null)
        {
            context.sendMessage("roles", "&cCould not find the specified permission!");
            return;
        }
        context.sendMessage("roles", (myPerm.isSet()
                ? "&aThe player &2%s &adoes have access to &f\"&6%s&f\"&a"
                : "&cThe player &2%s &cdoes not have access to &f\"&6%s&f\"&c")
                + " in &6%s", user.getName(), permission, world.getName());
        if (user.isOnline() && !permission.endsWith("*")) // Can have superperm
        {
            boolean superPerm = user.hasPermission(permission);
            context.sendMessage("roles", "&eSuperPerm Node: %s", superPerm);
        }
        // Display origin
        Role originRole = myPerm.getOrigin();
        if (!originRole.getLitaralPerms().containsKey(permission))
        {
            while (!permission.equals("*"))
            {
                if (permission.endsWith("*"))
                {
                    permission = permission.substring(0, permission.lastIndexOf("."));
                }
                permission = permission.substring(0, permission.lastIndexOf(".") + 1) + "*";

                if (originRole.getLitaralPerms().containsKey(permission))
                {
                    if (originRole.getLitaralPerms().get(permission) == myPerm.isSet())
                    {
                        break;
                    }
                }
            }
        }
        context.sendMessage("roles", "&ePermission inherited from:");
        if (user.getName().equals(originRole.getName()))
        {
            context.sendMessage("roles", "&6%s &ein the users role!", permission);
        }
        else
        {
            context.sendMessage("roles", "&6%s &ein the role &6%s&e!", permission, originRole.getName());
        }
    }

    @Alias(names = "listuperm")
    @Command(names =
    {
        "listperm", "listpermission"
    },
             desc = "List permission of a user [in world]",
             usage = "[player] [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2)
    public void listpermission(CommandContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        // List permissions
        if (role.getAllLiteralPerms().isEmpty())
        {
            context.sendMessage("roles", "&2%s &ehas no permissions set in &6%s&e.", user.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&ePermissions of &2%s&e in &6%s&e.", user.getName(), world.getName());
            for (Entry<String, Boolean> entry : role.getAllLiteralPerms().entrySet())
            {
                context.sendMessage("- &e" + entry.getKey() + ": &6" + entry.getValue());
            }
        }
    }

    @Alias(names = "checkumeta")
    @Command(names =
    {
        "checkdata", "checkmetadata"
    },
             desc = "Checks for metadata of a user [in world]",
             usage = "<metadatakey> [player] [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 1)
    public void checkmetadata(CommandContext context)
    {
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        // Check metadata
        String metaKey = context.getString(0);
        if (!role.getMetaData().containsKey(metaKey))
        {
            context.sendMessage("roles", "&6%s &is not set for &2%s &ein &6%s&e.", metaKey, user.getName(), world.getName());
            return;
        }
        RoleMetaData value = role.getMetaData().get(metaKey);
        context.sendMessage("roles", "&6%s&e: &6%s&e is set for &2%s &ein &6%s&e.", metaKey, value.getValue(), user.getName(), world.getName());
        if (value.getOrigin() != role)
        {
            context.sendMessage("roles", "&eOrigin: &&%s&e.", value.getOrigin().getName());
        }
    }

    @Alias(names = "listumeta")
    @Command(names =
    {
        "listdata", "listmetadata"
    },
             desc = "List metadata of a user [in world]",
             usage = "[player] [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2)
    public void listmetadata(CommandContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        // List all metadata
        context.sendMessage("roles", "&eMetadata of &2%s&e in &6%s&e.:", user.getName(), world.getName());
        for (Entry<String, RoleMetaData> entry : role.getMetaData().entrySet())
        {
            context.sendMessage("- &e" + entry.getKey() + ": &6" + entry.getValue().getValue());
        }
    }

    @Alias(names =
    {
        "manuadd", "assignurole", "addurole", "giveurole"
    })
    @Command(names =
    {
        "assign", "add", "give"
    },
             desc = "Assign a role to the player [in world]",
             usage = "<role> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void assign(CommandContext context)
    {
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        String roleName = context.getString(0);
        Role role = this.manager.getProvider(worldId).getRole(roleName);
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s &ein &6%s&e.", roleName, world.getName());
            return;
        }
        if (this.manager.addRoles(user, user.getPlayer(), worldId, role))
        {
            context.sendMessage("roles", "&aAdded the role &6%s&a to &2%s&a in &6%s&a.", roleName, user.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&2%s&e already had the role &6%s&e in &6%s&e.", user.getName(), roleName, world.getName());
        }
    }

    @Alias(names = "remurole")
    @Command(desc = "Removes a role from the player [in world]",
             usage = "<role> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void remove(CommandContext context)
    {
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.manager.getProvider(worldId).getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s &ein &6%s&e.", context.getString(0), world.getName());
            return;
        }
        if (this.manager.removeRole(user, role, worldId))
        {
            context.sendMessage("roles", "&aRemoved the role &6%s&a from &2%s&a in &6%s&a.", role.getName(), user.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&2%s&e did not have the role &6%s&e in &6%s&e.", user.getName(), role.getName(), world.getName());
        }
    }

    @Alias(names = "clearurole")
    @Command(desc = "Clears all roles from the player and sets the defaultroles [in world]",
             usage = "<player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void clear(CommandContext context)
    {
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Set<Role> newRoles = this.manager.clearRoles(user, worldId);
        context.sendMessage("roles", "&eCleared the roles of &2%s &ein &6%s&e.", user.getName(), world.getName());
        if (!newRoles.isEmpty())
        {
            context.sendMessage("roles", "&eDefault roles assigned:");
            for (Role role : newRoles)
            {
                context.sendMessage("- &6" + role.getName());
            }
        }
    }

    @Command(names =
    {
        "setperm", "setpermission"
    },
             desc = "Sets a permission for this user [in world]",
             usage = "<permission> <true|false|reset> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 5, min = 3)
    public void setpermission(CommandContext context)
    {
        User user = this.getUser(context, 2);
        String perm = context.getString(0);
        Boolean set;
        String setTo = context.getString(1);
        if (setTo.equalsIgnoreCase("true"))
        {
            set = true;
        }
        else if (setTo.equalsIgnoreCase("false"))
        {
            set = false;
        }
        else if (setTo.equalsIgnoreCase("reset"))
        {
            set = null;
        }
        else
        {
            context.sendMessage("roles", "&cUnkown setting: &6%s &cUse &6true&c,&6false&c or &6reset&c!", setTo);
            return;
        }
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        role.setPermission(perm, set);
        if (set == null)
        {
            context.sendMessage("roles", "&ePermission &6%s &eof &2%s&e resetted!", perm, user.getName());
        }
        else
        {
            if (set)
            {
                context.sendMessage("roles", "&aPermission &6%s &aof &2%s&a set to true!", perm, user.getName());
            }
            else
            {
                context.sendMessage("roles", "&cPermission &6%s &cof &2%s&c set to false!", perm, user.getName());
            }
        }
    }

    public void resetpermission(CommandContext context)
    {
        //TODO use this as proxy method for setPermission with reset
        //other alias givePermissions for setPermission with true
    }

    @Command(names =
    {
        "setdata", "setmeta", "setmetadata"
    },
             desc = "Sets metadata for this user [in world]",
             usage = "<metaKey> <metaValue> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 4, min = 3)
    public void setmetadata(CommandContext context)
    {
        String metaKey = context.getString(0);
        String metaVal = context.getString(1);
        User user = this.getUser(context, 2);
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        role.setMetaData(metaKey, metaVal);
        context.sendMessage("roles", "&aMetadata &6%s &aof &2%s&a set to &6%s &ain &6%s&a!", metaKey, user.getName(), metaVal, world.getName());
    }

    @Command(names =
    {
        "resetdata", "resetmeta", "resetmetadata"
    },
             desc = "Resets metadata for this user [in world]",
             usage = "<metaKey> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 4, min = 3)
    public void resetmetadata(CommandContext context)
    {
        String metaKey = context.getString(0);
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        role.setMetaData(metaKey, null);
        context.sendMessage("roles", "&eMetadata &6%s &eof &2%s &eremoved in &6%s&e!", metaKey, user.getName(), world.getName());
    }

    @Command(names =
    {
        "cleardata", "clearmeta", "clearmetadata"
    },
             desc = "Resets metadata for this user [in world]",
             usage = "<player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void clearMetaData(CommandContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        role.clearMetaData();
        context.sendMessage("roles", "&eMetadata of &2%s &ecleared in &6%s&e!", user.getName(), world.getName());
    }
}
