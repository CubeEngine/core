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
package org.cubeengine.module.controlc;

import javax.inject.Inject;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.libcube.service.task.TaskManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import sun.misc.Signal;
import sun.misc.SignalHandler;

@ModuleInfo(name = "ControlC", description = "Prevents the server shutting down immediatly by pressing control and C")
public class ControlC extends Module implements SignalHandler
{
    @Inject private Log logger;
    @Inject private TaskManager tm;

    private long lastReceived = 0;

    @Enable
    public void onEnable()
    {
        try
        {
            Class.forName("sun.misc.Signal");

            Signal.handle(new Signal("INT"), this);
        }
        catch (ClassNotFoundException ignored)
        {
        }
    }

    @Override
    public void handle(Signal signal)
    {
        if (lastReceived == -1)
        {
            return;
        }
        final long time = System.currentTimeMillis();
        if (time - lastReceived <= 5000)
        {
            logger.info("Shutting down the server now!");
            tm.runTask(this, () -> {
                Sponge.getServer().shutdown(Text.of()); // tODO default message?
                lastReceived = -1;
            });
        }
        else
        {
            lastReceived = time;
            logger.info("You can't copy content from the console using CTRL-C!");
            logger.info("If you really want shutdown the server use the stop command or press CTRL-C again within 5 seconds!");
        }
    }
}
