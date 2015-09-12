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
package org.cubeengine.service.command;

import java.util.List;
import java.util.Set;
import com.google.common.base.Optional;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.text.sink.MessageSinks;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.command.CommandSource;

public abstract class AbstractCommandSource implements CommandSource
{
    private MessageSink sink;

    public AbstractCommandSource()
    {
        this.sink = MessageSinks.toAll();
    }

    @Override
    public void sendMessage(Text... messages)
    {
        for (Text message : messages)
        {
            sendMessage0(message);
        }
    }

    @Override
    public void sendMessage(Iterable<Text> messages)
    {
        messages.forEach(this::sendMessage0);
    }

    protected abstract void sendMessage0(Text text);

    @Override
    public MessageSink getMessageSink()
    {
        return sink;
    }

    @Override
    public void setMessageSink(MessageSink sink)
    {
        this.sink = sink;
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
    public boolean isChildOf(Subject parent)
    {
        return internalSubject().isChildOf(parent);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent)
    {
        return internalSubject().isChildOf(contexts, parent);
    }

    @Override
    public List<Subject> getParents()
    {
        return internalSubject().getParents();
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts)
    {
        return internalSubject().getParents(contexts);
    }

    @Override
    public Set<Context> getActiveContexts()
    {
        return internalSubject().getActiveContexts();
    }
}
