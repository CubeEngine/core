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
package de.cubeisland.engine.log.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Location;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.logaction.kill.MonsterDeath;

public class QueryResults
{
    private final Lookup lookup;
    private final Module module;
    private final TreeSet<LogEntry> logEntries = new TreeSet<>();

    public QueryResults(Lookup lookup, Module module)
    {
        this.lookup = lookup;
        this.module = module;
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
        if (show.pagelimit == -1)
        {
            show.pagelimit = this.logEntries.size();
        }
        if (show.pagelimit > 80) // prevent showing too much logs
        {
            show.pagelimit = 80;
        }

        int totalPages = (this.logEntries.size()+show.pagelimit-1) / show.pagelimit; // rounded up
        user.sendTranslated("&6%d&a distinct logs (&6%d&a pages)", this.logEntries.size(), totalPages);
        Iterator<LogEntry> entries = this.logEntries.iterator();
        LogEntry entry = entries.next();
        LogEntry lastAttach = entry;
        TreeSet<LogEntry> compressedEntries = new TreeSet<>();
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
        else if (compressedEntries.size() - ((show.page-1) * show.pagelimit) < show.pagelimit)
        {
            showing = compressedEntries.size() - ((show.page-1) * show.pagelimit);
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
        NavigableSet<LogEntry> navigableSet;
        if (show.reverseOrder)
        {
            navigableSet = compressedEntries.descendingSet();
        }
        else
        {
            navigableSet = compressedEntries;
        }
		CubeEngine.getLog().info("Showing {}/{}/{} logentries to {} (page {})", showing, navigableSet.size(), this.logEntries.size() , user.getName(), show.page);
        for (LogEntry logEntry : navigableSet)
        {
            if (cpage == show.page)
            {
                try
                {
                    logEntry.getActionType().showLogEntry(user,parameter,logEntry, show);
                }
                catch (Exception e)
                {
                    module.getLog().error(e, "An error occurred while showing LogEntries!");
                    user.sendTranslated("&4Internal Error! Could not show LogEntry");
                }
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
        Map<Location,LogEntry> finalBlock = new HashMap<>();
        Map<Location,LinkedList<LogEntry>> blockChanges = new HashMap<>();
        TreeSet<LogEntry> filteredLogs = new TreeSet<>();
        for (LogEntry logEntry : this.logEntries.descendingSet())
        {
            if (logEntry.getActionType().canRollback()) // can rollback
            {
                if (logEntry.getActionType() instanceof MonsterDeath && !this.lookup.getQueryParameter().containsAction(logEntry.getActionType()))
                {
                    continue; // ignoring Monster-respawning when not explicitly wanted
                }
                if (logEntry.getActionType().isBlockBound())
                {
                    if (logEntry.getActionType().isStackable())
                    {
                        LinkedList<LogEntry> changes = blockChanges.get(logEntry.getLocation());
                        if (changes == null)
                        {
                            changes = new LinkedList<>();
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
        // Finished filtering! Merge back together...
        for (LinkedList<LogEntry> entries : blockChanges.values())
        {
            filteredLogs.addAll(entries);
        }
        filteredLogs.addAll(finalBlock.values());
        // Start Rollback 1st Round
        Set <LogEntry> rollbackRound2 = new LinkedHashSet<>();
        for (LogEntry logEntry : filteredLogs.descendingSet()) // Rollback normal blocks
        {
            if (logEntry.getWorld() == null)
            {
                this.module.getLog().warn("LogEntry #{} belongs to a deleted world!", logEntry.getId().longValue());
                continue;
            }
            if (!logEntry.rollback(attachment, false, preview)) // Rollback failed (cannot set yet (torches etc)) try again later
            {
                rollbackRound2.add(logEntry);
            }
        }
        ShowParameter show = new ShowParameter();
        // Start Rollback 2nd Round (Attachables etc.)
        for (LogEntry logEntry : rollbackRound2) // Rollback attached blocks
        {
            if (!logEntry.rollback(attachment, true, preview))
            {
                attachment.getHolder().sendTranslated("&cCould not Rollback:");
                logEntry.getActionType().showLogEntry(attachment.getHolder(), null, logEntry, show);
                CubeEngine.getLog().warn("Could not rollback!");
            }
        }
    }

    public void redo(LogAttachment attachment, boolean preview)
    {
        // Find the newest entry at a location
        Map<Location,LogEntry> finalBlock = new HashMap<>();
        Map<Location,LinkedList<LogEntry>> blockChanges = new HashMap<>();
        TreeSet<LogEntry> filteredLogs = new TreeSet<>();
        for (LogEntry logEntry : this.logEntries)
        {
            if (logEntry.getActionType().canRedo()) // can redo
            {
                if (logEntry.getActionType().isBlockBound())
                {
                    if (logEntry.getActionType().isStackable())
                    {
                        LinkedList<LogEntry> changes = blockChanges.get(logEntry.getLocation());
                        if (changes == null)
                        {
                            changes = new LinkedList<>();
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
        // Finished filtering! Merge back together...
        for (LinkedList<LogEntry> entries : blockChanges.values())
        {
            filteredLogs.addAll(entries);
        }
        filteredLogs.addAll(finalBlock.values());
        // Start Rollback 1st Round
        Set <LogEntry> rollbackRound2 = new LinkedHashSet<>();
        for (LogEntry logEntry : filteredLogs.descendingSet()) // Rollback normal blocks
        {
            if (logEntry.getWorld() == null)
            {
                this.module.getLog().warn("LogEntry #{} belongs to a deleted world!", logEntry.getId().longValue());
                continue;
            }
            if (!logEntry.redo(attachment, false, preview)) // Redo failed (cannot set yet (torches etc)) try again later
            {
                rollbackRound2.add(logEntry);
            }
        }
        ShowParameter show = new ShowParameter();
        // Start Rollback 2nd Round (Attachables etc.)
        for (LogEntry logEntry : rollbackRound2) // Rollback attached blocks
        {
            if (!logEntry.redo(attachment, true, preview))
            {
                attachment.getHolder().sendTranslated("&cCould not Redo:");
                logEntry.getActionType().showLogEntry(attachment.getHolder(), null, logEntry, show);
                CubeEngine.getLog().warn("Could not redo!");
            }
        }
    }
}
