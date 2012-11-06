package de.cubeisland.cubeengine.log.storage.chests;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import de.cubeisland.cubeengine.log.LogManager.ContainerType;
import de.cubeisland.cubeengine.log.storage.AbstractLog;
import de.cubeisland.cubeengine.log.storage.ItemData;
import java.sql.Timestamp;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;

@Entity(name = "chestlog")
public class ChestLog extends AbstractLog
{
    @Attribute(type = AttrType.VARCHAR, length = 10)
    public ItemData item;  //ID:DATA
    @Attribute(type = AttrType.INT)
    public int amount;      //+ added to chest - took from chest
    @Attribute(type = AttrType.INT)
    public int typeId;
    @Attribute(type = AttrType.VARCHAR, length = 100)
    public String itemName = null;
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

        this.item = Convert.fromObject(ItemData.class, args.get(7));
        this.amount = Convert.fromObject(Integer.class, args.get(8));
        this.typeId = Convert.fromObject(Integer.class, args.get(9));
        this.itemName = Convert.fromObject(String.class, args.get(10));
    }

    public ChestLog(Integer userId, ItemData item, int amount, Location loc, int type)
    {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.world = loc.getWorld();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();

        this.causeID = userId;
        this.item = item;
        this.amount = amount;
        this.itemName = item.name;
        this.typeId = type;
    }

    public ItemData getFullItemData()
    {
        this.item.name = this.itemName;
        return this.item;
    }
}
