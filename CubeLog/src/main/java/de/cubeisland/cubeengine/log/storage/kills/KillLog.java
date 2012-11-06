package de.cubeisland.cubeengine.log.storage.kills;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import de.cubeisland.cubeengine.log.LogManager.KillCause;
import de.cubeisland.cubeengine.log.storage.AbstractLog;
import java.sql.Timestamp;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@de.cubeisland.cubeengine.core.storage.database.Entity(name = "killlog")
public class KillLog extends AbstractLog
{
    @Attribute(type = AttrType.INT)
    public int killedId; // positive values for Players / negative EntityId for mobs
    //causeID is the killer

    @DatabaseConstructor
    public KillLog(List<Object> args) throws ConversionException
    {
        this.key = Convert.fromObject(Integer.class, args.get(0));
        this.timestamp = (Timestamp)args.get(1);
        this.causeID = Convert.fromObject(Integer.class, args.get(2));
        this.world = Convert.fromObject(World.class, args.get(3));
        this.x = Convert.fromObject(Integer.class, args.get(4));
        this.y = Convert.fromObject(Integer.class, args.get(5));
        this.z = Convert.fromObject(Integer.class, args.get(6));
        this.killedId = Convert.fromObject(Integer.class, args.get(7));
    }

    public KillLog(KillCause killCause, Player damager, Entity entity, Location loc)
    {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        if (damager != null)
        {
            this.causeID = CubeEngine.getUserManager().getExactUser(damager).getKey();
        }
        else
        {
            this.causeID = killCause.getId();
        }
        if (entity instanceof Player)
        {
            this.killedId = CubeEngine.getUserManager().getExactUser((Player)entity).getKey();
        }
        else
        {
            this.killedId = -entity.getType().getTypeId();
        }
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.world = loc.getWorld();
    }
}