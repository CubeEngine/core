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

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.BaseAction.Coordinate;
import de.cubeisland.engine.log.action.Redoable;
import de.cubeisland.engine.log.action.Rollbackable;
import de.cubeisland.engine.log.action.death.DeathMonster;

public class QueryResults
{
    private final Lookup lookup;
    private final Module module;
    private final TreeSet<BaseAction> logEntries = new TreeSet<>();

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

        int totalPages = (this.logEntries.size() + show.pagelimit - 1) / show.pagelimit; // rounded up
        user.sendTranslated(MessageType.POSITIVE, "{amount} distinct logs ({amount} pages)", this.logEntries.size(),
                            totalPages);
        Iterator<BaseAction> entries = this.logEntries.iterator();
        BaseAction entry = entries.next();
        BaseAction lastAttach = entry;
        TreeSet<BaseAction> compressedEntries = new TreeSet<>();
        if (show.compress)
        {
            compressedEntries.add(entry); // add first entry
            while (entries.hasNext())
            {
                BaseAction next = entries.next();
                if (lastAttach.canAttach(next)) // can be compressed ?
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
                totalPages = (compressedEntries.size() + show.pagelimit - 1) / show.pagelimit; // rounded up
                if (totalPages > 1)
                {
                    user.sendTranslated(MessageType.POSITIVE, "Compressed into {amount} logs! ({amount} pages)",
                                        compressedEntries.size(), totalPages);
                }
                else
                {
                    user.sendTranslated(MessageType.POSITIVE, "Compressed into {amount} logs!",
                                        compressedEntries.size());
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
        else if (compressedEntries.size() - ((show.page - 1) * show.pagelimit) < show.pagelimit)
        {
            showing = compressedEntries.size() - ((show.page - 1) * show.pagelimit);
        }
        if (show.page == 1)
        {
            user.sendTranslated(MessageType.POSITIVE, "Showing {integer} most recent logs:", showing);
        }
        else
        {
            user.sendTranslated(MessageType.POSITIVE, "Showing {integer} logs (Page {integer}):", showing, show.page);
        }
        int i = 0;
        int cpage = 1;
        NavigableSet<BaseAction> navigableSet;
        if (show.reverseOrder)
        {
            navigableSet = compressedEntries.descendingSet();
        }
        else
        {
            navigableSet = compressedEntries;
        }
        CubeEngine.getLog().info("Showing {}/{}/{} logentries to {} (page {})", showing, navigableSet.size(),
                                 this.logEntries.size(), user.getName(), show.page);
        for (BaseAction action : navigableSet)
        {
            if (cpage == show.page)
            {
                try
                {
                    action.showAction(user, show);
                }
                catch (Exception e)
                {
                    module.getLog().error(e, "An error occurred while showing LogEntries!");
                    user.sendTranslated(MessageType.CRITICAL, "Internal Error! Could not show LogEntry");
                }
            }
            i++;
            if (i % show.pagelimit == 0)
            {
                cpage++;
            }
            if (action.hasAttached())
            {
                action.getAttached().clear();
            }
        }
    }

    public void addResult(BaseAction logEntry)
    {
        this.logEntries.add(logEntry);
    }

    public void rollback(LogAttachment attachment, boolean preview)
    {
        // Find the oldest entry at a location
        Map<Coordinate, Rollbackable> finalBlock = new HashMap<>();
        Map<Coordinate, LinkedList<Rollbackable>> blockChanges = new HashMap<>();
        TreeSet<Rollbackable> filteredLogs = new TreeSet<>();
        for (BaseAction logEntry : this.logEntries.descendingSet())
        {
            if (logEntry instanceof Rollbackable) // can rollback
            {
                if (logEntry.coord.getWorld() == null)
                {
                    continue;
                }
                if (logEntry instanceof DeathMonster && !this.lookup.getQueryParameter().containsAction(DeathMonster.class))
                {
                    continue; // ignoring Monster-respawning when not explicitly wanted
                }
                if (((Rollbackable)logEntry).isBlockBound())
                {
                    if (((Rollbackable)logEntry).isStackable())
                    {
                        LinkedList<Rollbackable> changes = blockChanges.get(logEntry.coord);
                        if (changes == null)
                        {
                            changes = new LinkedList<>();
                            blockChanges.put(logEntry.coord, changes);
                        }
                        changes.add((Rollbackable)logEntry);
                    }
                    else
                    {
                        blockChanges.remove(logEntry.coord); // Clear blockChanges when new final block
                        finalBlock.put(logEntry.coord, (Rollbackable)logEntry);
                    }
                }
                else
                {
                    filteredLogs.add((Rollbackable)logEntry); // Not a block change at the location -> do rollback
                }
            }
        }
        // Finished filtering! Merge back together...
        for (LinkedList<Rollbackable> entries : blockChanges.values())
        {
            filteredLogs.addAll(entries);
        }
        filteredLogs.addAll(finalBlock.values());
        // Start Rollback 1st Round
        Set<Rollbackable> rollbackRound2 = new LinkedHashSet<>();
        // Rollback normal blocks
        for (Rollbackable logEntry : filteredLogs.descendingSet())
        {
            if (!logEntry.rollback(attachment, false, preview))
            {
                // Rollback failed (cannot set yet (torches etc)) try again later
                rollbackRound2.add(logEntry);
            }
        }
        ShowParameter show = new ShowParameter();
        // Start Rollback 2nd Round (Attachables etc.)
        for (Rollbackable logEntry : rollbackRound2) // Rollback attached blocks
        {
            if (!logEntry.rollback(attachment, true, preview))
            {
                attachment.getHolder().sendTranslated(MessageType.NEGATIVE, "Could not Rollback:");
                ((BaseAction)logEntry).showAction(attachment.getHolder(), show);
                CubeEngine.getLog().warn("Could not rollback!");
            }
        }
    }

    public void redo(LogAttachment attachment, boolean preview)
    {
        // Find the newest entry at a location
        Map<Coordinate, Redoable> finalBlock = new HashMap<>();
        Map<Coordinate, LinkedList<Redoable>> blockChanges = new HashMap<>();
        TreeSet<Redoable> filteredLogs = new TreeSet<>();
        for (BaseAction logEntry : this.logEntries)
        {
            if (logEntry instanceof Redoable) // can redo
            {
                if (logEntry.coord.getWorld() == null)
                {
                    continue;
                }
                if (((Redoable)logEntry).isBlockBound())
                {
                    if (((Redoable)logEntry).isStackable())
                    {
                        LinkedList<Redoable> changes = blockChanges.get(logEntry.coord);
                        if (changes == null)
                        {
                            changes = new LinkedList<>();
                            blockChanges.put(logEntry.coord, changes);
                        }
                        changes.add((Redoable)logEntry);
                    }
                    else
                    {
                        blockChanges.remove(logEntry.coord); // Clear blockChanges when new final block
                        finalBlock.put(logEntry.coord, (Redoable)logEntry);
                    }
                }
                else
                {
                    filteredLogs.add((Redoable)logEntry); // Not a block change at the location -> do rollback
                }
            }
        }
        // Finished filtering! Merge back together...
        for (LinkedList<Redoable> entries : blockChanges.values())
        {
            filteredLogs.addAll(entries);
        }
        filteredLogs.addAll(finalBlock.values());
        // Start Rollback 1st Round
        Set<Redoable> rollbackRound2 = new LinkedHashSet<>();
        for (Redoable logEntry : filteredLogs.descendingSet()) // Rollback normal blocks
        {
            if (!logEntry.redo(attachment, false,
                               preview)) // Redo failed (cannot set yet (torches etc)) try again later
            {
                rollbackRound2.add(logEntry);
            }
        }
        ShowParameter show = new ShowParameter();
        // Start Rollback 2nd Round (Attachables etc.)
        for (Redoable logEntry : rollbackRound2) // Rollback attached blocks
        {
            if (!logEntry.redo(attachment, true, preview))
            {
                attachment.getHolder().sendTranslated(MessageType.NEGATIVE, "Could not Redo:");
                ((BaseAction)logEntry).showAction(attachment.getHolder(), show);
                CubeEngine.getLog().warn("Could not redo!");
            }
        }
    }
}
