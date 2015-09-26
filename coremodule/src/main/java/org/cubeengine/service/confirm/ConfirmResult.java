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
package org.cubeengine.service.confirm;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.result.CommandResult;
import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.service.command.CommandContext;
import org.spongepowered.api.util.command.CommandSource;


/**
 * A result that should be confirmed via the /confirm command
 */
public class ConfirmResult implements CommandResult
{
    private final Runnable runnable;
    private final CommandSource sender;
    private final Module module;

    public ConfirmResult(Module module, Runnable runnable, CommandContext context)
    {
        this.module = module;
        this.runnable = runnable;
        this.sender = context.getSource();
    }

    @Override
    public void process(CommandInvocation context)
    {
        module.getModularity().provide(SpongeConfirmManager.class).registerConfirmation(this, this.module, sender);
    }

    public void run()
    {
        this.runnable.run();
    }
}
