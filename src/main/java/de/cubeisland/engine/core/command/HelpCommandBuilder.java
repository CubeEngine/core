package de.cubeisland.engine.core.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.cubeisland.engine.command.ExternalBaseCommandBuilder;
import de.cubeisland.engine.command.CommandRunner;
import de.cubeisland.engine.command.context.parameter.FlagParameter;
import de.cubeisland.engine.command.context.parameter.NamedParameter;
import de.cubeisland.engine.command.result.CommandResult;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.command.context.CubeContextFactory;
import de.cubeisland.engine.core.command.conversation.ConversationCommand;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.command.context.CtxDescriptor.emptyDescriptor;
import static de.cubeisland.engine.core.util.ChatFormat.*;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class HelpCommandBuilder<HCmdT extends HelpCommand> extends ExternalBaseCommandBuilder<HCmdT, CubeCommand, Void>
{
    public HelpCommandBuilder(Class<HCmdT> clazz)
    {
        super(clazz, null);
    }

    @Override
    protected ExternalBaseCommandBuilder<HCmdT, CubeCommand, Void> build(CubeCommand source)
    {
        this.begin();

        this.init("?", "Displays Help", new CubeContextFactory(emptyDescriptor()), getRunner(source));
        this.cmd().module = source.module;
        this.cmd().helpTarget = source;

        return this;
    }

    public CommandRunner<CubeContext> getRunner(CubeCommand source)
    {
        if (source instanceof ContainerCommand)
        {
            return new ContainerHelpRunner(source);
        }
        else if (source instanceof ConversationCommand)
        {
            return new ConversationHelpRunner(source);
        }
        else
        {
            return new HelpRunner(source);
        }
    }

    private class HelpRunner implements CommandRunner<CubeContext>
    {
        private CubeCommand helpTarget;

        public HelpRunner(CubeCommand source)
        {
            this.helpTarget = source;
        }

        @Override
        public CommandResult run(CubeContext context)
        {
            context.sendTranslated(NONE, "{text:Description:color=GREY}: {input}", helpTarget.getDescription());
            context.sendTranslated(NONE, "{text:Usage:color=GREY}: {input}", helpTarget.getUsage(context));
            context.sendMessage(" ");
            context.sendTranslated(NONE, "{text:Detailed help:color=GREY}: {input#link:color=INDIGO}",
                                   "http://engine.cubeisland.de/c/" + helpTarget.getModule().getId() + "/"
                                       + helpTarget.implodeCommandParentNames("/"));
            return null;
        }
    }

    private class ConversationHelpRunner implements CommandRunner<CubeContext>
    {
        private CubeCommand helpTarget;

        private ConversationHelpRunner(CubeCommand helpTarget)
        {
            this.helpTarget = helpTarget;
        }

        @Override
        public CommandResult run(CubeContext context)
        {
            context.sendTranslated(NEUTRAL, "Flags:");
            Set<String> flags = new HashSet<>();
            for (FlagParameter flag : helpTarget.getContextFactory().descriptor().getFlags())
            {
                flags.add(flag.getLongName().toLowerCase());
            }
            context.sendMessage("    " + StringUtils.implode(ChatFormat.GREY + ", " + ChatFormat.WHITE, flags));
            context.sendTranslated(NEUTRAL, "Parameters:");
            Set<String> params  = new HashSet<>();
            for (NamedParameter param : helpTarget.getContextFactory().descriptor().getNamedGroups().listAll())
            {
                params.add(param.getName().toLowerCase());
            }
            context.sendMessage("    " + StringUtils.implode(ChatFormat.GREY + ", " + ChatFormat.WHITE, params));
            return null;
        }
    }

    private class ContainerHelpRunner implements CommandRunner<CubeContext>
    {
        private CubeCommand helpTarget;

        public ContainerHelpRunner(CubeCommand source)
        {
            this.helpTarget = source;
        }

        @Override
        public CommandResult run(CubeContext context)
        {
            CommandSender sender = context.getSource();
            context.sendTranslated(NONE, "{text:Usage:color=INDIGO}: {input#usage}", helpTarget.getUsage(context));
            context.sendMessage(" ");

            List<CubeCommand> commands = new ArrayList<>();
            for (CubeCommand child : helpTarget.getChildren())
            {
                if (child instanceof HelpCommand)
                {
                    continue;
                }
                if (!child.isCheckperm() || child.isAuthorized(sender))
                {
                    commands.add(child);
                }
            }

            if (commands.isEmpty())
            {
                context.sendTranslated(NEGATIVE, "No actions are available");
            }
            else
            {
                context.sendTranslated(NEUTRAL, "The following actions are available:");
                context.sendMessage(" ");
                for (CubeCommand command : commands)
                {
                    context.sendMessage(YELLOW + command.getName() + WHITE + ": " + GREY + sender.getTranslation(NONE,
                                                                                                                 command.getDescription()));
                }
            }
            context.sendMessage(" ");
            context.sendTranslated(NONE, "{text:Detailed help:color=GREY}: {input#link:color=INDIGO}",
                                   "http://engine.cubeisland.de/c/" + helpTarget.implodeCommandParentNames("/"));
            return null;
        }
    }
}
