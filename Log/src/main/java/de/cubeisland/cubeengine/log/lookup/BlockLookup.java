package de.cubeisland.cubeengine.log.lookup;

import java.util.Collection;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class BlockLookup
{
    private Collection<BlockLog> entries = new TreeSet<BlockLog>();

    public void addEntry(BlockLog logentry)
    {
        if (this.entries.add(logentry))
        {
            System.out.println(entries.size() + ": " + logentry.date + " : " + logentry.getCauser() + " : "
                    + logentry.loc.getWorld().getName() + "("
                    + logentry.loc.getBlockX() + "," + logentry.loc.getBlockY() + "," + logentry.loc.getBlockZ() + ")"
                    + logentry.oldBlock + "-->" + logentry.newBlock);
        }
    }
}
