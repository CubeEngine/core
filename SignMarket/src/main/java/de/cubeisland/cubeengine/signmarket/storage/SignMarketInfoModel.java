
package de.cubeisland.cubeengine.signmarket.storage;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;


@SingleKeyEntity(autoIncrement = true, primaryKey = "key", tableName = "signmarketinfo",
        indices = {
                @Index(value = Index.IndexType.FOREIGN_KEY, fields = "key", f_field = "key", f_table = "signmarketblocks", onDelete = "CASCADE"),
        })
public class SignMarketInfoModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key;
    @Attribute(type = AttrType.BOOLEAN)
    public Boolean isBuySign; //else isSellSign / NULL -> Edit illegal value for database!


    @Attribute(type = AttrType.SMALLINT, unsigned = true)
    public int amount;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long price;
    @Attribute(type = AttrType.MEDIUMINT, unsigned = true, notnull = false)
    public Integer stock;
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    public Long owner; //TODO foreign key to user

    //ITEM-data:
    @Attribute(type = AttrType.VARCHAR, length = 32)
    public String item;
    @Attribute(type = AttrType.SMALLINT, unsigned = true)
    public Short damageValue;
    @Attribute(type = AttrType.VARCHAR, length = 100, notnull = false)
    public String customName;
    @Attribute(type = AttrType.VARCHAR, length = 1000, notnull = false)
    public String lore;
    @Attribute(type = AttrType.VARCHAR, length = 255, notnull = false)
    public String enchantments;



    @Override
    public Long getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }

    public SignMarketInfoModel() {
    }
}