package de.cubeisland.cubeengine.log.lookup;

import java.util.TreeSet;

public class ChestLookup
{
    private TreeSet<ChestLog> entries = new TreeSet<ChestLog>();

    public void addEntry(ChestLog logentry)
    {
        this.entries.add(logentry);
    }

    public TreeSet<ChestLog> getEntries()
    {
        return this.entries;
    }
}
