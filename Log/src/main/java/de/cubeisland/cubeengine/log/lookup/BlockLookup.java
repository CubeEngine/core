package de.cubeisland.cubeengine.log.lookup;

import java.util.TreeSet;

public class BlockLookup
{
    private TreeSet<BlockLog> entries = new TreeSet<BlockLog>();

    public void addEntry(BlockLog logentry)
    {
        this.entries.add(logentry);
    }

    public TreeSet<BlockLog> getEntries()
    {
        return this.entries;
    }
}
