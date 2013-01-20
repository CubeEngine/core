package de.cubeisland.cubeengine.log.lookup;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.BlockData;
import java.sql.Timestamp;
import org.bukkit.Location;

public class BlockLog implements Comparable<BlockLog>
{
    public final long key;
    public final Timestamp date;
    public final Location loc;
    public final Long causer;
    public final BlockData oldBlock;
    public final BlockData newBlock;

    public BlockLog(long key, Timestamp date, Location loc, Long causer, BlockData oldBlock, BlockData newBlock)
    {
        this.key = key;
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
}
