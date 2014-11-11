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
package de.cubeisland.engine.core.command.result;

import de.cubeisland.engine.command.result.CommandResult;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ModuleProvider;
import de.cubeisland.engine.core.module.Module;

public abstract class DelayedResult implements CommandResult<CommandContext>
{
    private final long delay;

    protected DelayedResult(long delay)
    {
        this.delay = delay;
    }

    protected DelayedResult()
    {
        this(1);
    }

    @Override
    public void process(final CommandContext context)
    {
        Module module = context.getCommand().getDescriptor().valueFor(ModuleProvider.class);
        final int taskId = context.getCore().getTaskManager().runTaskDelayed(module, new Runnable()
        {
            @Override
            public void run()
            {
                DelayedResult.this.run(context);
            }
        }, this.delay);

        if (taskId == -1)
        {
            throw new RuntimeException("Failed to schedule the task for the delayed command result!");
        }
    }

    public abstract void run(CommandContext context);
}
