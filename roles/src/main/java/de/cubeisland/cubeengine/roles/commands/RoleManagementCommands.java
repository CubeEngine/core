package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.exception.CircularRoleDepedencyException;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.provider.RoleProvider;
import de.cubeisland.cubeengine.roles.provider.WorldRoleProvider;
import de.cubeisland.cubeengine.roles.config.Priority;
import org.bukkit.World;

public class RoleManagementCommands extends RoleCommandHelper
{
    public RoleManagementCommands(Roles module)
    {
        super(module);
        this.registerAlias(new String[]{"manrole"},new String[]{});
    }

    @Command(names = {
        "setperm", "setpermission"
    }, desc = "Sets the permission for given role [in world]", usage = "<[g:]role> <permission> <true|false|reset> [in <world>]", params = @Param(names = "in", type = World.class), max = 4, min = 3)
    public void setpermission(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        String permission = context.getString(1);
        Boolean set;
        String setTo = context.getString(2);
        if (setTo.equalsIgnoreCase("true"))
        {
            set = true;
            if (global)
            {
                context.sendTranslated("&6%s &ais now set to &2true &afor the global role &6%s&a!",
                                       permission, role.getName());
            }
            else
            {
                context.sendTranslated("&6%s &ais now set to &2true &afor the role &6%s &ain &6%s&a!",
                                       permission, role.getName(), world.getName());
            }
        }
        else if (setTo.equalsIgnoreCase("false"))
        {
            set = false;
            if (global)
            {
                context.sendTranslated("&6%s &cis now set to &4false &cfor the global role &6%s&c!",
                                       permission, role.getName());
            }
            else
            {
                context.sendTranslated("&6%s &cis now set to &4false &cfor the role &6%s &cin &6%s&c!",
                                       permission, role.getName(), world.getName());
            }
        }
        else if (setTo.equalsIgnoreCase("reset"))
        {
            set = null;
            if (global)
            {
                context.sendTranslated("&6%s &eis now resetted for the global role &6%s&e!",
                                       permission, role.getName());
            }
            else
            {
                context.sendTranslated("&6%s &eis now resetted for the role &6%s &ein &6%s&e!",
                                       permission, role.getName(), world.getName());
            }
        }
        else
        {
            context.sendTranslated("&cUnkown setting: &6%s &cUse &6true&c,&6false&c or &6reset&c!", setTo);
            return;
        }
        provider.setRolePermission(role, permission, set);
    }

