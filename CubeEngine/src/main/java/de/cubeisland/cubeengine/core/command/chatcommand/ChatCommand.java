package de.cubeisland.cubeengine.core.command.chatcommand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.HelpContext;
import de.cubeisland.cubeengine.core.command.parameterized.CommandFlag;
import de.cubeisland.cubeengine.core.command.parameterized.CommandParameter;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.StringUtils;

import gnu.trove.set.hash.TLongHashSet;

public abstract class ChatCommand extends CubeCommand implements Listener
{
    private TLongHashSet usersInMode = new TLongHashSet();

    protected ChatCommand(Module module, ChatCommandContextFactory contextFactory)
    {
        super(module, "", "", contextFactory);
        module.getCore().getEventManager().registerListener(module, this);
    }

    @Override
    public Module getModule()
    {
        return super.getModule();
    }

    public boolean hasUser(User user)
    {
        return usersInMode.contains(user.key);
    }

    @EventHandler
    public void onChatHandler(AsyncPlayerChatEvent event)
    {
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer());
        if (this.hasUser(user))
        {
            user.sendMessage(ChatFormat.parseFormats("&5[&fChatCommand&5]&f ") + String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage()));
            this.execute(event.getPlayer(), "", StringUtils.explode(" ", event.getMessage()));
            event.setCancelled(true);
        }
        //TODO block chat for users in mode & save missed chatlines to print when exiting
    }

    @EventHandler
    public void onTabComplete(PlayerChatTabCompleteEvent event)
    {
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer());
        if (this.hasUser(user))
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

    /**
     * Adds a user to this chatcommands internal list
     *
     * @param user
     */
    public boolean addUser(User user)
    {
        return this.usersInMode.add(user.key);
    }

    /**
     * Removes a user from this chatcommands internal list
     *
     * @param user
     */
    public void removeUser(User user)
    {
        this.usersInMode.remove(user.key);
    }

    @Override
    public void help(HelpContext context) throws Exception //TODO beautify this
    {
        context.sendMessage("core","Flags:");
        Set<String> flags = new HashSet<String>();
        for (CommandFlag flag : this.getContextFactory().getFlags())
        {
            flags.add(flag.getLongName().toLowerCase());
        }
        context.sendMessage("    "+StringUtils.implode("&7, &f",flags));
        context.sendMessage("core","Parameters:");
        Set<String> params  = new HashSet<String>();
        for (CommandParameter param : this.getContextFactory().getParameters())
        {
            params.add(param.getName().toLowerCase());
        }
        context.sendMessage("    "+StringUtils.implode("&7, &f",params));
    }
}
