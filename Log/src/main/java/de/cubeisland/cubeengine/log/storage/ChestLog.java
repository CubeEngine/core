package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.util.List;
import org.bukkit.Location;

@Entity(name = "chestlog")
public class ChestLog extends AbstractPositionLog
{
    @Attribute(type = AttrType.VARCHAR, length = 10)
    public String item; //ID:DATA
    @Attribute(type = AttrType.INT)
    public int amount; //+ added to chest - took from chest
    @Attribute(type = AttrType.INT)
    public int typeId;
    @Attribute(type = AttrType.VARCHAR, length = 100)
    public String itemName = null;
    private ItemData itemData = null;

    //causeID is the player or = 0 if unknown
    @DatabaseConstructor
    public ChestLog(List<Object> args) throws ConversionException
    {
        super(args.subList(0, 7));
        this.item = args.get(8).toString();
        this.amount = Convert.fromObject(Integer.class, args.get(9));
        this.typeId = Convert.fromObject(Integer.class, args.get(10));
        this.itemName = Convert.fromObject(String.class, args.get(11));
    }

    public ChestLog(Integer userId, Location loc, ItemData item, int amount, int type)
    {
        super(userId, loc);
        try
        {
            this.item = (String)Convert.toObject(item);
            this.amount = amount;
            this.itemName = item.name;
            this.typeId = type;
        }
        catch (ConversionException ingored)
        {}
    }

    public ItemData getFullItemData()
    {
        this.initItemData().name = this.itemName;
        return this.itemData;
    }

    private ItemData initItemData()
    {
        try
        {
            if (this.itemData == null)
            {
                this.itemData = Convert.fromObject(ItemData.class, item);
            }
            return this.itemData;
        }
        catch (ConversionException ingored)
        {
            return null;
        }
    }
}
