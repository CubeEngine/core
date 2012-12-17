package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
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
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.World;

public class UserManagementCommands extends ContainerCommand
{
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
            paramNotFound(context, "roles",  "&cUser %s not found!", context.getString(pos));
        }
        if (!user.isOnline())
        {
            paramNotFound(context, "roles", "&2%s &cis not online!", user.getName());
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
    @Command(names={"checkpermission","checkperm"},
             desc = "Checks for permissions of a user [in world]",
             usage = "<permission> [player] [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 1)
    public void checkpermission(CommandContext context)
    {
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.getRole(user, worldId);
        String permission = context.getString(0);
        //context.sendMessage("roles", "&eCould not find the permission &6%s for &2%s&e!", context.getString(0), user.getName());
        RolePermission myPerm = role.getPerms().get(permission); // should never be null
        if (myPerm == null)
        {
            context.sendMessage("roles", "&cThe specified permission does not exist!");
            return;
        }
        boolean superPerm = user.hasPermission(permission);
        context.sendMessage("roles", (myPerm.isSet() ? "&aThe player &2%s &adoes have access to &f\"&6%s&f\"&a"
                : "&cThe player &2%s &cdoes not have access to &f\"&6%s&f\"&c") + " in &6%s", user.getName(), permission, world.getName());
        if (!permission.endsWith("*"))
        {
            context.sendMessage("roles", "&eSuperPerm Node: %s", superPerm); // Do not show when * permission as it would never be correct
        }
        Role originRole = myPerm.getOrigin();
        if (!originRole.getLitaralPerms().containsKey(permission))
        {
            boolean found = false;
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
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
            {
                throw new IllegalStateException("Found permission not found in literal permissions");
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
    @Command(names={"listpermission","listperm"},
             desc = "List permission of a user [in world]",
             usage = "[player] [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2)
    public void listpermission(CommandContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.getRole(user, worldId);
        context.sendMessage("roles", "&ePermissions of &2%s&e in &6%s&e.", user.getName(), world.getName());
        for (Entry<String, Boolean> entry : role.getLitaralPerms().entrySet())
        {
            context.sendMessage("- &e" + entry.getKey() + ": &6" + entry.getValue());
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

    @Command(names={"setpermission","setperm"},
             desc = "Sets a permission for this user [in world]",
             usage = "<permission> <true|false|reset> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 5, min = 3)
    public void setpermission(CommandContext context)
    {
        String perm = context.getString(0);
        User user = context.getUser(2);
        if (user == null)
        {
            context.sendMessage("roles", "&cUser %s not found!", context.getString(2));
            return;
        }
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
            //TODO msg define true|false|reset
            return;
        }
        World world = this.getWorld(context, user);
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        UserPermissionsManager upManager = ((Roles) this.getModule()).getDbUserPerm();
        if (set == null)
        {
            upManager.deleteByKey(new Triplet<Integer, Integer, String>(user.key, worldId, perm));
            context.sendMessage("roles", "&ePermission &6%s &eof &2%s&e resetted!", perm, user.getName());
        }
        else
        {
            UserPermission up = new UserPermission(user.key, worldId, perm, set);
            upManager.merge(up);
            if (set)
            {
                context.sendMessage("roles", "&aPermission &6%s &aof &2%s&a set to true!", perm, user.getName());
            }
            else
            {
                context.sendMessage("roles", "&cPermission &6%s &cof &2%s&c set tp false!", perm, user.getName());
            }
        }
        ((Roles) this.getModule()).getManager().reloadAndApplyRole(user, worldId);
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
        context.sendMessage("roles", "&aMetadata &6%s &aof &2%s&a set to &6%s &ain &6%s&a!", metaKey, user.getName(), metaVal, world.getName());
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
        context.sendMessage("roles", "&eMetadata &6%s &eof &2%s &eremoved in &6%s&e!", metaKey, user.getName(), world.getName());
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
            paramNotFound(context, "roles", "&cWorld %s not found!", context.getString("in"));
        }
        return world;
    }
}
