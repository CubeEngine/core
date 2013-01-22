package de.cubeisland.cubeengine.log.lookup;

import java.sql.Timestamp;
import org.bukkit.Location;

public class MessageLog
{
    public final long key;
    public final int action;
    public final Timestamp date;
    public final Location loc;
    public final Long causer;
    public final String message;

    public MessageLog(long key, int action, Timestamp date, Location loc, Long causer, String message)
    {
        this.key = key;
        this.action = action;
        this.date = date;
        this.loc = loc;
        this.causer = causer;
        this.message = message;
    }
}
