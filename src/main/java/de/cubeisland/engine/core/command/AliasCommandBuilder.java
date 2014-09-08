package de.cubeisland.engine.core.command;

import java.util.Set;

import de.cubeisland.engine.command.BaseCommandBuilder;
import de.cubeisland.engine.command.CommandRunner;
import de.cubeisland.engine.command.result.CommandResult;
import de.cubeisland.engine.core.command.context.CubeContext;

public class AliasCommandBuilder extends BaseCommandBuilder<AliasCommand>
{
    public AliasCommandBuilder(AliasCommand cmd, CubeCommand target, String name, Set<String> aliases)
    {
        super(cmd);
        this.init(name, target.getDescription(), target.getContextFactory(), new AliasRunner(target));
        this.setAlias(aliases);
        cmd.module = target.getModule();
    }

    private class AliasRunner implements CommandRunner<CubeContext>
    {
        private final CubeCommand target;

        private AliasRunner(CubeCommand target)
        {
            this.target = target;
        }

        @Override
        public CommandResult run(CubeContext context)
        {
            return this.target.run(context);
        }
    }
}
