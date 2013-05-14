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
package de.cubeisland.cubeengine.core.command.result.confirm;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.ContextFactory;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.HelpContext;
import de.cubeisland.cubeengine.core.module.Module;

public class ConfirmCommand extends CubeCommand
{
    private final ConfirmManager confirmManager;

    public ConfirmCommand(Module module, ContextFactory contextFactory, ConfirmManager confirmManager)
    {
        super(module, "confirm", "Confirm a command", contextFactory);
        this.confirmManager = confirmManager;
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (!confirmManager.hasPendingConfirmation(context.getSender()))
        {
            context.sendTranslated("You don't have any pending confirmations!");
        }
        confirmManager.getPendingConfirmation(context.getSender()).run();
        return null;
    }

    @Override
    public void help(HelpContext context) throws Exception
    {
        context.sendTranslated("Usage: %s", this.getUsage(context));
    }
}
