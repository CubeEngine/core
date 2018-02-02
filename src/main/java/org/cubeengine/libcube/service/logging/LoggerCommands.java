/*
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
package org.cubeengine.libcube.service.logging;

import org.cubeengine.logscribe.Log;
import org.cubeengine.logscribe.LogLevel;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.butler.parametric.Optional;
import org.spongepowered.api.command.CommandSource;

public class LoggerCommands
{
    /* TODO
    public LoggerCommands(I18n i18n)
    {
    }


    @Command(desc = "Changes or displays the log level of the server.")
    public void loglevel(CommandSource context, @Optional LogLevel loglevel)
    {
        // TODO persist
        if (loglevel != null)
        {
            core.getProvided(Log.class).setLevel(loglevel);
            i18n.sendTranslated(context, POSITIVE, "New log level successfully set!");
            return;
        }
        i18n.sendTranslated(context, NEUTRAL, "The current log level is: {input#loglevel}", core.getProvided(Log.class).getLevel().getName());
    }
    */
}
