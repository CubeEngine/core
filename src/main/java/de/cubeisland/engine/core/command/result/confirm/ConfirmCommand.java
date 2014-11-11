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
package de.cubeisland.engine.core.command.result.confirm;

import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.result.CommandResult;
import de.cubeisland.engine.core.command.CommandContext;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;

public class ConfirmCommand
{
    private final ConfirmManager confirmManager;

    public ConfirmCommand(ConfirmManager confirmManager)
    {
        this.confirmManager = confirmManager;
    }

    @Command(desc = "Confirm a command")
    public CommandResult confirm(CommandContext context)
    {
        int pendingConfirmations = confirmManager.countPendingConfirmations(context.getSource());
        if (pendingConfirmations < 1)
        {
            context.sendTranslated(NEGATIVE, "You don't have any pending confirmations!");
            return null;
        }
        confirmManager.getLastPendingConfirmation(context.getSource()).run();
        pendingConfirmations = confirmManager.countPendingConfirmations(context.getSource());
        if (pendingConfirmations > 0)
        {
            context.sendTranslated(NEUTRAL, "You have {amount} pending confirmations", pendingConfirmations);
        }
        return null;
    }
}
