package de.cubeisland.cubeengine.log.lookup;

import java.util.TreeSet;

public class KillLookup
{
    private TreeSet<KillLog> entries = new TreeSet<KillLog>();

    public void addEntry(KillLog logentry)
    {
        this.entries.add(logentry);
    }

    public TreeSet<KillLog> getEntries()
    {
        return this.entries;
    }
}
