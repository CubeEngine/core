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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.player.SignChange;

public class QueryResults
{
    private TreeSet<LogEntry> logEntries = new TreeSet<LogEntry>();

    public void show(User user, QueryParameter parameter, int page)
    {
        user.updateInventory();

        if (this.logEntries.isEmpty())
        {
            parameter.showNoLogsFound(user);
            return;
        }
        System.out.print("Showing " + this.logEntries.size() + " logentries (limit:" + parameter.getPageLimit() + ")to " + user.getName());
        int pageLimit = parameter.getPageLimit();
        if (pageLimit == -1)
        {
            pageLimit = this.logEntries.size();
        }
        int totalPages = (this.logEntries.size()+pageLimit-1) / pageLimit; // rounded up
        user.sendTranslated("&6%d&a distinct logs (&6%d&a pages)", this.logEntries.size(), totalPages);
        Iterator<LogEntry> entries = this.logEntries.iterator();
        // compressing data: //TODO add if it should be compressed or not
        LogEntry entry = entries.next();
        LogEntry lastAttach = entry;
        LinkedHashSet <LogEntry> compressedEntries = new LinkedHashSet<LogEntry>();
        compressedEntries.add(entry); // add first entry
        while (entries.hasNext())
        {
            LogEntry next = entries.next();
            if (lastAttach.isSimilar(next)) // can be compressed ?
            {
                entry.attach(next);
                lastAttach = next;
            }
            else // no more compression -> move on to next entry
            {
                entry = next;
                lastAttach = entry;
                compressedEntries.add(entry);
            }
        }
        if (compressedEntries.size() < this.logEntries.size())
        {
            totalPages = (compressedEntries.size()+pageLimit-1) / pageLimit; // rounded up
            user.sendTranslated("&aCompressed into &6%d&a logs! (&6%d&a pages)", compressedEntries.size(), totalPages);
        }
        if (page > totalPages)
        {
            return;
        }
        if (page == 1)
        {
            user.sendTranslated("&aShowing %d most recent logs:", pageLimit);
        }
        else
        {
            user.sendTranslated("&aShowing %d logs (Page %d):", pageLimit, page);
        }
        int i = 0;
        int cpage = 1;
        for (LogEntry logEntry : compressedEntries)
        {
            if (cpage == page)
            {
                logEntry.actionType.showLogEntry(user,parameter,logEntry);
            }
            i++;
            if (i % pageLimit == 0)
            {
                cpage++;
            }
        }
    }

    public void addResult(LogEntry logEntry)
    {
        this.logEntries.add(logEntry);
    }

    public void rollback(User user)
    {
        // Find the oldest entry at a location
        Map<Location,LogEntry> finalBlock = new HashMap<Location, LogEntry>();
        Map<Location,LinkedList<LogEntry>> blockChanges = new HashMap<Location, LinkedList<LogEntry>>();
        TreeSet<LogEntry> filteredLogs = new TreeSet<LogEntry>();
        for (LogEntry logEntry : this.logEntries.descendingSet())
        {
            if (logEntry.actionType.canRollback()) // can rollback
            {
                if (logEntry.actionType instanceof BlockActionType) // If blockaction ignore
                {
                    // TODO chest changes and other that can be stacked into one location
                    if (logEntry.actionType instanceof SignChange)
                    {
                        LinkedList<LogEntry> changes = blockChanges.get(logEntry.getLocation());
                        if (changes == null)
                        {
                            changes = new LinkedList<LogEntry>();
                            blockChanges.put(logEntry.getLocation(), changes);
                        }
                        changes.add(logEntry);
                    }
                    else
                    {
                        blockChanges.remove(logEntry.getLocation()); // Clear blockChanges when new final block
                        finalBlock.put(logEntry.getLocation(), logEntry);
                    }
                }
                else
                {
                    filteredLogs.add(logEntry); // Not a block change at the location -> do rollback
                }
            }
        }
        for (LinkedList<LogEntry> entries : blockChanges.values())
        {
            // TODO interface of ActionType if logs are "stackable"
            // similar to is Attachable
            if (entries.getLast().actionType instanceof SignChange)
            {
                filteredLogs.add(entries.getLast());
            }
            else
            {
                filteredLogs.addAll(entries); // TODO filter correctly (container changes stack together)
            }
        }
        filteredLogs.addAll(finalBlock.values());
        Set<LogEntry> rollbackRound2 = new LinkedHashSet<LogEntry>();
        for (LogEntry logEntry : filteredLogs.descendingSet()) // Rollback normal blocks
        {
            // Can Rollback
            if (!logEntry.rollback(user, false)) // Rollback failed (cannot set yet (torches etc)) try again later
            {
                rollbackRound2.add(logEntry);
            }
        }
        for (LogEntry logEntry : rollbackRound2) // Rollback attached blocks
        {
            if (!logEntry.rollback(user, true))
            {
                user.sendTranslated("&cCould not Rollback:");
                logEntry.actionType.showLogEntry(user, null, logEntry);
                System.out.print("Could not Rollback!");
            }
        }
    }
}
