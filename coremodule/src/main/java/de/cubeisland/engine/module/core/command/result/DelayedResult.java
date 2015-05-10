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
package de.cubeisland.engine.module.core.command.result;

import java.util.UUID;
import com.google.common.base.Optional;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.result.CommandResult;
import de.cubeisland.engine.module.core.module.Module;

public abstract class DelayedResult implements CommandResult
{
    private Module module;
    private final long delay;

    protected DelayedResult(Module module, long delay)
    {
        this.module = module;
        this.delay = delay;
    }

    protected DelayedResult(Module module)
    {
        this(module, 1);
    }

    @Override
    public void process(final CommandInvocation context)
    {
        Optional<UUID> uuid = module.getCore().getTaskManager().runTaskDelayed(module, () -> DelayedResult.this.run(context), this.delay);

        if (!uuid.isPresent())
        {
            throw new RuntimeException("Failed to schedule the task for the delayed command result!");
        }
    }

    public abstract void run(CommandInvocation context);
}
