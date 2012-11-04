package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import de.cubeisland.cubeengine.log.storage.AbstractLog;
import de.cubeisland.cubeengine.log.storage.blocks.BlockLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * This class represents an instance of a lookup.
 * (The lookup gets bound to the user executing the lookup)
 */
public class Lookup
{
    boolean showCoords;
    private List<BlockLog> blocklogs;
    //private List<ChatLog> chatlogs;
    //private List<ChestLog> chestlogs;

    //TODO possibility to "give" the lookup to an other User
    public void printLookup(User user)
    {
        //TODO sort by timestamp (or other)
        //TODO print
        for (AbstractLog log : blocklogs)
        {
            StringBuilder sb = new StringBuilder();
            if (log instanceof BlockLog)
            {
                BlockLog blog = (BlockLog)log;
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                sb.append(sdf.format(new Date(log.getTimeStamp()))).append(" - ");
                if (blog.isBlockBreak())
                {
                    //TODO getCausedBy -> String func in AbstractLog
                    if (blog.isCausedByPlayer())
                    {
                        sb.append(blog.getUser().getName());
                    }
                    sb.append(" break ").append(MaterialMatcher.get().getNameFor(new ItemStack(blog.oldBlock.mat)));
                }
                else if (blog.isBlockPlace())
                {
                    //TODO
                }
                else if (blog.isBlockRePlace())
                {
                    //TODO
                }
                if (this.showCoords)
                {
                    sb.append(blog.x).append(" ").append(blog.y).append(" ").append(blog.z);
                }
            }
            //TODO else other logtypes
            user.sendMessage(sb.toString());//TODO translateable
        }
    }

    public Lookup filterSelection()//TODO the selection as param / & / how to get the selection?
    {
        List<BlockLog> newBlockLogs = new ArrayList<BlockLog>();
        for (BlockLog blocklog : blocklogs)
        {
            //TODO if in selection
            if (1 == 0)
            {
                newBlockLogs.add(blocklog);
            }
        }
        //TODO same with chatlogs
        blocklogs = newBlockLogs;
        return this;
    }

    public Lookup filterWorld(World world)
    {
        List<BlockLog> newBlockLogs = new ArrayList<BlockLog>();
        for (BlockLog blocklog : blocklogs)
        {
            if (blocklog.getLocation().getWorld().equals(world))
            {
                newBlockLogs.add(blocklog);
            }
        }
        //TODO same with chatlogs
        blocklogs = newBlockLogs;
        return this;
    }

    public Lookup filterItemType(ItemStack[] blocks)//or item
    {
        List<BlockLog> newBlockLogs = new ArrayList<BlockLog>();
        for (BlockLog blocklog : blocklogs)
        {
            for (ItemStack item : blocks)
            {
                if (blocklog.oldBlock.mat.equals(item.getType())
                    || blocklog.newBlock.mat.equals(item.getType()))
                {
                    newBlockLogs.add(blocklog);
                }
            }
        }
        //TODO same with chestlogs
        blocklogs = newBlockLogs;
        return this;
    }

    public Lookup filterUsers(User[] names)
    {
        List<BlockLog> newBlockLogs = new ArrayList<BlockLog>();
        for (BlockLog blocklog : blocklogs)
        {
            for (User user : names)
            {
                if (blocklog.causeID == user.key)
                {
                    newBlockLogs.add(blocklog);
                }
            }
        }
        //TODO same with chatlogs
        blocklogs = newBlockLogs;
        return this;
    }

    public Lookup filterBlockBreak()
    {
        List<BlockLog> newBlockLogs = new ArrayList<BlockLog>();
        for (BlockLog blocklog : blocklogs)
        {
            if (blocklog.isBlockBreak())
            {
                newBlockLogs.add(blocklog);
            }
        }
        blocklogs = newBlockLogs;
        return this;
    }

    public Lookup filterBlockPlace()
    {
        List<BlockLog> newBlockLogs = new ArrayList<BlockLog>();
        for (BlockLog blocklog : blocklogs)
        {
            if (blocklog.isBlockPlace() || blocklog.isBlockRePlace())
            {
                newBlockLogs.add(blocklog);
            }
        }
        blocklogs = newBlockLogs;
        return this;
    }

    public Lookup filterTimeSince(Date date)
    {
        return this.filterTimeFrame(date, new Date(System.currentTimeMillis()));
    }

    public Lookup filterTimeOlder(Date date)
    {
        return this.filterTimeFrame(new Date(0), date);
    }

    public Lookup filterTimeFrame(Date date1, Date date2)
    {
        List<BlockLog> newBlockLogs = new ArrayList<BlockLog>();
        for (BlockLog blocklog : blocklogs)
        {
            if (blocklog.getTimeStamp() > date1.getTime() && blocklog.getTimeStamp() < date2.getTime())
            {
                newBlockLogs.add(blocklog);
            }
        }
        blocklogs = newBlockLogs;
        //TODO same for chatlogs
        return this;
    }
}
