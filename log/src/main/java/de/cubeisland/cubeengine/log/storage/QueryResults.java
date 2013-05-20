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
package de.cubeisland.cubeengine.log.storage;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import de.cubeisland.cubeengine.core.user.User;

public class QueryResults
{
    private TreeSet<LogEntry> logEntries = new TreeSet<LogEntry>();

    public void show(User user, QueryParameter parameter)
    {
        user.updateInventory();
        if (this.logEntries.isEmpty())
        {
            parameter.showNoLogsFound(user);
            return;
        }
        System.out.print("Showing " + this.logEntries.size() + " logentries to " + user.getName());
        user.sendTranslated("&aFound %d logs:", this.logEntries.size());
        Iterator<LogEntry> entries = this.logEntries.iterator();
        // compressing data: //TODO add if it should be compressed or not
        LogEntry entry = entries.next();
        LinkedHashSet<LogEntry> compressedEntries = new LinkedHashSet<LogEntry>();
        compressedEntries.add(entry); // add first entry
        while (entries.hasNext())
        {
            LogEntry next = entries.next();
            if (entry.isSimilar(next)) // can be compressed ?
            {
                entry.attach(next);
            }
            else // no more compression -> move on to next entry
            {
                entry = next;
                compressedEntries.add(entry);
            }
        }
        //TODO pages
        for (LogEntry logEntry : compressedEntries)
        {
            logEntry.actionType.showLogEntry(user,parameter,logEntry);
        }
    }

    public void addResult(LogEntry logEntry)
    {
        this.logEntries.add(logEntry);
    }
}
