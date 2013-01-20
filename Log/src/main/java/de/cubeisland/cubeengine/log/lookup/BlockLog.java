package de.cubeisland.cubeengine.log.lookup;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.BlockData;
import de.cubeisland.cubeengine.log.storage.LogManager;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.bukkit.Location;

public class BlockLog implements Comparable<BlockLog>
{
    public final long key;
    public final int action;
    public final Timestamp date;
    public final Location loc;
    public final Long causer;
    public final BlockData oldBlock;
    public final BlockData newBlock;

    public BlockLog(long key, int action, Timestamp date, Location loc, Long causer, BlockData oldBlock, BlockData newBlock)
    {
        this.key = key;
        this.action = action;
        this.date = date;
        this.loc = loc;
        this.causer = causer;
        this.oldBlock = oldBlock;
        this.newBlock = newBlock;
    }

    @Override
    public int compareTo(BlockLog o)
    {
        return (int) (this.date.getTime() - o.date.getTime());
    }

    public String getCauser()
    {
        if (causer > 0)
        {
            User user = CubeEngine.getUserManager().getUser(causer);
            if (user == null)
            {
                return "unknown Player";
            }
            return user.getName();
        }
        else
        {
            switch (causer.intValue())
            {
                case -1:
                    return "#player";
                case -2:
                    return "#Lava";
                case -3:
                    return "#Water";
                case -4:
                    return "#Explosion";
                case -5:
                    return "#Fire";
                case -6:
                    return "#Enderman";
                case -7:
                    return "#Fade/Form";
                case -8:
                    return "#Decay/Grow";
                case -9:
                    return "#Wither";
            }
        }
        throw new IllegalStateException("Illegal Blocklog type!");
    }

    public String format(User user, boolean showLoc)
    {
        ArrayList<Object> list = new ArrayList<Object>();
        String message = "%s &2%s ";
        SimpleDateFormat sdf = new SimpleDateFormat();
        list.add(sdf.format(this.date));
        list.add(this.getCauser());
        if (action == LogManager.BLOCK_BREAK)
        {
            message += "&edestroyed &6%s";
            list.add(this.oldBlock.toString());
        }
        else if (action == LogManager.BLOCK_PLACE)
        {
            if (this.oldBlock == null || this.oldBlock.mat.getId() == 0)
            {
                message += "&eplaced &6%s";
                list.add(this.newBlock.toString());
            }
            else
            {
                message += "&ereplaced &6%s &ewith &6%s";
                list.add(this.oldBlock.toString());
                list.add(this.newBlock.toString());
            }
        }
        else if (action == LogManager.BLOCK_CHANGE)
        {
            switch (this.oldBlock.mat)
            {
                case WOODEN_DOOR:
                case TRAP_DOOR:
                case FENCE_GATE:
                    if ((this.newBlock.data & 0x4) == 0x4)
                    {
                        message += "&eopened &6%s";
                    }
                    else
                    {
                        message += "&eclosed &6%s";
                    }
                    list.add(this.oldBlock.mat.toString()); //TODO reversematch
                    break;
                case LEVER:
                    if ((this.newBlock.data & 0x8) == 0x8)
                    {
                        message += "&eactivated &6%s";
                    }
                    else
                    {
                        message += "&edeactivated &6%s";
                    }
                    list.add(this.oldBlock.mat.toString()); //TODO reversematch
                    break;
                case STONE_BUTTON:
                case WOOD_BUTTON:
                    message += "&epressed &6%s";
                    list.add(this.oldBlock.mat.toString()); //TODO reversematch
                    break;
                case CAKE_BLOCK: // data: remaining slices
                    message += "&eeat &6%s";
                    list.add(this.oldBlock.toString()); //TODO reversematch
                    break;
                case NOTE_BLOCK:
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                    message += "&eadjusted &6%s";
                    list.add(this.oldBlock.toString()); //TODO reversematch
                    break;
            }
        }
        if (showLoc)
        {
            message += " &eat &f(&6%d,%d,%d&f) &ein &6%s";
            list.add(this.loc.getBlockX());
            list.add(this.loc.getBlockY());
            list.add(this.loc.getBlockZ());
            list.add(this.loc.getWorld().getName());
        }
        return ChatFormat.parseFormats(String.format(message, list.toArray())); //TODO translate and parse color too
    }
}
