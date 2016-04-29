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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import javax.inject.Inject;
import com.google.common.base.Preconditions;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.libcube.util.Pair;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.task.TaskManager;
import org.spongepowered.api.command.CommandSource;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;

@ServiceImpl(ConfirmManager.class)
@Version(1)
public class SpongeConfirmManager implements ConfirmManager
{
    private static final int CONFIRM_TIMEOUT = 600; // 30 seconds
    private final Map<String, Queue<ConfirmResult>> pendingConfirmations;
    private final Map<String, Queue<Pair<Module, UUID>>> confirmationTimeoutTasks;
    private CommandManager cm;
    @Inject private TaskManager taskManager;
    @Inject private I18n i18n;

    @Inject
    public SpongeConfirmManager(CommandManager cm)
    {
        this.cm = cm;
        this.pendingConfirmations = new HashMap<>();
        confirmationTimeoutTasks = new HashMap<>();
    }

    @Enable
    public void onEnable()
    {
        cm.addCommands(null, new ConfirmCommand(this, i18n)); // TODO allow register commands as non-module
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
    public synchronized void registerConfirmation(ConfirmResult confirmResult, Module module, CommandSource sender)
    {
        Queue<ConfirmResult> pendingConfirmations = this.pendingConfirmations.get(sender.getIdentifier());
        if (pendingConfirmations == null)
        {
            pendingConfirmations = new LinkedList<>();
        }
        pendingConfirmations.add(confirmResult);
        this.pendingConfirmations.put(sender.getIdentifier(), pendingConfirmations);

        Queue<Pair<Module, UUID>> confirmationTimeoutTasks = this.confirmationTimeoutTasks.get(sender.getIdentifier());
        if (confirmationTimeoutTasks == null)
        {
            confirmationTimeoutTasks = new LinkedList<>();
        }
        confirmationTimeoutTasks.add(new Pair<>(module, taskManager.runTaskDelayed(
            module, new ConfirmationTimeoutTask(sender), CONFIRM_TIMEOUT)));
        this.confirmationTimeoutTasks.put(sender.getIdentifier(), confirmationTimeoutTasks);
    }

    /**
     * Check if a commandSender has something to confirm
     * @param sender
     * @return
     */
    @Override
    public synchronized int countPendingConfirmations(CommandSource sender)
    {
        Preconditions.checkNotNull(sender);
        Queue<ConfirmResult> pendingConfirmations = this.pendingConfirmations.get(sender.getIdentifier());
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
    public synchronized ConfirmResult getLastPendingConfirmation(CommandSource sender)
    {
        if (countPendingConfirmations(sender) < 1)
        {
            return null;
        }

        Queue<Pair<Module, UUID>> confirmationTimeoutTasks = this.confirmationTimeoutTasks.get(sender.getIdentifier());
        if (confirmationTimeoutTasks == null)
        {
            confirmationTimeoutTasks = new LinkedList<>();
        }
        Pair<Module, UUID> pair = confirmationTimeoutTasks.poll();
        this.confirmationTimeoutTasks.put(sender.getIdentifier(), confirmationTimeoutTasks);
        taskManager.cancelTask(pair.getLeft(), pair.getRight());

        Queue<ConfirmResult> pendingConfirmations = this.pendingConfirmations.get(sender.getIdentifier());
        if (pendingConfirmations == null)
        {
            pendingConfirmations = new LinkedList<>();
        }
        this.pendingConfirmations.put(sender.getIdentifier(), pendingConfirmations);
        return pendingConfirmations.poll();
    }

    /**
     * Class to remove tasks that have timed out, and notify the CommandSender
     */
    private class ConfirmationTimeoutTask implements Runnable
    {

        private final CommandSource sender;

        private ConfirmationTimeoutTask(CommandSource sender)
        {
            this.sender = sender;
        }

        @Override
        public void run()
        {
            i18n.sendTranslated(sender, NEGATIVE, "Your confirmation timed out....");
            pendingConfirmations.remove(sender.getIdentifier());
        }
    }
}
