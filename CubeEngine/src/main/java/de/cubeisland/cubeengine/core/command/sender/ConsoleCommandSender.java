package de.cubeisland.cubeengine.core.command.sender;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.i18n.I18n;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;

import java.util.Locale;

public class ConsoleCommandSender extends WrappedCommandSender implements org.bukkit.command.ConsoleCommandSender
{
    public static final String NAME = ":console";

    public ConsoleCommandSender(Core core, org.bukkit.command.ConsoleCommandSender sender)
    {
        super(core, sender);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getDisplayName()
    {
        final I18n i18n = this.getCore().getI18n();
        return i18n.translate(Locale.getDefault(), "Console");
    }

    @Override
    public org.bukkit.command.ConsoleCommandSender getWrappedSender()
    {
        return (org.bukkit.command.ConsoleCommandSender)super.getWrappedSender();
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
