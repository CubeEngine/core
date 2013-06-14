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
package de.cubeisland.cubeengine.shout.announce.announcer;

import de.cubeisland.cubeengine.core.task.TaskManager;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.receiver.Receiver;

public class MessageTask implements Runnable
{
    private final TaskManager tm;
    private final Receiver receiver;
    private int runs;
    private int nextExecution;

    public MessageTask(TaskManager taskManager, Receiver receiver)
    {
        this.tm = taskManager;
        this.receiver = receiver;
        this.runs = 0;
        this.nextExecution = 0;
    }

    @Override
    public void run()
    {
        if (this.runs == this.nextExecution)
        {
            Pair<Announcement, Integer> pair = receiver.getNextDelayAndAnnouncement();
            if (pair != null && pair.getLeft() != null && pair.getRight() != null)
            {
                this.tm.callSync(new SenderTask(pair.getLeft().getMessage(receiver.getLocale()), this.receiver));
                this.nextExecution = this.runs + pair.getRight();
            }
            else
            {
                this.nextExecution = this.runs + 1;
            }
        }
        ++this.runs;
    }
}
