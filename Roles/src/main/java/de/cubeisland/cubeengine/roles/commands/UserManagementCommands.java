package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.MergedRole;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RolePermission;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

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
            context.sendMessage("roles", "&cUser %s not found!", context.getString(1));
            return null;
        }
        return user;
    }

    private Role getRole(CommandContext context, User user, int worldpos)
    {
        if (user == null)
        {
            return null;
        }
        Integer worldId;
        if (context.hasIndexed(worldpos))
        {
            worldId = this.getModule().getCore().getWorldManager().getWorldId(context.getString(worldpos));
            if (worldId == null)
            {
                context.sendMessage("roles", "&cUnkown world %s!", context.getString(worldpos));
                return null;
            }
        }
        else
        {
            worldId = user.getWorldId();
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
             max = 2)
    public void list(CommandContext context)
    {
        User user = this.getUser(context, 0);
        Role role = this.getRole(context, user, 1);
        Collection<Role> roles = role.getParentRoles();
        String world = context.hasIndexed(1) ? context.getString(1) : user.getWorld().getName();
        context.sendMessage("roles", "&eRoles of &2%s&e in &6%s&e:", user.getName(), world);
        for (Role pRole : roles)
        {
            if (pRole.isGlobal())
            {
                context.sendMessage("&6global&e: " + pRole.getName());
            }
            else
            {
                context.sendMessage("&6" + world + "&e: " + pRole.getName());
            }
        }
    }

    @Alias(names = "checkuperm")
    @Command(desc = "Checks for permissions of a user [in world]",
             usage = "<permission> [player] [in <world>]",
             max = 3, min = 1)
    public void checkperm(CommandContext context)
    {
        User user = this.getUser(context, 1);
        Role role = this.getRole(context, user, 2);
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
        String world = context.hasIndexed(2) ? context.getString(2) : user.getWorld().getName();
        context.sendMessage("roles", (myPerm ? "&aThe player &2%s &adoes have access to &f\"&6%s&f\"&a"
                : "&cThe player &2%s &cdoes not have access to &f\"&6%s&f\"&c") + " in &6%s", user.getName(), permission, world);
        if (!permission.endsWith("*"))
        {
            context.sendMessage("roles", "&eSuperPerm Node: %s", superPerm); // Do not show when * permission as it would never be correct
        }
        if (!permissionsfound.isEmpty())
        {
            context.sendMessage("roles", "&ePermission inherited from:");
            for (String permFound : permissionsfound)
            {
                if (role.getPerms().get(permFound).isSet() == myPerm)
                {
                    for (Role pRole : role.getParentRoles())
                    {
                        if (pRole.getPerms().containsKey(permFound))
                        {
                            context.sendMessage("roles", "&6%s &ein the role &6%s&e!", permFound, pRole.getName());
                            return;
                        }
                    }
                    context.sendMessage("roles", "&6%s &ein the users role!", permFound);
                    return;
                }
            }
        }
    }

    @Alias(names = "listuperm")
    @Command(desc = "List permission of a user [in world]",
             usage = "[player] [in <world>]",
             max = 2)
    public void listperm(CommandContext context)
    {
        User user = this.getUser(context, 0);
        Role role = this.getRole(context, user, 1);
        String world = context.hasIndexed(1) ? context.getString(1) : user.getWorld().getName();
        context.sendMessage("roles", "&ePermissions of &2%s&e in &6%s&e.", user.getName(), world);
        for (Entry<String, RolePermission> entry : role.getPerms().entrySet())
        {
            context.sendMessage("- &e" + entry.getValue().getPerm() + ": &6" + entry.getValue().isSet());
        }
    }

    @Alias(names = "checkumeta")
    @Command(desc = "Checks for metadata of a user [in world]",
             usage = "<metadatakey> [player] [in <world>]",
             max = 3, min = 1)
    public void checkmetadata(CommandContext context)
    {
        User user = this.getUser(context, 1);
        Role role = this.getRole(context, user, 2);
        String world = context.hasIndexed(2) ? context.getString(2) : user.getWorld().getName();
        String metaKey = context.getString(0);
        if (!role.getMetaData().containsKey(metaKey))
        {
            context.sendMessage("roles", "&6%s &is not set for &2%s &ein &6%s&e.", metaKey, user.getName(), world);
            return;
        }
        String value = role.getMetaData().get(metaKey);
        context.sendMessage("roles", "&6%s&e: &6%s&e is set for &2%s &ein &6%s&e.", metaKey, value, user.getName(), world);
        //TODO show where its coming from
    }

    @Alias(names = "listumeta")
    @Command(desc = "List metadata of a user [in world]",
             usage = "[player] [in <world>]",
             max = 2)
    public void listmetadata(CommandContext context)
    {
        User user = this.getUser(context, 0);
        Role role = this.getRole(context, user, 1);
        String world = context.hasIndexed(1) ? context.getString(1) : user.getWorld().getName();
        context.sendMessage("roles", "&eMetadata of &2%s&e in world &6%s&e.:", user.getName(), world);
        for (Entry<String, String> entry : role.getMetaData().entrySet())
        {
            context.sendMessage("- &e" + entry.getKey() + ": &6" + entry.getValue());
        }
    }

    @Alias(names = {"manuadd","assignurole","addurole","giveurole"})
    @Command(names= {"assign","add","give"},
             desc = "Assign a role to the player [in world]",
             usage = "<role> <player> [in <world>]",
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
        int worldId;
        if (context.hasIndexed(2))
        {
            worldId = this.getModule().getCore().getWorldManager().getWorldId(context.getString(2));
        }
        else
        {
            worldId = user.getWorldId();
        }
        String roleName = context.getString(0);
        role = ((Roles) this.getModule()).getManager().getProvider(worldId).getRole(roleName);
        String world = context.hasIndexed(1) ? context.getString(1) : user.getWorld().getName();
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s &ein &6%s&e.", roleName, world);
            return;
        }
        if (((Roles) this.getModule()).getManager().addRole(user, role, worldId))
        {
            context.sendMessage("roles", "&aAdded the role &6%s&a to &2%s&a.", roleName, user.getName());
        }
        else
        {
            context.sendMessage("roles", "&2%s&e already had the role &6%s&e.", user.getName(), roleName);
        }
    }

    public void remove(CommandContext context)
    {
    }

    public void clear(CommandContext context)
    {
    }

    public void setpermission(CommandContext context)
    {
    }

    public void resetpermission(CommandContext context)
    {
    }

    public void setmetadata(CommandContext context)
    {
    }

    public void resetmetadata(CommandContext context)
    {
    }
}
