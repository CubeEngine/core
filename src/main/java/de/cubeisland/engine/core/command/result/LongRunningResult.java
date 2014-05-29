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

import de.cubeisland.engine.core.command.CubeContext;
import de.cubeisland.engine.core.command.CommandResult;

public abstract class LongRunningResult implements CommandResult
{
    private boolean isDone = false;
    private int taskId = -1;

    @Override
    public void show(final CubeContext context)
    {
        this.taskId = context.getCore().getTaskManager().runTimer(context.getCommand().getModule(), new Runnable()
        {
            @Override
            public void run()
            {
                LongRunningResult.this.run(context);
                if (isDone)
                {
                    context.getCore().getTaskManager()
                           .cancelTask(context.getCommand().getModule(), taskId);
                }
            }
        }, 0, 1);
        if (this.taskId == -1)
        {
            throw new RuntimeException("Failed to schedule the task for the long running command result!");
        }
    }

    protected final void setDone()
    {
        this.isDone = true;
    }

    public abstract void run(CubeContext context);
}
