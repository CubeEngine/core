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
package org.cubeengine.module.core.util;

import org.cubeengine.module.core.CoreModule;
import org.cubeengine.service.task.TaskManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * This class contains various methods to access sponge-related stuff.
 */
public class SignalUtil
{
    private SignalUtil()
    {}



    public static void setSignalHandlers(final CoreModule core)
    {
        try
        {
            Class.forName("sun.misc.Signal");

            Signal.handle(new Signal("INT"), new SignalHandler()
            {
                private long lastReceived = 0;

                @Override
                public void handle(Signal signal)
                {
                    if (this.lastReceived == -1)
                    {
                        return;
                    }
                    final long time = System.currentTimeMillis();
                    if (time - this.lastReceived <= 5000)
                    {
                        core.getLog().info("Shutting down the server now!");
                        core.getModularity().provide(TaskManager.class).runTask(core, () -> {
                            Sponge.getServer().shutdown(Text.of()); // tODO default message?
                            lastReceived = -1;
                        });
                    }
                    else
                    {
                        this.lastReceived = time;
                        core.getLog().info("You can't copy content from the console using CTRL-C!");
                        core.getLog().info("If you really want shutdown the server use the stop command or press CTRL-C again within 5 seconds!");
                    }
                }
            });

        }
        catch (ClassNotFoundException ignored)
        {}
    }

}
