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
package org.cubeengine.service.command.conversation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.service.command.CommandManager;
import org.cubeengine.service.command.ContainerCommand;
import org.cubeengine.service.command.property.RawPermission;
import org.cubeengine.service.permission.PermissionManager;
import org.cubeengine.module.core.sponge.EventManager;
import org.cubeengine.service.user.User;
import org.cubeengine.service.user.UserManager;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

import static org.spongepowered.api.text.format.TextColors.DARK_PURPLE;
import static org.spongepowered.api.text.format.TextColors.WHITE;

public abstract class ConversationCommand extends ContainerCommand
{
    private final Set<UUID> usersInMode = new HashSet<>();
    private final CommandManager cm;
    private final UserManager um;

    protected ConversationCommand(Module module)
    {
        super(module);
        module.getModularity().provide(EventManager.class).registerListener(module, this);
        cm = getModule().getModularity().provide(CommandManager.class);
        um = getModule().getModularity().provide(UserManager.class);
        getDescriptor().setDispatcher(cm); // needed for exceptionhandler
        RawPermission permission = getDescriptor().getPermission();
        permission.registerPermission(module, module.getModularity().provide(PermissionManager.class), null);
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
        User user = um.getExactUser(event.getUser().getUniqueId());
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
        return new CommandInvocation(user, message, cm.getProviderManager());
    }

    /* TODO Chat Tabcompletion Events
    @Subscribe
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
