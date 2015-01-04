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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.ExceptionHandlerProperty;
import de.cubeisland.engine.command.ImmutableCommandDescriptor;
import de.cubeisland.engine.core.command.CommandContainer;
import de.cubeisland.engine.core.command.ExceptionHandler;
import de.cubeisland.engine.core.command.ModuleProvider;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;

public abstract class ConversationCommand extends CommandContainer implements Listener
{
    private final Set<UUID> usersInMode = new HashSet<>();

    protected ConversationCommand(Module module)
    {
        super(module);
        module.getCore().getEventManager().registerListener(module, this);

        ((ImmutableCommandDescriptor)getDescriptor()).setProperty(new ExceptionHandlerProperty(new ExceptionHandler(module.getCore())));
        this.registerSubCommands();
    }

    public Module getModule()
    {
        return this.getDescriptor().valueFor(ModuleProvider.class);
    }

    public boolean hasUser(User user)
    {
        return usersInMode.contains(user.getUniqueId());
    }

    @EventHandler
    public void onChatHandler(AsyncPlayerChatEvent event)
    {
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (this.hasUser(user))
        {
            user.sendMessage(
                ChatFormat.PURPLE + "[" + ChatFormat.WHITE + this.getDescriptor().getName() + ChatFormat.PURPLE + "] "
                    + ChatFormat.WHITE + event.getMessage());

            String message = event.getMessage();
            CommandInvocation invocation = newInvocation(user, message);
            this.execute(invocation);

            event.setCancelled(true);
        }
    }

    private CommandInvocation newInvocation(User user, String message)
    {
        return new CommandInvocation(user, message, this.getModule().getCore().getCommandManager().getReaderManager());
    }

    @EventHandler
    public void onTabComplete(PlayerChatTabCompleteEvent event)
    {
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (this.hasUser(user))
        {
            event.getTabCompletions().clear();
            String message = event.getChatMessage();
            List<String> suggestions = this.getSuggestions(newInvocation(user, message));
            if (suggestions != null)
            {
                event.getTabCompletions().addAll(suggestions);
            }
        }
    }

    /**
     * Adds a user to this chat-commands internal list
     *
     * @param user the user to add
     */
    public boolean addUser(User user)
    {
        return this.usersInMode.add(user.getUniqueId());
    }

    /**
     * Removes a user from this chat-commands internal list
     *
     * @param user the user tp remove
     */
    public void removeUser(User user)
    {
        this.usersInMode.remove(user.getUniqueId());
    }

    @Override
    protected boolean selfExecute(CommandInvocation invocation)
    {
        return super.selfExecute(invocation);
    }
}
