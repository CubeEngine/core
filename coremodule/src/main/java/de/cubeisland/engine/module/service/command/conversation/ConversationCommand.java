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
package de.cubeisland.engine.module.service.command.conversation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.service.command.CommandManager;
import de.cubeisland.engine.module.service.command.ContainerCommand;
import de.cubeisland.engine.module.service.permission.Permission;
import de.cubeisland.engine.module.service.permission.PermissionManager;
import de.cubeisland.engine.module.core.sponge.EventManager;
import de.cubeisland.engine.module.service.user.User;
import de.cubeisland.engine.module.service.user.UserManager;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

import static org.spongepowered.api.text.format.TextColors.DARK_PURPLE;
import static org.spongepowered.api.text.format.TextColors.WHITE;

public abstract class ConversationCommand extends ContainerCommand
{
    private final Set<UUID> usersInMode = new HashSet<>();

    protected ConversationCommand(Module module)
    {
        super(module);
        module.getModularity().start(EventManager.class).registerListener(module, this);
        getDescriptor().setDispatcher(module.getModularity().start(CommandManager.class)); // needed for exceptionhandler
        Permission childPerm = getDescriptor().getPermission();
        childPerm.setParent(module.getProvided(Permission.class).childWildcard("command"));
        module.getModularity().start(PermissionManager.class).registerPermission(module, childPerm);
        this.registerSubCommands();
    }

    public Module getModule()
    {
        return this.getDescriptor().getModule();
    }

    public boolean hasUser(User user)
    {
        return usersInMode.contains(user.getUniqueId());
    }

    @Subscribe
    public void onChatHandler(PlayerChatEvent event)
    {
        User user = getModule().getModularity().start(UserManager.class).getExactUser(event.getUser().getUniqueId());
        if (this.hasUser(user))
        {
            user.sendMessage(Texts.of(DARK_PURPLE, "[", WHITE, getDescriptor().getName(), DARK_PURPLE, "] ", WHITE, event.getMessage()));

            Text message = event.getMessage();
            CommandInvocation invocation = newInvocation(user, message.toString()); // TODO
            this.execute(invocation);

            event.setCancelled(true);
        }
    }

    private CommandInvocation newInvocation(User user, String message)
    {
        CommandManager cm = getModule().getModularity().start(CommandManager.class);
        return new CommandInvocation(user, message, cm.getProviderManager());
    }

    /* TODO Chat Tabcompletion Events
    @Subscribe
    public void onTabComplete(ChatTabCompleteEvent event)
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
    */

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
