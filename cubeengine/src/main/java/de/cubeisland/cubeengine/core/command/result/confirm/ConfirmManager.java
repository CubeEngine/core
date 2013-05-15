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

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.ArgBounds;
import de.cubeisland.cubeengine.core.command.BasicContextFactory;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.Pair;

public class ConfirmManager
{
    private static final int CONFIRM_TIMEOUT = 600; // 30 seconds
    private final ConcurrentMap<CommandSender, Queue<ConfirmResult>> pendingConfirmations;
    private final ConcurrentHashMap<CommandSender, Queue<Pair<Module, Integer>>> confirmationTimeoutTasks;
    private final Core core;

    public ConfirmManager(CommandManager commandManager, Core core)
    {
        this.core = core;
        this.pendingConfirmations = new ConcurrentHashMap<CommandSender, Queue<ConfirmResult>>();
        confirmationTimeoutTasks = new ConcurrentHashMap<CommandSender, Queue<Pair<Module, Integer>>>();
        commandManager.registerCommand(new ConfirmCommand(core.getModuleManager()
                                                              .getCoreModule(), new BasicContextFactory(new ArgBounds(0, 0)), this));
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
    public synchronized void registerConfirmation(ConfirmResult confirmResult, Module module, CommandSender sender)
    {
        Queue<ConfirmResult> pendingConfirmations = this.pendingConfirmations.get(sender);
        if (pendingConfirmations == null)
        {
            pendingConfirmations = new LinkedList<ConfirmResult>();
        }
        pendingConfirmations.add(confirmResult);
        this.pendingConfirmations.put(sender, pendingConfirmations);

        Queue<Pair<Module, Integer>> confirmationTimeoutTasks = this.confirmationTimeoutTasks.get(sender);
        if (confirmationTimeoutTasks == null)
        {
            confirmationTimeoutTasks = new LinkedList<Pair<Module, Integer>>();
        }
        confirmationTimeoutTasks.add(new Pair<Module, Integer>(module, this.core.getTaskManager()
                                                                                .runTaskDelayed(module, new ConfirmationTimeoutTask(sender), CONFIRM_TIMEOUT)));
        this.confirmationTimeoutTasks.put(sender, confirmationTimeoutTasks);
    }

    /**
     * Check if a commandSender has something to confirm
     * @param sender
     * @return
     */
    public synchronized int countPendingConfirmations(CommandSender sender)
    {
        assert sender != null;
        Queue<ConfirmResult> pendingConfirmations = this.pendingConfirmations.get(sender);
        if (pendingConfirmations == null)
        {
            return 0;
        }
        return pendingConfirmations.size();
    }

    /**
     * Get the pending confirmation of the CommandSender and abort the task.
     * This can only be called once per confirmation
     * @param sender
     * @return
     */
    public synchronized ConfirmResult getLastPendingConfirmation(CommandSender sender)
    {
        if (countPendingConfirmations(sender) < 1)
        {
            return null;
        }

        Queue<Pair<Module, Integer>> confirmationTimeoutTasks = this.confirmationTimeoutTasks.get(sender);
        if (confirmationTimeoutTasks == null)
        {
            confirmationTimeoutTasks = new LinkedList<Pair<Module, Integer>>();
        }
        Pair<Module, Integer> pair = confirmationTimeoutTasks.poll();
        this.confirmationTimeoutTasks.put(sender, confirmationTimeoutTasks);
        this.core.getTaskManager().cancelTask(pair.getLeft(), pair.getRight());

        Queue<ConfirmResult> pendingConfirmations = this.pendingConfirmations.get(sender);
        if (pendingConfirmations == null)
        {
            pendingConfirmations = new LinkedList<ConfirmResult>();
        }
        this.pendingConfirmations.put(sender, pendingConfirmations);
        return pendingConfirmations.poll();
    }

    /**
     * Class to remove tasks that have timed out, and notify the CommandSender
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
            sender.sendTranslated("&cYour confirmation timed out....");
            pendingConfirmations.remove(sender);
        }
    }
}
