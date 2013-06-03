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
import de.cubeisland.cubeengine.log.LogAttachment;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.player.SignChange;
import de.cubeisland.cubeengine.log.action.logaction.kill.MonsterDeath;

public class QueryResults
{
    private Lookup lookup;
    private TreeSet<LogEntry> logEntries = new TreeSet<LogEntry>();

    public QueryResults(Lookup lookup)
    {
        this.lookup = lookup;
    }

    @SuppressWarnings("deprecation")
    public void show(User user, QueryParameter parameter, ShowParameter show)
    {
        user.updateInventory();

        if (this.logEntries.isEmpty())
        {
            parameter.showNoLogsFound(user);
            return;
        }
        System.out.print("Showing " + this.logEntries.size() + " logentries to " + user.getName());
        if (show.pagelimit == -1)
        {
            show.pagelimit = this.logEntries.size();
        }
        int totalPages = (this.logEntries.size()+show.pagelimit-1) / show.pagelimit; // rounded up
        user.sendTranslated("&6%d&a distinct logs (&6%d&a pages)", this.logEntries.size(), totalPages);
        Iterator<LogEntry> entries = this.logEntries.iterator();
        LogEntry entry = entries.next();
        LogEntry lastAttach = entry;
        TreeSet<LogEntry> compressedEntries = new TreeSet<LogEntry>();
        if (show.compress)
        {
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
                totalPages = (compressedEntries.size()+show.pagelimit-1) / show.pagelimit; // rounded up
                if (totalPages > 1)
                {
                    user.sendTranslated("&aCompressed into &6%d&a logs! (&6%d&a pages)", compressedEntries.size(), totalPages);
                }
                else
                {
                    user.sendTranslated("&aCompressed into &6%d&a logs!", compressedEntries.size());
                }
            }
        }
        else
        {
            compressedEntries.addAll(this.logEntries);
        }
        if (show.page > totalPages)
        {
            return;
        }
        int showing = show.pagelimit;
        if (showing > compressedEntries.size())
        {
            showing = compressedEntries.size();
        }
        else if (compressedEntries.size() - (show.page * show.pagelimit) < show.pagelimit)
        {
            showing = compressedEntries.size() - (show.page * show.pagelimit);
        }
        if (show.page == 1)
        {
            user.sendTranslated("&aShowing %d most recent logs:", showing);
        }
        else
        {
            user.sendTranslated("&aShowing %d logs (Page %d):", showing, show.page);
        }
        int i = 0;
        int cpage = 1;
        for (LogEntry logEntry : compressedEntries.descendingSet())
        {
            if (cpage == show.page)
            {
                logEntry.actionType.showLogEntry(user,parameter,logEntry, show);
            }
            i++;
            if (i % show.pagelimit == 0)
            {
                cpage++;
            }
            logEntry.clearAttached();
        }
    }

    public void addResult(LogEntry logEntry)
    {
        this.logEntries.add(logEntry);
    }

    public void rollback(LogAttachment attachment, boolean preview)
    {
        // Find the oldest entry at a location
        Map<Location,LogEntry> finalBlock = new HashMap<Location, LogEntry>();
        Map<Location,LinkedList<LogEntry>> blockChanges = new HashMap<Location, LinkedList<LogEntry>>();
        TreeSet<LogEntry> filteredLogs = new TreeSet<LogEntry>();
        for (LogEntry logEntry : this.logEntries.descendingSet())
        {
            if (logEntry.actionType.canRollback()) // can rollback
            {
                if (logEntry.actionType instanceof MonsterDeath && !this.lookup.getQueryParameter().containsAction(logEntry.actionType))
                {
                    continue;
                }
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
        Set <LogEntry> rollbackRound2 = new LinkedHashSet<LogEntry>();
        for (LogEntry logEntry : filteredLogs.descendingSet()) // Rollback normal blocks
        {
            // Can Rollback
            if (!logEntry.rollback(attachment, false, preview)) // Rollback failed (cannot set yet (torches etc)) try again later
            {
                rollbackRound2.add(logEntry);
            }
        }
        ShowParameter show = new ShowParameter();
        for (LogEntry logEntry : rollbackRound2) // Rollback attached blocks
        {
            if (!logEntry.rollback(attachment, true, preview))
            {
                attachment.getHolder().sendTranslated("&cCould not Rollback:");
                logEntry.actionType.showLogEntry(attachment.getHolder(), null, logEntry, show);
                System.out.print("Could not Rollback!");
            }
        }
    }
}
