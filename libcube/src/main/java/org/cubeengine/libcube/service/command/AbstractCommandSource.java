/*
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
package org.cubeengine.libcube.service.command;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;

public abstract class AbstractCommandSource implements CommandSource
{
    private MessageChannel channel;

    public AbstractCommandSource()
    {
        this.channel = MessageChannel.TO_ALL;
    }

    @Override
    public void sendMessage(Text message)
    {
        sendMessage0(message);
    }

    @Override
    public void sendMessages(Text... messages)
    {
        for (Text message : messages)
        {
            sendMessage0(message);
        }
    }

    @Override
    public void sendMessages(Iterable<Text> messages)
    {
        messages.forEach(this::sendMessage0);
    }

    protected abstract void sendMessage0(Text text);

    @Override
    public MessageChannel getMessageChannel()
    {
        return channel;
    }

    @Override
    public void setMessageChannel(MessageChannel channel)
    {
        this.channel = channel;
    }

    @Override
    public Optional<CommandSource> getCommandSource()
    {
        return Optional.of(this);
    }

    protected abstract Subject internalSubject();

    @Override
    public SubjectCollection getContainingCollection()
    {
        return internalSubject().getContainingCollection();
    }

    @Override
    public SubjectData getSubjectData()
    {
        return internalSubject().getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData()
    {
        return internalSubject().getTransientSubjectData();
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission)
    {
        return internalSubject().hasPermission(contexts, permission);
    }

    @Override
    public boolean hasPermission(String permission)
    {
        return internalSubject().hasPermission(permission);
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission)
    {
        return internalSubject().getPermissionValue(contexts, permission);
    }

    @Override
    public boolean isChildOf(SubjectReference parent)
    {
        return internalSubject().isChildOf(parent);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, SubjectReference parent)
    {
        return internalSubject().isChildOf(contexts, parent);
    }

    @Override
    public List<SubjectReference> getParents()
    {
        return internalSubject().getParents();
    }

    @Override
    public List<SubjectReference> getParents(Set<Context> contexts)
    {
        return internalSubject().getParents(contexts);
    }

    @Override
    public Set<Context> getActiveContexts()
    {
        return internalSubject().getActiveContexts();
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String key)
    {
        return internalSubject().getOption(contexts, key);
    }
}
