package de.cubeisland.cubeengine.log.lookup;

import java.util.TreeSet;

public class MessageLookup
{
    private TreeSet<MessageLog> entries = new TreeSet<MessageLog>();

    public void addEntry(MessageLog logentry)
    {
        this.entries.add(logentry);
    }

    public TreeSet<MessageLog> getEntries()
    {
        return this.entries;
    }
}
