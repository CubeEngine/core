package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.paramNotFound;

public class KitCommand extends ContainerCommand
{
    public KitCommand(Module module)
    {
        super(module, "kit", "Manages item-kits");
    }

    @Alias(names = "kit")
    @Command(
        desc = "Gives a kit of items.",
        usage = "<kitname> [player]",
        min = 1, max = 2,
        flags =
        {
            @Flag(longName = "all", name = "a"),
            @Flag(longName = "force", name = "f")
        })
    public void give(CommandContext context)
    {
        String kitname = context.getString(0);
        User user;
        Kit kit = KitConfiguration.getKit(kitname);
        boolean force = false;
        if (context.hasFlag("f") && BasicsPerm.COMMAND_KIT_GIVE_FORCE.isAuthorized(context.getSender()) )
        {
            force = true;
        }
        if (kit == null)
        {
            paramNotFound(context, "basics", "&cKit &6%s &cnot found!", kitname);
        }
        if (context.hasFlag("a"))
        {
            boolean gaveKit = false;
            int kitNotreceived = 0;
            for (User receiver : this.getModule().getUserManager().getOnlineUsers())
            {
                try
                {
                    kit.give(context.getSender(), receiver, force);
                    if (receiver.getName().equals(context.getSender().getName()))
                    {
                        context.sendMessage("basics", "&aReceived the &6%s &akit!", kitname);
                    }
                    else
                    {
                        context.sendMessage("basics", "&aYou gave &2%s &athe &6%s &akit!", receiver.getName(), kitname);
                        receiver.sendMessage("basics", "&aReceived the &6%s &akit. Enjoy it!", kitname);
                    }
                    gaveKit = true;
                }
                catch (Exception ex)
                {
                    kitNotreceived++;
                }
            }
            if (!gaveKit)
            {
                context.sendMessage("basics", "&cNo one received the kit!");
            }
            else if (kitNotreceived > 0)
            {
                context.sendMessage("basics", "&c%d players did not receive a kit!");
            }
        }
        else
        {
            if (context.hasIndexed(1))
            {
                user = context.getUser(1);
            }
            else
            {
                user = context.getSenderAsUser();
            }
            if (user == null)
            {
                paramNotFound(context, "basics", "&cUser %s &cnot found!", context.getString(0));
            }
            kit.give(context.getSender(), user, force);
            if (user.getName().equals(context.getSender().getName()))
            {
                if (kit.getCustomMessage().equals(""))
                {
                    context.sendMessage(kit.getCustomMessage());
                }
                else
                {
                    context.sendMessage("basics", "&aReceived the &6%s &akit. Enjoy it!", kitname);
                }
            }
            else
            {
                context.sendMessage("basics", "&aYou gave &2%s &athe &6%s &akit!", user.getName(), kitname);
                if (kit.getCustomMessage().equals(""))
                {
                    context.sendMessage(kit.getCustomMessage());
                }
                else
                {
                    context.sendMessage("basics", "&aReceived the &6%s &akit. Enjoy it!", kitname);
                }
            }
        }
    }
}
