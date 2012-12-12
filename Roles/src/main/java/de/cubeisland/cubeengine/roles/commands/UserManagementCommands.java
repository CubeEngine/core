package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.MergedRole;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;
import de.cubeisland.cubeengine.roles.role.RolePermission;
import de.cubeisland.cubeengine.roles.storage.UserMetaData;
import de.cubeisland.cubeengine.roles.storage.UserMetaDataManager;
import de.cubeisland.cubeengine.roles.storage.UserPermission;
import de.cubeisland.cubeengine.roles.storage.UserPermissionsManager;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.World;

public class UserManagementCommands extends ContainerCommand
{//TODO remove codeDuplication

    public UserManagementCommands(Roles module)
    {
        super(module, "user", "Manage users.");//TODO alias manuser
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
            context.sendMessage("roles", "&cUser %s not found!", context.getString(1));
            return null;
        }
        return user;
    }

    private Role getRole(User user, int worldId)
    {
        if (user == null)
        {
            return null;
        }
        TIntObjectHashMap<MergedRole> roleContainer = user.getAttribute(this.getModule(), "roleContainer");
        if (roleContainer == null)
        {
            throw new IllegalStateException("User has no rolecontainer!");
        }
        return roleContainer.get(worldId);
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
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.getRole(user, worldId);
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
    @Command(desc = "Checks for permissions of a user [in world]",
    usage = "<permission> [player] [in <world>]",
    params =
    @Param(names = "in", type = World.class),
    max = 3, min = 1)
    public void checkperm(CommandContext context)
    {
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.getRole(user, worldId);
        String permission = context.getString(0);
        ArrayList<String> permissionsfound = new ArrayList<String>();
        while (permission.contains("."))
        {
            if (role.getPerms().containsKey(permission))
            {
                permissionsfound.add(permission);
            }
            if (permission.endsWith("*"))
            {
                permission = permission.substring(0, permission.lastIndexOf("."));
            }
            permission = permission.substring(0, permission.lastIndexOf(".") + 1) + "*";
        }
        if (permissionsfound.isEmpty())
        {
            context.sendMessage("roles", "&eCould not find the permission &6%s for &2%s&e!", context.getString(0), user.getName());
            return;
        }
        permission = context.getString(0);
        boolean superPerm = user.hasPermission(permission);
        Boolean myPerm = role.resolvePermissions().get(permission); // should never be null
        if (myPerm == null)
        {
            context.sendMessage("roles", "&cThe specified permission does not exist!");
            return;
        }
        context.sendMessage("roles", (myPerm ? "&aThe player &2%s &adoes have access to &f\"&6%s&f\"&a"
                : "&cThe player &2%s &cdoes not have access to &f\"&6%s&f\"&c") + " in &6%s", user.getName(), permission, world.getName());
        if (!permission.endsWith("*"))
        {
            context.sendMessage("roles", "&eSuperPerm Node: %s", superPerm); // Do not show when * permission as it would never be correct
        }
        if (!permissionsfound.isEmpty())
        {
            //TODO check if this works correctly only display perms that are really set in the role not calculated perms
            //e.g. do not show ce.basics.commands.* if ce.basics.* was set
            context.sendMessage("roles", "&ePermission inherited from:");
            for (String permFound : permissionsfound)
            {
                if (role.getPerms().get(permFound).isSet() == myPerm)
                {
                    String permOrigin = role.getPerms().get(permFound).getOrigin().getName();
                    if (user.getName().equals(permOrigin))
                    {
                        context.sendMessage("roles", "&6%s &ein the users role!", permFound);
                    }
                    else
                    {
                        context.sendMessage("roles", "&6%s &ein the role &6%s&e!", permFound, permOrigin);
                    }
                    return;
                }
            }
        }
    }

    @Alias(names = "listuperm")
    @Command(desc = "List permission of a user [in world]",
    usage = "[player] [in <world>]",
    params =
    @Param(names = "in", type = World.class),
    max = 2)
    public void listperm(CommandContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.getRole(user, worldId);
        context.sendMessage("roles", "&ePermissions of &2%s&e in &6%s&e.", user.getName(), world.getName());
        for (Entry<String, RolePermission> entry : role.getPerms().entrySet())
        {
            context.sendMessage("- &e" + entry.getValue().getPerm() + ": &6" + entry.getValue().isSet());
        }
    }

    @Alias(names = "checkumeta")
    @Command(desc = "Checks for metadata of a user [in world]",
    usage = "<metadatakey> [player] [in <world>]",
    params =
    @Param(names = "in", type = World.class),
    max = 3, min = 1)
    public void checkmetadata(CommandContext context)
    {
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.getRole(user, worldId);
        String metaKey = context.getString(0);
        if (!role.getMetaData().containsKey(metaKey))
        {
            context.sendMessage("roles", "&6%s &is not set for &2%s &ein &6%s&e.", metaKey, user.getName(), world.getName());
            return;
        }
        RoleMetaData value = role.getMetaData().get(metaKey);
        context.sendMessage("roles", "&6%s&e: &6%s&e is set for &2%s &ein &6%s&e.", metaKey, value.getValue(), user.getName(), world.getName());
        context.sendMessage("roles", "&eOrigin: &&%s&e.", value.getOrigin().getName());
    }

    @Alias(names = "listumeta")
    @Command(desc = "List metadata of a user [in world]",
    usage = "[player] [in <world>]",
    params =
    @Param(names = "in", type = World.class),
    max = 2)
    public void listmetadata(CommandContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.getRole(user, worldId);
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
        Role role;
        User user = context.getUser(1);
        if (user == null)
        {
            context.sendMessage("roles", "&cUser %s not found!", context.getString(1));
            return;
        }
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        String roleName = context.getString(0);
        role = ((Roles) this.getModule()).getManager().getProvider(worldId).getRole(roleName);
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s &ein &6%s&e.", roleName, world.getName());
            return;
        }
        if (((Roles) this.getModule()).getManager().addRoles(user, worldId, role))
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
        Role role;
        User user = context.getUser(1);
        if (user == null)
        {
            context.sendMessage("roles", "&cUser %s not found!", context.getString(1));
            return;
        }
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        String roleName = context.getString(0);
        role = ((Roles) this.getModule()).getManager().getProvider(worldId).getRole(roleName);
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s &ein &6%s&e.", roleName, world.getName());
            return;
        }
        if (((Roles) this.getModule()).getManager().removeRole(user, role, worldId))
        {
            context.sendMessage("roles", "&aRemoved the role &6%s&a from &2%s&a in &6%s&a.", roleName, user.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&2%s&e did not have the role &6%s&e in &6%s&e.", user.getName(), roleName, world.getName());
        }
    }

    @Alias(names = "clearurole")
    @Command(desc = "Clears all roles from the player and sets the defaultworlds [in world]",
    usage = "<player> [in <world>]",
    params =
    @Param(names = "in", type = World.class),
    max = 2, min = 1)
    public void clear(CommandContext context)
    {
        User user = context.getUser(1);
        if (user == null)
        {
            context.sendMessage("roles", "&cUser %s not found!", context.getString(1));
            return;
        }
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        List<Role> newRoles = ((Roles) this.getModule()).getManager().clearRoles(user, worldId);
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

    @Command(desc = "Sets a permission for this user [in world]",
    usage = "<permission> <player> <true|false|reset> [in <world>]",
    params =
    @Param(names = "in", type = World.class),
    max = 5, min = 3)
    public void setpermission(CommandContext context)
    {
        String perm = context.getString(0);
        User user = context.getUser(1);
        if (user == null)
        {
            context.sendMessage("roles", "&cUser %s not found!", context.getString(1));
            return;
        }
        Boolean set;
        String setTo = context.getString(2);
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
            //TODO msg define true|false|reset
            return;
        }
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        UserPermissionsManager upManager = ((Roles) this.getModule()).getDbUserPerm();
        if (set == null)
        {
            upManager.deleteByKey(new Triplet<Integer, Integer, String>(user.key, worldId, perm));
        }
        else
        {
            UserPermission up = new UserPermission(user.key, worldId, perm, set);
            upManager.merge(up);
        }
        ((Roles) this.getModule()).getManager().reloadAndApplyRole(user, worldId);
        //TODO msg
    }

    public void resetpermission(CommandContext context)
    {
        //TODO use this as proxy method for setPermission with reset
        //other alias givePermissions for setPermission with true
    }

    @Command(desc = "Sets metadata for this user [in world]",
    usage = "<metaKey> <metaValue> <player> [in <world>]",
    params =
    @Param(names = "in", type = World.class),
    max = 4, min = 3)
    public void setmetadata(CommandContext context)
    {
        String metaKey = context.getString(0);
        String metaVal = context.getString(1);
        User user = context.getUser(2);
        if (user == null)
        {
            context.sendMessage("roles", "&cUser %s not found!", context.getString(2));
            return;
        }
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        UserMetaDataManager umManager = ((Roles) this.getModule()).getDbUserMeta();
        umManager.merge(new UserMetaData(user.key, worldId, metaKey, metaVal));
        //TODO msg
    }

    @Command(desc = "Resets metadata for this user [in world]",
    usage = "<metaKey> <player> [in <world>]",
    params =
    @Param(names = "in", type = World.class),
    max = 4, min = 3)
    public void resetmetadata(CommandContext context)
    {
        String metaKey = context.getString(0);
        User user = context.getUser(1);
        if (user == null)
        {
            context.sendMessage("roles", "&cUser %s not found!", context.getString(1));
            return;
        }
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        UserMetaDataManager umManager = ((Roles) this.getModule()).getDbUserMeta();
        umManager.deleteByKey(new Triplet<Integer, Integer, String>(user.key, worldId, metaKey));
        //TODO msg
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
        World world = context.hasNamed("in") ? context.getNamed("in", World.class) : user.getWorld();
        if (world == null)
        {
            //TODO world not found msg as exception
        }
        return world;
    }
}
