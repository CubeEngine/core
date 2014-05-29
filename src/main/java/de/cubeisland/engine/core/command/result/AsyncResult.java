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

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.CubeContext;

public abstract class AsyncResult implements CommandResult
{
    public abstract void main(CubeContext context);
    public void onFinish(CubeContext context)
    {}

    @Override
    public final void show(final CubeContext context)
    {
        if (CubeEngine.isMainThread()) // only run on another thread if we're on the main thread
        {
            context.getCore().getTaskManager().getThreadFactory().newThread(new Runnable() {
                @Override
                public void run()
                {
                    doShow(context);
                }
            }).start();
        }
        else
        {
            this.doShow(context);
        }
    }

    private void doShow(final CubeContext context)
    {
        this.main(context);
        context.getCore().getTaskManager().runTask(context.getCommand().getModule(), new Runnable()
        {
            @Override
            public void run()
            {
                onFinish(context);
            }
        });
    }
}
