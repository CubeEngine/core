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
package de.cubeisland.engine.core.command;

import de.cubeisland.engine.command.exception.CommandException;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;


public class HelpCommand extends CubeCommand
{
    private static final HelpCommandBuilder<HelpCommand> helpCmdBuilder = new HelpCommandBuilder<>(HelpCommand.class);

    protected CubeCommand helpTarget;

    @Override
    public void checkContext(CubeContext ctx) throws CommandException
    {
        if (this.helpTarget.isCheckperm() && !this.helpTarget.isAuthorized(ctx.getSource()))
        {
            throw new PermissionDeniedException(this.helpTarget.getPermission());
        }
    }

    public static HelpCommand newHelpCommand(CubeCommand command)
    {
        return helpCmdBuilder.build(command).finish();
    }
}
