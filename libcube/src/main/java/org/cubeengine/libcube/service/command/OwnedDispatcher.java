package org.cubeengine.libcube.service.command;

import java.util.List;
import java.util.Set;
import org.cubeengine.butler.*;
import org.cubeengine.butler.CommandManager;
import org.cubeengine.butler.alias.AliasConfiguration;

/**
 * Owned Proxy Dispatcher
 */
public class OwnedDispatcher implements Dispatcher
{
    private final Dispatcher dispatcher;
    private final OwnedDescriptor descriptor;

    public OwnedDispatcher(Dispatcher dispatcher, Object owner)
    {
        this.dispatcher = dispatcher;
        this.descriptor = new OwnedDescriptor(owner);
    }

    @Override
    public boolean addCommand(CommandBase command)
    {
        return dispatcher.addCommand(command);
    }

    @Override
    public boolean removeCommand(CommandBase command)
    {
        return dispatcher.removeCommand(command);
    }

    @Override
    public Set<CommandBase> getCommands()
    {
        return dispatcher.getCommands();
    }

    @Override
    public boolean hasCommand(String alias)
    {
        return dispatcher.hasCommand(alias);
    }

    @Override
    public CommandBase getCommand(String... alias)
    {
        return dispatcher.getCommand(alias);
    }

    @Override
    public CommandManager getManager()
    {
        return dispatcher.getManager();
    }

    @Override
    public boolean execute(CommandInvocation invocation)
    {
        return dispatcher.execute(invocation);
    }

    @Override
    public CommandDescriptor getDescriptor()
    {
        return descriptor;
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        return dispatcher.getSuggestions(invocation);
    }


    private class OwnedDescriptor implements CommandDescriptor
    {
        private Object owner;

        public OwnedDescriptor(Object owner)
        {
            this.owner = owner;
        }

        @Override
        public String getName()
        {
            return dispatcher.getDescriptor().getName();
        }

        @Override
        public List<AliasConfiguration> getAliases()
        {
            return dispatcher.getDescriptor().getAliases();
        }

        @Override
        public String getUsage(CommandInvocation invocation, String... labels)
        {
            return dispatcher.getDescriptor().getUsage(invocation, labels);
        }

        @Override
        public String getDescription()
        {
            return dispatcher.getDescriptor().getDescription();
        }

        @Override
        public Dispatcher getDispatcher()
        {
            return dispatcher.getDescriptor().getDispatcher();
        }

        @Override
        public Class getOwner()
        {
            return owner.getClass();
        }
    }
}
