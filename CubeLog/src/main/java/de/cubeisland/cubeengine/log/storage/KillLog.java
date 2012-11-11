package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.util.List;
import org.bukkit.Location;

@de.cubeisland.cubeengine.core.storage.database.Entity(name = "killlog")
public class KillLog extends AbstractPositionLog
{
    @Attribute(type = AttrType.INT)
    public int killedId; // positive values for Players / negative EntityId for mobs
    //causeID is the killer

    @DatabaseConstructor
    public KillLog(List<Object> args) throws ConversionException
    {
        super(args.subList(0, 7));
        this.killedId = Convert.fromObject(Integer.class, args.get(8));
    }

    public KillLog(int killerId, int killedId, Location loc)
    {
        super(killerId, loc);
        this.killedId = killedId;
    }
}