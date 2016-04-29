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
package org.cubeengine.libcube.service.command.confirm;

import org.cubeengine.butler.parametric.Command;
import org.cubeengine.butler.result.CommandResult;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.command.CommandSource;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEUTRAL;

public class ConfirmCommand
{
    private final SpongeConfirmManager confirmManager;
    private I18n i18n;

    public ConfirmCommand(SpongeConfirmManager confirmManager, I18n i18n)
    {
        this.confirmManager = confirmManager;
        this.i18n = i18n;
    }

    @Command(desc = "Confirm a command")
    public CommandResult confirm(CommandSource context)
    {
        int pendingConfirmations = confirmManager.countPendingConfirmations(context);
        if (pendingConfirmations < 1)
        {
            i18n.sendTranslated(context, NEGATIVE, "You don't have any pending confirmations!");
            return null;
        }
        confirmManager.getLastPendingConfirmation(context).run();
        pendingConfirmations = confirmManager.countPendingConfirmations(context);
        if (pendingConfirmations > 0)
        {
            i18n.sendTranslated(context, NEUTRAL, "You have {amount} pending confirmations", pendingConfirmations);
        }
        return null;
    }
}
