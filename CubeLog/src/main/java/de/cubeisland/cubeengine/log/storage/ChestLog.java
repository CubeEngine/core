package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.sql.Timestamp;
import java.util.List;
import org.bukkit.Location;

@Entity(name = "chestlog")
public class ChestLog extends AbstractPositionLog
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
        super(args.subList(0, 7));
        this.item = Convert.fromObject(ItemData.class, args.get(8));
        this.amount = Convert.fromObject(Integer.class, args.get(9));
        this.typeId = Convert.fromObject(Integer.class, args.get(10));
        this.itemName = Convert.fromObject(String.class, args.get(11));
    }

    public ChestLog(Integer userId, ItemData item, int amount, Location loc, int type)
    {
        super(userId, loc);
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