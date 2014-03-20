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
package de.cubeisland.engine.core.command.sender;

import java.util.Locale;

import org.bukkit.Server;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;

import de.cubeisland.engine.core.bukkit.BukkitCore;

public class ConsoleCommandSender extends WrappedCommandSender implements org.bukkit.command.ConsoleCommandSender
{
    public static final String NAME = ":console";
    private final Server server;

    public ConsoleCommandSender(BukkitCore core)
    {
        super(core, core.getServer().getConsoleSender());
        this.server = core.getServer();
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getDisplayName()
    {
        return this.getCore().getI18n().translate(Locale.getDefault(), "Console");
    }

    @Override
    public org.bukkit.command.ConsoleCommandSender getWrappedSender()
    {
        return this.server.getConsoleSender();
    }

    @Override
    public boolean hasPermission(String name)
    {
        return true;
    }

    @Override
    public boolean hasPermission(Permission perm)
    {
        return true;
    }

    @Override
    public boolean isConversing()
    {

        return this.getWrappedSender().isConversing();
    }

    @Override
    public void acceptConversationInput(String input)
    {
        this.getWrappedSender().acceptConversationInput(input);
    }

    @Override
    public boolean beginConversation(Conversation conversation)
    {
        return this.getWrappedSender().beginConversation(conversation);
    }

    @Override
    public void abandonConversation(Conversation conversation)
    {
        this.getWrappedSender().abandonConversation(conversation);
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details)
    {
        this.getWrappedSender().abandonConversation(conversation, details);
    }

    @Override
    public void sendRawMessage(String message)
    {
        this.getWrappedSender().sendRawMessage(message);
    }
}
