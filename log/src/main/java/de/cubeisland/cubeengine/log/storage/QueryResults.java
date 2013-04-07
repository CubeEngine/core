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
        if (this.logEntries.isEmpty())
        {
            parameter.showNoLogsFound(user);
            return;
        }
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
