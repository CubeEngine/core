/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.command.conversation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.HelpContext;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import gnu.trove.set.hash.TLongHashSet;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;

public abstract class ConversationCommand extends CubeCommand implements Listener
{
    private final TLongHashSet usersInMode = new TLongHashSet();

    protected ConversationCommand(Module module, ConversationContextFactory contextFactory)
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
        return usersInMode.contains(user.getId());
    }

    @EventHandler
    public void onChatHandler(AsyncPlayerChatEvent event)
    {
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (this.hasUser(user))
        {
            user.sendMessage(ChatFormat.PURPLE + "[" + ChatFormat.WHITE + "ChatCommand" + ChatFormat.PURPLE + "] " + ChatFormat.WHITE + event.getMessage());
            this.execute(event.getPlayer(), "", StringUtils.explode(" ", event.getMessage()));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTabComplete(PlayerChatTabCompleteEvent event)
    {
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (this.hasUser(user))
        {
            event.getTabCompletions().clear();
            event.getTabCompletions().addAll(this.tabComplete(event.getPlayer(),"",StringUtils.explode(" ",event.getChatMessage())));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args)
    {
        List<String> list = new ArrayList<>();
        Set<String> flags = new HashSet<>();
        Set<String> params  = new HashSet<>();
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
                    return this.getContextFactory().getParameter(beforeLastArg).getCompleter().complete(sender, lastArg);
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
                    return this.getContextFactory().getParameter(beforeLastArg).getCompleter().complete(sender, lastArg);
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
    public ConversationContextFactory getContextFactory()
    {
        return (ConversationContextFactory)super.getContextFactory();
    }

    /**
     * Adds a user to this chatcommands internal list
     *
     * @param user
     */
    public boolean addUser(User user)
    {
        return this.usersInMode.add(user.getId());
    }

    /**
     * Removes a user from this chatcommands internal list
     *
     * @param user
     */
    public void removeUser(User user)
    {
        this.usersInMode.remove(user.getId());
    }

    @Override
    public void help(HelpContext context) throws Exception
    {
        context.sendTranslated(NEUTRAL, "Flags:");
        Set<String> flags = new HashSet<>();
        for (CommandFlag flag : this.getContextFactory().getFlags())
        {
            flags.add(flag.getLongName().toLowerCase());
        }
        context.sendMessage("    " + StringUtils.implode(ChatFormat.GREY + ", " + ChatFormat.WHITE, flags));
        context.sendTranslated(NEUTRAL, "Parameters:");
        Set<String> params  = new HashSet<>();
        for (CommandParameter param : this.getContextFactory().getParameters())
        {
            params.add(param.getName().toLowerCase());
        }
        context.sendMessage("    " + StringUtils.implode(ChatFormat.GREY + ", " + ChatFormat.WHITE, params));
    }
}