    @Command(names = {
        "setdata", "setmeta", "setmetadata"
    }, desc = "Sets the metadata for given role [in world]", usage = "<[g:]role> <key> [value] [in <world>]", params = @Param(names = "in", type = World.class), max = 4, min = 3)
    public void setmetadata(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        String key = context.getString(1);
        String value = context.getString(2);
        provider.setRoleMetaData(role, key, value);
        if (value == null)
        {
            if (global)
            {
                context.sendTranslated("&eMetadata &6%s &eresetted for the global role &6%s&e!", key, role.getName());
            }
            else
            {
                context.sendTranslated("&eMetadata &6%s &eresetted for the role &6%s &ein &6%s&e!",
                                       key, role.getName(), world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendTranslated("&aMetadata &6%s &aset to &6%s &afor the global role &6%s&a!",
                                       key, value, role.getName());
            }
            else
            {
                context.sendTranslated("&aMetadata &6%s &aset to &6%s &afor the role &6%s &ain &6%s&a!",
                                       key, value, role.getName(), world.getName());
            }
        }
    }

    @Command(names = {
        "resetdata", "resetmeta", "resetmetadata"
    }, desc = "Resets the metadata for given role [in world]", usage = "<[g:]role> <key> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 2)
    public void resetmetadata(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        String key = context.getString(1);
        provider.resetRoleMetaData(role, key);
        if (global)
        {
            context.sendTranslated("&eMetadata &6%s &eresetted for the global role &6%s&e!",
                                   key, role.getName());
        }
        else
        {
            context.sendTranslated("&eMetadata &6%s &eresetted for the role &6%s &ein &6%s&e!",
                                   key, role.getName(), world.getName());
        }
    }

    @Command(names = {
        "cleardata", "clearmeta", "clearmetadata"
    }, desc = "Clears the metadata for given role [in world]", usage = "<[g:]role> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void clearmetadata(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        provider.clearRoleMetaData(role);
        if (global)
        {
            context.sendTranslated("&eMetadata cleared for the global role &6%s&e!", role.getName());
        }
        else
        {
            context.sendTranslated("&eMetadata cleared for the role &6%s &ein &6%s&e!",
                                   role.getName(), world.getName());
        }
    }

    @Command(desc = "Adds a parent role to given role [in world]", usage = "<[g:]role> <[g:]parentrole> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 2)
    public void addParent(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        Role pRole = provider.getRole(context.getString(1));
        try
        {
            if (pRole == null)
            {
                if (global)
                {
                    context.sendTranslated("&eCould not find the global parent-role &6%s&e.", context.getString(1));
                }
                else
                {
                    context.sendTranslated("&eCould not find the parent-role &6%s &ein &6%s&e.",
                                           context.getString(1), world.getName());
                }
            }
            else if (provider.setParentRole(role, pRole))
            {
                if (global)
                {
                    if (pRole.isGlobal())
                    {
                        context.sendTranslated("&&%s &cis a global role and cannot inherit from a non-global role!", role.getName());
                        return;
                    }
                    context.sendTranslated("&aAdded &6%s &aas parent-role for the global role &6%s&a!", pRole.getName(), role.getName());
                }
                else
                {
                    context.sendTranslated("&aAdded &6%s &aas parent-role for the role &6%s &ain &6%s&a!",
                                           pRole.getName(), role.getName(), world.getName());
                }
            }
            else
            {
                if (global)
                {
                    context.sendTranslated("&6%s &eis already parent-role of the global role &6%s&e!",
                                           pRole.getName(), role.getName());
                }
                else
                {
                    context.sendTranslated("&6%s &eis already parent-role of the role &6%s &ain &6%s&e!",
                                           pRole.getName(), role.getName(), world.getName());
                }
            }
        }
        catch (CircularRoleDepedencyException ex)
        {
            context.sendTranslated("&cCircular Dependency! &6%s &cdepends on &6%s&c!", pRole.getName(), role.getName());
        }
    }

    @Command(desc = "Removes a parent role from given role [in world]", usage = "<[g:]role> <[g:]parentrole> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 2)
    public void removeParent(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        Role pRole = provider.getRole(context.getString(1));
        if (pRole == null)
        {
            if (global)
            {
                context.sendTranslated("&eCould not find the global parent-role &6%s&e.", context.getString(1));
            }
            else
            {
                context.sendTranslated("&eCould not find the parent-role &6%s &ein &6%s&e.", context.getString(1), world.getName());
            }
        }
        else if (provider.removeParentRole(role, pRole))
        {
            if (global)
            {
                context.sendTranslated("&aRemoved the parent-role &6%s &afrom the global role &6%s&a!",
                                       pRole.getName(), role.getName());
            }
            else
            {
                context.sendTranslated("&aRemoved the parent-role &6%s &afrom tje role &6%s &ain &6%s&a!",
                                       pRole.getName(), role.getName(), world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendTranslated("&6%s &eis not a parent-role of the global role &6%s&e!",
                                       pRole.getName(), role.getName());
            }
            else
            {
                context.sendTranslated("&6%s &eis not a parent-role of the role &6%s &ein &6%s&e!",
                                       pRole.getName(), role.getName(), world.getName());
            }
        }
    }

    @Command(desc = "Removes all parent roles from given role [in world]", usage = "<[g:]role> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void clearParent(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        provider.clearParentRoles(role);
        if (global)
        {
            context.sendTranslated("&eAll parent-roles of the global role &6%s &ecleared!",
                                   role.getName());
        }
        else
        {
            context.sendTranslated("&eAll parent-roles of the role &6%s &ein &6%s cleared!",
                                   role.getName(), world.getName());
        }
    }

    @Command(names = {
        "setprio", "setPriority"
    }, desc = "Sets the priority of given role [in world]", usage = "<[g:]role> <priority> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 2)
    public void setPriority(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        Converter<Priority> converter = Convert.matchConverter(Priority.class);
        Priority priority;
        try
        {
            priority = converter.fromNode(new StringNode(context.getString(1)));
            provider.setRolePriority(role, priority);
            if (global)
            {
                context.sendTranslated("&aPriority of the global role &6%s &aset to &6%s&a!",
                                       role.getName(), context.getString(1));
            }
            else
            {
                context.sendTranslated("&aPriority of the role &6%s &aset to &6%s &ain &6%s&a!",
                                       role.getName(), context.getString(1), world.getName());
            }
        }
        catch (ConversionException ex)
        {
            context.sendTranslated("&6%s &cis not a valid priority!", context.getString(1));
        }

    }

    @Command(desc = "Renames given role [in world]", usage = "<[g:]role> <new name> [in <world>]|[-global]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void rename(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        String newName = context.getString(1);
        if (role.getName().equalsIgnoreCase(newName))
        {
            context.sendTranslated("&cThese are the same names!");
        }
        else if (provider.renameRole(role, newName))
        {
            if (global)
            {
                context.sendTranslated("&aGlobal role &6%s &arenamed to &6%s&a!",
                                       role.getName(), newName);
            }
            else
            {
                context.sendTranslated("&6%s &arenamed to &6%s &ain &6%s&a!",
                                       role.getName(), newName, world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendTranslated("&cRenaming failed! The role global &6%s &calready exists!", newName);
            }
            else
            {
                context.sendTranslated("&cRenaming failed! The role &6%s &calready exists in &6%s&c!",
                                       newName, world.getName());
            }
        }
    }

    @Command(desc = "Creates a new role [in world]", usage = "<rolename> [in <world>]|[-global]", params = @Param(names = "in", type = World.class), flags = @Flag(longName = "global", name = "g"), max = 2, min = 1)
    public void create(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        World world = null;
        if (this.manager.createRole(roleName, context.hasFlag("g") ? null : (world = this.getWorld(context))))
        {
            if (world == null)
            {
                context.sendTranslated("&aGlobal role created!");
            }
            else
            {
                context.sendTranslated("&aRole created!");
            }
        }
        else
        {
            if (world == null)
            {
                context.sendTranslated("&eThere is already a global role named &6%s&e.", roleName, world);
            }
            else
            {
                context.sendTranslated("&eThere is already a role named &6%s &ein &6%s&e.", roleName, world.getName());
            }
        }
    }

    @Command(desc = "Deletes a role [in world]", usage = "<[g:]rolename> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void delete(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        provider.deleteRole(role);
        if (global)
        {
            context.sendTranslated("&aGlobal role &6%s &adeleted!", role.getName());
        }
        else
        {
            context.sendTranslated("&aDeleted the role &6%s &ain &6%s&a!", role.getName(), world.getName());
        }
    }

    @Command(names = {
        "toggledefault", "toggledef", "toggledefaultrole"
    }, desc = "Toggles whether given role is a default-role [in world]", usage = "<rolename> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void toggleDefaultRole(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        World world = this.getWorld(context);
        WorldRoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (provider.toggleDefaultRole(role))
        {
            context.sendTranslated("&6%s &ais now a default-role in &6%s&a!", role.getName(), world.getName());
        }
        else
        {
            context.sendTranslated("&6%s &ais no longer a default-role in &6%s&a!", role.getName(), world.getName());
        }
    }
}
