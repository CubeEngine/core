package de.cubeisland.cubeengine.log.lookup;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.log.storage.BlockData;
import de.cubeisland.cubeengine.log.storage.LogManager;
import org.bukkit.Location;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class BlockLog implements Comparable<BlockLog>
{
    public final long key;
    public final int action;
    public final Timestamp date;
    public final Location loc;
    public final Long causer;
    public final BlockData oldBlock;
    public final BlockData newBlock;
    public final String[] oldLines;
    public final String[] newLines;

    public BlockLog(long key, int action, Timestamp date, Location loc, Long causer, BlockData oldBlock, BlockData newBlock)
    {
        this.key = key;
        this.action = action;
        this.date = date;
        this.loc = loc;
        this.causer = causer;
        this.oldBlock = oldBlock;
        this.newBlock = newBlock;
        this.oldLines = null;
        this.newLines = null;
    }

    public BlockLog(long key, int action, Timestamp date, Location loc, Long causer, String[] oldLines, String[] newLines)
    {
        this.key = key;
        this.action = action;
        this.date = date;
        this.loc = loc;
        this.causer = causer;
        this.oldBlock = null;
        this.newBlock = null;
        this.oldLines = oldLines;
        this.newLines = newLines;
    }

    @Override
    public int compareTo(BlockLog o)
    {
        int temp = (int)(this.date.getTime() - o.date.getTime());
        if (temp != 0)
        {
            return temp;
        }
        return (int)(this.key - o.key);
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

    public void sendToUser(User user, boolean showLoc)
    {
        ArrayList<Object> list = new ArrayList<Object>();
        String message = "%s &2%s ";
        SimpleDateFormat sdf = new SimpleDateFormat();
        list.add(sdf.format(this.date));
        list.add(this.getCauser());
        if (action == LogManager.BLOCK_BREAK)
        {
            message += "&edestroyed &6%s";
            list.add(Match.material().getNameForItem(this.oldBlock.mat, this.oldBlock.data.shortValue()));
        }
        else if (action == LogManager.BLOCK_PLACE)
        {
            if (this.oldBlock == null || this.oldBlock.mat.getId() == 0)
            {
                message += "&eplaced &6%s";
                list.add(Match.material().getNameForItem(this.newBlock.mat, this.newBlock.data.shortValue()));
            }
            else
            {
                message += "&ereplaced &6%s &ewith &6%s";
                list.add(Match.material().getNameForItem(this.oldBlock.mat, this.oldBlock.data.shortValue()));
                list.add(Match.material().getNameForItem(this.newBlock.mat, this.newBlock.data.shortValue()));
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
                    list.add(Match.material().getNameForItem(this.oldBlock.mat, this.oldBlock.data.shortValue()));
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
                    list.add(Match.material().getNameForItem(this.oldBlock.mat, this.oldBlock.data.shortValue()));
                    break;
                case STONE_BUTTON:
                case WOOD_BUTTON:
                    message += "&epressed &6%s";
                    list.add(Match.material().getNameForItem(this.oldBlock.mat, this.oldBlock.data.shortValue()));
                    break;
                case CAKE_BLOCK: // data: remaining slices
                    message += "&eeat &6%s";
                    list.add(Match.material().getNameForItem(this.oldBlock.mat, this.oldBlock.data.shortValue()));
                    break;
                case NOTE_BLOCK:
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                    message += "&eadjusted &6%s";
                    list.add(Match.material().getNameForItem(this.oldBlock.mat, this.oldBlock.data.shortValue()));
                    break;
            }
        }
        else if (action == LogManager.BLOCK_SIGN)
        {
            if (this.newLines[0] == null)
            {
                message += "&edestroyed a sign with &f%s&e|&f%s&e|&f%s&e|&f%s";
                list.add(this.oldLines[0]);
                list.add(this.oldLines[1]);
                list.add(this.oldLines[2]);
                list.add(this.oldLines[3]);
            }
            else if (this.oldLines[0] == null)
            {
                message += "&eplaced a sign with &f%s&e|&f%s&e|&f%s&e|&f%s";
                list.add(this.newLines[0]);
                list.add(this.newLines[1]);
                list.add(this.newLines[2]);
                list.add(this.newLines[3]);
            }
            else
            {
                message += "&ewrote &f%s&e|&f%s&e|&f%s&e|&f%s";
                list.add(this.newLines[0]);
                list.add(this.newLines[1]);
                list.add(this.newLines[2]);
                list.add(this.newLines[3]);
            }
        }
        else if (action == LogManager.BLOCK_GROW_BP)
        {
            message += "&elet grow &6%s";
            list.add(this.newBlock.toString());
            if (this.oldBlock != null && this.oldBlock.mat.getId() != 0)
            {
                message += " &ereplacing &6%s";
                list.add(Match.material().getNameForItem(this.oldBlock.mat, this.oldBlock.data.shortValue()));
            }
        }
        else if (action == LogManager.BLOCK_CHANGE_WE)
        {
            message += "&echanged &6%s &eto &6%s &ewith &6WorldEdit";
            list.add(Match.material().getNameForItem(this.oldBlock.mat, this.oldBlock.data.shortValue()));
            list.add(Match.material().getNameForItem(this.newBlock.mat, this.newBlock.data.shortValue()));
        }
        else if (action == LogManager.BLOCK_EXPLODE)
        {
            message += "&elet a Creeper explode &6%s";
            list.add(Match.material().getNameForItem(this.oldBlock.mat, this.oldBlock.data.shortValue()));
        }
        if (showLoc)
        {
            message += " &eat &f(&6%d,%d,%d&f) &ein &6%s";
            list.add(this.loc.getBlockX());
            list.add(this.loc.getBlockY());
            list.add(this.loc.getBlockZ());
            list.add(this.loc.getWorld().getName());
        }
        user.sendMessage("log", message, list.toArray());
    }
}
