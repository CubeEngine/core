package de.cubeisland.cubeengine.signmarket.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;

@SingleKeyEntity(autoIncrement = true, primaryKey = "key", tableName = "signmarketblocks", indices = {
        @Index(value = Index.IndexType.FOREIGN_KEY, fields = "world", f_field = "key", f_table = "worlds", onDelete = "CASCADE"),
        @Index(value = Index.IndexType.FOREIGN_KEY, fields = "itemKey", f_field = "key", f_table = "signmarketitem", onDelete = "CASCADE"),
        @Index(value = Index.IndexType.FOREIGN_KEY, fields = "owner", f_field = "key", f_table = "user"),
        @Index(value = Index.IndexType.INDEX, fields = {"x", "y", "z"})
})
public class SignMarketBlockModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key = -1;

    @Attribute(type = AttrType.INT, unsigned = true)
    public long world;
    @Attribute(type = AttrType.INT)
    public int x;
    @Attribute(type = AttrType.INT)
    public int y;
    @Attribute(type = AttrType.INT)
    public int z;

    @Attribute(type = AttrType.BOOLEAN)
    public Boolean signType; // null - invalid | true - buy | false - sell
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    public Long owner; // null - admin-shop | else user-key

    @Attribute(type = AttrType.INT, unsigned = true)
    public long itemKey = -1;

    @Attribute(type = AttrType.SMALLINT, unsigned = true)
    public int amount = 0;
    @Attribute(type = AttrType.MEDIUMINT, unsigned = true, notnull = false)
    public Integer demand;

    @Attribute(type = AttrType.INT, unsigned = true)
    public long price;
    @Attribute(type = AttrType.VARCHAR, length = 64)
    public String currency;

    // Helper-methods:
    private Location location;

    public SignMarketBlockModel(Location location) {
        this.world = CubeEngine.getCore().getWorldManager().getWorldId(location.getWorld());
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public void applyValues(SignMarketBlockModel blockInfo) {
        this.signType = blockInfo.signType;
        this.owner = blockInfo.owner;
        this.userOwner = null;
        this.itemKey = blockInfo.itemKey;
        this.amount = blockInfo.amount;
        this.demand = blockInfo.demand;
        this.price = blockInfo.price;
        this.currency = blockInfo.currency;
    }


    /**
     * Returns the location of this sign
     * <p>Do NEVER change this location!
     *
     * @return the location of the sign represented by this model
     */
    public final Location getLocation()
    {
        if (this.location == null)
        {
            this.location = new Location(CubeEngine.getCore().getWorldManager().getWorld(world), x, y, z);
        }
        return this.location;
    }

    /**
     * Sets the owner
     *
     * @param owner null for admin-signs
     */
    public void setOwner(User owner)
    {
        this.owner = owner == null ? null : owner.key;
        this.userOwner = owner;
    }

    private User userOwner;
    public User getOwner()
    {
        if (owner == null)
            return null;
        if (userOwner == null)
        {
            userOwner = CubeEngine.getUserManager().getUser(owner);
        }
        return userOwner;
    }

    public boolean isBuyOrSell()
    {
        return this.signType != null;
    }
    public boolean isOwner(User user)
    {
        return this.owner == user.key;
    }


    //for database:
    @Override
    public Long getKey()
    {
        return key;
    }
    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }
    public SignMarketBlockModel()
    {}


}
