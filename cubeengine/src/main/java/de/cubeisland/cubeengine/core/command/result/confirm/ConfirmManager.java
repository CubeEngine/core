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

import java.util.HashMap;
import java.util.Map;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.ArgBounds;
import de.cubeisland.cubeengine.core.command.BasicContextFactory;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.time.Duration;

public class ConfirmManager
{
    private static final int CONFIRM_TIMEOUT = 600; // 30 seconds
    private final Map<CommandSender, ConfirmResult> pendingConfirmations;
    private final Map<CommandSender, Pair<Module, Integer>> confirmationTimeoutTasks;
    private final Core core;

    public ConfirmManager(Core core)
    {
        this.pendingConfirmations = new HashMap<CommandSender, ConfirmResult>();
        confirmationTimeoutTasks = new HashMap<CommandSender, Pair<Module, Integer>>();
        this.core = core;
        core.getCommandManager().registerCommand(new ConfirmCommand(core.getModuleManager().getCoreModule(),
                                                                    new BasicContextFactory(new ArgBounds(0, 0)), this));
    }

    /**
     * Register a Confirmation request. This will start a timer that will abort the request after 30 seconds and notify
     * the user.
     * This should only be called from the ConfirmResult itself!
     *
     * @param confirmResult The ConfirmResult to register
     * @param module The module the ConfirmResult is registered to
     * @param sender The user that need to confirm something
     */
    public void registerConfirmResult(ConfirmResult confirmResult, Module module, CommandSender sender)
    {
        this.pendingConfirmations.put(sender, confirmResult);
        this.confirmationTimeoutTasks.put(sender, new Pair<Module, Integer>(module, this.core.getTaskManager()
                                                                                             .runTaskDelayed(module, new ConfirmationTimeoutTask(sender),
                                                                                                             CONFIRM_TIMEOUT)));
    }

    /**
     * Check if a commandSender has something to confirm
     * @param sender
     * @return
     */
    public boolean hasPendingConfirmation(CommandSender sender)
    {
        return pendingConfirmations.containsKey(sender);
    }

    /**
     * Get the pending confirmation of the CommandSender and abort the task.
     * This can only be called once per confirmation
     * @param sender
     * @return
     */
    public ConfirmResult getPendingConfirmation(CommandSender sender)
    {
        Pair<Module, Integer> pair = this.confirmationTimeoutTasks.get(sender);
        this.core.getTaskManager().cancelTask(pair.getLeft(), pair.getRight());
        return pendingConfirmations.get(sender);
    }

    /**
     * Class to remove tasks that have timed out, and notify the COmmandSender
     */
    private class ConfirmationTimeoutTask implements Runnable
    {

        private final CommandSender sender;

        private ConfirmationTimeoutTask(CommandSender sender)
        {
            this.sender = sender;
        }

        @Override
        public void run()
        {
            sender.sendTranslated("Your confirmation timed out....");
            pendingConfirmations.remove(sender);
        }
    }
}
