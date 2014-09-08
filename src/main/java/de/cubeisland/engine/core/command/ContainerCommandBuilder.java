package de.cubeisland.engine.core.command;

import java.util.ArrayList;

import de.cubeisland.engine.command.BaseCommandBuilder;
import de.cubeisland.engine.command.CommandRunner;
import de.cubeisland.engine.command.base.Command;
import de.cubeisland.engine.command.context.CtxDescriptor;
import de.cubeisland.engine.command.context.parameter.BaseParameterBuilder;
import de.cubeisland.engine.command.context.parameter.FlagParameter;
import de.cubeisland.engine.command.context.parameter.NamedParameter;
import de.cubeisland.engine.command.context.parameter.ParameterBuilder;
import de.cubeisland.engine.command.context.parameter.ParameterGroup;
import de.cubeisland.engine.command.result.CommandResult;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.command.context.CubeContextFactory;
import de.cubeisland.engine.core.command.parameterized.PermissibleIndexedParameter;
import de.cubeisland.engine.core.module.Module;

public class ContainerCommandBuilder extends BaseCommandBuilder<ContainerCommand>
{
    private static BaseParameterBuilder<PermissibleIndexedParameter, ContainerCommand> builder = new BaseParameterBuilder<PermissibleIndexedParameter, ContainerCommand>(
        PermissibleIndexedParameter.class)
    {
        @Override
        public ParameterBuilder<PermissibleIndexedParameter, ContainerCommand> build(ContainerCommand source)
        {
            builder.begin();
            builder.setValueLabel("action");
            builder.setDescription("Possible actions");
            builder.setGreed(1);
            return this;
        }
    };

    public ContainerCommandBuilder(ContainerCommand source)
    {
        super(source);
        Class<? extends ContainerCommand> clazz = source.getClass();
        Command annotation = clazz.getAnnotation(Command.class);
        if (annotation == null)
        {
            throw new IllegalArgumentException("Container command without Command Annotation");
        }
        CubeContextFactory ctxFactory = new CubeContextFactory(new CtxDescriptor(builder.build(source).finish(), new ParameterGroup<NamedParameter>(), new ArrayList<FlagParameter>()));
        this.init(annotation.name(), annotation.desc(), ctxFactory, new ContainerRunner(source));
    }

    protected ContainerCommandBuilder setModule(Module module)
    {
        cmd().module = module;
        return this;
    }

    private class ContainerRunner implements CommandRunner<CubeContext>
    {
        private final CubeCommand container;

        private ContainerRunner(CubeCommand container)
        {
            this.container = container;
        }

        @Override
        public CommandResult run(CubeContext context)
        {
            return this.container.getChild("?").run(context);
        }
    }
}
