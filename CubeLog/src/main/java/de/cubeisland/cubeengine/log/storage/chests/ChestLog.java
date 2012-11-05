package de.cubeisland.cubeengine.log.storage.chests;

import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import de.cubeisland.cubeengine.log.LogManager.ContainerType;
import de.cubeisland.cubeengine.log.storage.AbstractLog;
import de.cubeisland.cubeengine.log.storage.BlockData;
import java.sql.Timestamp;
import java.util.List;
import org.bukkit.World;

@Entity(name = "chestlog")
public class ChestLog extends AbstractLog
{
    private BlockData item;  //ID:DATA
    private int amount;      //+ added to chest - took from chest
    private ContainerType containerType;
    //causeID is the player or = 0 if unknown

    @DatabaseConstructor
    public ChestLog(List<Object> args) throws ConversionException
    {
        this.key = Convert.fromObject(Integer.class, args.get(0));
        this.timestamp = (Timestamp)args.get(1);
        this.causeID = Convert.fromObject(Integer.class, args.get(2));
        this.world = Convert.fromObject(World.class, args.get(3));
        this.x = Convert.fromObject(Integer.class, args.get(4));
        this.y = Convert.fromObject(Integer.class, args.get(5));
        this.z = Convert.fromObject(Integer.class, args.get(6));

        this.item = Convert.fromObject(BlockData.class, args.get(7));
        this.amount = Convert.fromObject(Integer.class, args.get(8));
        this.containerType = ContainerType.getContainerType(Convert.fromObject(Integer.class, args.get(9)));
    }

    
}
