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
package org.cubeengine.service.command.exception;

import de.cubeisland.engine.logscribe.Log;
import org.cubeengine.butler.*;
import org.cubeengine.butler.exception.PriorityExceptionHandler;
import org.spongepowered.api.command.CommandSource;

public class UnknownSourceExceptionHandler implements PriorityExceptionHandler
{
    private Log logger;

    public UnknownSourceExceptionHandler(Log logger)
    {
        this.logger = logger;
    }

    @Override
    public boolean handleException(Throwable e, CommandBase command, CommandInvocation invocation)
    {
        if (!(invocation.getCommandSource() instanceof CommandSource))
        {
           logger.info("An unknown CommandSource ({}) caused an exception: {}",
                    invocation.getCommandSource().getClass().getName(), e.getMessage());
            return true;
        }
        return false;
    }

    @Override
    public int priority()
    {
        return Integer.MIN_VALUE;
    }
}
