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
package org.cubeengine.libcube.service.command;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class DispatcherExecutor implements CommandExecutor
{
    private HelpExecutor help;
    private CommandExecutor executor;

    public DispatcherExecutor(HelpExecutor help, @Nullable CommandExecutor executor)
    {
        this.help = help;
        this.executor = executor;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        if (this.executor != null)
        {
            return this.executor.execute(context);
        }
        return this.help.execute(context);
    }

}
