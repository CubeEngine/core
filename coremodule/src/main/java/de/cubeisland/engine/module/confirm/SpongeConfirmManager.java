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
package de.cubeisland.engine.module.confirm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.Enable;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.command.CommandManager;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.contract.Contract;
import de.cubeisland.engine.module.core.contract.NotNull;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.task.TaskManager;
import de.cubeisland.engine.module.core.util.Pair;
import de.cubeisland.engine.module.core.util.formatter.MessageType;
import de.cubeisland.engine.module.paginate.PaginationCommands;

@ServiceImpl(ConfirmManager.class)
@Version(1)
public class SpongeConfirmManager implements ConfirmManager
{
    private static final int CONFIRM_TIMEOUT = 600; // 30 seconds
    private final Map<CommandSender, Queue<ConfirmResult>> pendingConfirmations;
    private final Map<CommandSender, Queue<Pair<Module, UUID>>> confirmationTimeoutTasks;
    private CommandManager cm;
    private final CoreModule core;

    @Inject
    public SpongeConfirmManager(CommandManager cm, CoreModule core)
    {
        this.cm = cm;
        this.core = core;
        this.pendingConfirmations = new HashMap<>();
        confirmationTimeoutTasks = new HashMap<>();
    }

    @Enable
    public void onEnable()
    {
        cm.addCommands(cm, core, new ConfirmCommand(this));
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
    @Override
    public synchronized void registerConfirmation(ConfirmResult confirmResult, Module module, CommandSender sender)
    {
        Queue<ConfirmResult> pendingConfirmations = this.pendingConfirmations.get(sender);
        if (pendingConfirmations == null)
        {
            pendingConfirmations = new LinkedList<>();
        }
        pendingConfirmations.add(confirmResult);
        this.pendingConfirmations.put(sender, pendingConfirmations);

        Queue<Pair<Module, UUID>> confirmationTimeoutTasks = this.confirmationTimeoutTasks.get(sender);
        if (confirmationTimeoutTasks == null)
        {
            confirmationTimeoutTasks = new LinkedList<>();
        }
        confirmationTimeoutTasks.add(new Pair<>(module, this.core.getModularity().start(TaskManager.class).runTaskDelayed(
            module, new ConfirmationTimeoutTask(sender), CONFIRM_TIMEOUT).get()));
        this.confirmationTimeoutTasks.put(sender, confirmationTimeoutTasks);
    }

    /**
     * Check if a commandSender has something to confirm
     * @param sender
     * @return
     */
    @Override
    public synchronized int countPendingConfirmations(@NotNull CommandSender sender)
    {
        Contract.expectNotNull(sender);
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
    @Override
    public synchronized ConfirmResult getLastPendingConfirmation(CommandSender sender)
    {
        if (countPendingConfirmations(sender) < 1)
        {
            return null;
        }

        Queue<Pair<Module, UUID>> confirmationTimeoutTasks = this.confirmationTimeoutTasks.get(sender);
        if (confirmationTimeoutTasks == null)
        {
            confirmationTimeoutTasks = new LinkedList<>();
        }
        Pair<Module, UUID> pair = confirmationTimeoutTasks.poll();
        this.confirmationTimeoutTasks.put(sender, confirmationTimeoutTasks);
        this.core.getModularity().start(TaskManager.class).cancelTask(pair.getLeft(), pair.getRight());

        Queue<ConfirmResult> pendingConfirmations = this.pendingConfirmations.get(sender);
        if (pendingConfirmations == null)
        {
            pendingConfirmations = new LinkedList<>();
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
            sender.sendTranslated(MessageType.NEGATIVE, "Your confirmation timed out....");
            pendingConfirmations.remove(sender);
        }
    }
}
