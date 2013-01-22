package de.cubeisland.cubeengine.log.lookup;

import java.sql.Timestamp;
import org.bukkit.Location;

public class KillLog
{
    public final long key;
    public final int action;
    public final Timestamp date;
    public final Location loc;
    public final Long causer;
    public final Long killed;

    public KillLog(long key, int action, Timestamp date, Location loc, Long causer, Long killed)
    {
        this.key = key;
        this.action = action;
        this.date = date;
        this.loc = loc;
        this.causer = causer;
        this.killed = killed;
    }
}
