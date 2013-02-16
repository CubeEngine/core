package de.cubeisland.cubeengine.core.command.chatcommand;

import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.HelpContext;
import de.cubeisland.cubeengine.core.command.parameterized.CommandFlag;
import de.cubeisland.cubeengine.core.command.parameterized.CommandParameter;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import gnu.trove.set.hash.TLongHashSet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ChatCommand<M extends Module> extends CubeCommand implements Listener
{
    private TLongHashSet usersInMode = new TLongHashSet();

    protected ChatCommand(M module, ChatCommandContextFactory contextFactory)
    {
        super(module, "", "", contextFactory);
        module.registerListener(this);
    }

    @Override
    public M getModule()
    {
        return (M)super.getModule();
    }

    @EventHandler
    public void onChatHandler(AsyncPlayerChatEvent event)
    {
        User user = this.getModule().getUserManager().getExactUser(event.getPlayer());
        if (usersInMode.contains(user.key))
        {
            this.execute(event.getPlayer(), "", StringUtils.explode(" ", event.getMessage()));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTabComplete(PlayerChatTabCompleteEvent event)
    {
        User user = this.getModule().getUserManager().getExactUser(event.getPlayer());
        if (usersInMode.contains(user.key))
        {
            event.getTabCompletions().clear();
            event.getTabCompletions().addAll(this.tabComplete(event.getPlayer(),"",StringUtils.explode(" ",event.getChatMessage())));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args)
    {
        List<String> list = new ArrayList<String>();
        Set<String> flags = new HashSet<String>();
        Set<String> params  = new HashSet<String>();
        for (CommandFlag flag : this.getContextFactory().getFlags())
        {
            flags.add(flag.getLongName().toLowerCase());
        }
        for (CommandParameter param : this.getContextFactory().getParameters())
        {
            params.add(param.getName().toLowerCase());
        }
        if (args.length == 0)
        {
            list.addAll(flags);
            list.addAll(params);
        }
        else
        {
            String lastArg = args[args.length-1].toLowerCase();
            String beforeLastArg = args.length-2 >= 0 ? args[args.length -2]: null;
            if (lastArg.isEmpty())
            {
                //check for named
                if (beforeLastArg != null && params.contains(beforeLastArg.toLowerCase()))
                {
                    return this.getContextFactory().getParameter(beforeLastArg).getCompleter().complete((User) sender,lastArg);
                }
                else
                {
                    list.addAll(flags);
                    list.addAll(params);
                }
            }
            else
            {
                //check for named
                if (beforeLastArg != null && params.contains(beforeLastArg.toLowerCase()))
                {
                    return this.getContextFactory().getParameter(beforeLastArg).getCompleter().complete((User) sender,lastArg);
                }
                else // check starting
                {
                    for (String flag : flags)
                    {
                        if (flag.startsWith(lastArg))
                        {
                            list.add(flag);
                        }
                    }
                    for (String param : params)
                    {
                        if (param.startsWith(lastArg))
                        {
                            list.add(param);
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public ChatCommandContextFactory getContextFactory()
    {
        return (ChatCommandContextFactory)super.getContextFactory();
    }

    public void addUser(User user)
    {
        this.usersInMode.add(user.key);
    }

    public void removeUser(User user)
    {
        this.usersInMode.remove(user.key);
    }

    @Override
    public void help(HelpContext context) throws Exception
    {
        //TODO show flags and params available
    }
}
