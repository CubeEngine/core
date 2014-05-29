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

import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.CubeContext;
import de.cubeisland.engine.core.command.CubeContextFactory;
import de.cubeisland.engine.core.module.Module;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;

public class ConfirmCommand extends CubeCommand
{
    private final ConfirmManager confirmManager;

    public ConfirmCommand(Module module, CubeContextFactory contextFactory, ConfirmManager confirmManager)
    {
        super(module, "confirm", "Confirm a command", contextFactory, null, false);
        this.confirmManager = confirmManager;
    }

    @Override
    public CommandResult run(CubeContext context)
    {
        int pendingConfirmations = confirmManager.countPendingConfirmations(context.getSender());
        if (pendingConfirmations < 1)
        {
            context.sendTranslated(NEGATIVE, "You don't have any pending confirmations!");
            return null;
        }
        confirmManager.getLastPendingConfirmation(context.getSender()).run();
        pendingConfirmations = confirmManager.countPendingConfirmations(context.getSender());
        if (pendingConfirmations > 0)
        {
            context.sendTranslated(NEUTRAL, "You have {amount} pending confirmations", pendingConfirmations);
        }
        return null;
    }
}
