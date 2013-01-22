package de.cubeisland.cubeengine.log.lookup;

import de.cubeisland.cubeengine.log.storage.ItemData;
import java.sql.Timestamp;
import org.bukkit.Location;

public class ChestLog
{
    public final long key;
    public final int action;
    public final Timestamp date;
    public final Location loc;
    public final Long causer;
    public final ItemData itemData;
    public final Integer amount;
    public final Integer containerType;

    public ChestLog(long key, int action, Timestamp date, Location loc, Long causer, ItemData itemData, Integer amount, Integer containerType)
    {
        this.key = key;
        this.action = action;
        this.date = date;
        this.loc = loc;
        this.causer = causer;
        this.itemData = itemData;
        this.amount = amount;
        this.containerType = containerType;
    }
}
