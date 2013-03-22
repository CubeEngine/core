package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.RolesAttachment;

public class ManagementCommands extends ContainerCommand
{
    public ManagementCommands(Roles module)
    {
        super(module, "admin", "Manages the module.");
        this.registerAlias(new String[]{"manadmin"},new String[]{});
    }

    @Alias(names = "manload")
    @Command(desc = "Reloads all roles from config")
    public void reload(CommandContext context)
    {
        Roles module = (Roles)this.getModule();
        module.getConfiguration().load(); // reloads main config
        module.getManager().init(); // reloads all roleconfigs
        for (User user : module.getUserManager().getOnlineUsers())
        {
            user.attach(RolesAttachment.class,this.getModule());
            module.getManager().preCalculateRoles(user.getName(), true);
            if (user.isOnline())
            {
                module.getManager().applyRole(user.getPlayer());
            }
        }
        context.sendTranslated("&f[&6Roles&f] &areload complete!");
    }

    @Alias(names = "mansave")
    @Command(desc = "Overrides all configs with current settings")
    public void save(CommandContext context)
    {
        // database is up to date so only saving configs
        Roles module = (Roles)this.getModule();
        module.getConfiguration().save();
        module.getManager().saveAllConfigs();
        context.sendTranslated("&f[&6Roles&f] &aall configurations saved!");
    }

    public static Long curWorldIdOfConsole = null;

    @Command(desc = "Sets or resets the current default world", usage = "[world]", max = 1)
    public void defaultworld(CommandContext context)
    {
        Long worldId = null;
        if (context.hasArg(0))
        {
            worldId = this.getModule().getCore().getWorldManager().getWorldId(context.getString(0));
            if (worldId == null)
            {
                context.sendTranslated("&cInvalid world! No world &6%s &cfound", context.getString(0));
                return;
            }
            context.sendTranslated("&aAll your roles commands will now have &6%s &aas default world!", context.getString(0));
        }
        else
        {
            context.sendTranslated("&eCurrent world for roles resetted!");
        }
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            if (worldId == null)
            {
                ((User)sender).get(RolesAttachment.class).setCurrentWorldId(null);
            }
            else
            {
                ((User)sender).get(RolesAttachment.class).setCurrentWorldId(worldId);
            }
        }
        else
        {
            if (context.hasArg(0))
            {
                curWorldIdOfConsole = worldId;
            }
            else
            {
                curWorldIdOfConsole = null;
            }
        }
    }
}
