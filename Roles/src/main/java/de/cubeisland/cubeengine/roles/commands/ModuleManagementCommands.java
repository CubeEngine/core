package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;

public class ModuleManagementCommands extends ContainerCommand
{
    public ModuleManagementCommands(Roles module)
    {
        super(module, "admin", "Manages the module.");
    }

    @Command(desc = "Reloads all roles from config")
    public void reload(CommandContext context)
    {
        Roles module = (Roles) this.getModule();
        module.getConfiguration().load(); // reloads main config
        module.getManager().init(); // reloads all roleconfigs
        for (User user : module.getUserManager().getOnlineUsers())
        {
            user.clearAttributes(this.getModule()); // clear old attributes
            module.getManager().preCalculateRoles(user.getName());
            module.getManager().applyRole(user.getPlayer(), module.getCore().getWorldManager().getWorldId(user.getWorld()));
        }
    }

    @Command(desc = "Overrides all configs with current settings")
    public void save(CommandContext context)
    {
        // database is up to date so only saving configs
        Roles module = (Roles) this.getModule();
        module.getConfiguration().save();
        module.getManager().saveAllConfigs();
    }
    public static Integer curWorldIdOfConsole = null;

    @Command(desc = "Sets or resets the current default world", usage = "[world]", max = 1)
    public void defaultworld(CommandContext context)
    {
        Integer worldId = null;
        if (context.hasIndexed(0))
        {
            worldId = this.getModule().getCore().getWorldManager().getWorldId(context.getString(0));
            if (worldId == null)
            {
                context.sendMessage("roles", "&cInvalid world! No world &6%s &cfound", context.getString(0));
                return;
            }
            context.sendMessage("roles", "&aAll roles commands will now have the world &6%s &aas default!", context.getString(0));
        }
        else
        {
            context.sendMessage("roles", "&eCurrent world for roles resetted!");
        }
        User user = context.getSenderAsUser();
        if (user == null)
        {
            if (context.hasIndexed(0))
            {
                curWorldIdOfConsole = worldId;
            }
            else
            {
                curWorldIdOfConsole = null;
            }
        }
        else
        {
            if (worldId == null)
            {
                user.removeAttribute(this.getModule(), "curWorldId");
            }
            else
            {
                user.setAttribute(this.getModule(), "curWorldId", worldId);
            }
        }
    }
}
