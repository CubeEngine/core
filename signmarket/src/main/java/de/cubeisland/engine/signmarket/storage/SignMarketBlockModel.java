/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.signmarket.storage;

import java.sql.Connection;
import java.sql.SQLException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.bukkit.Location;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.DBUpdater;
import de.cubeisland.engine.core.storage.database.DatabaseUpdater;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.core.world.WorldEntity;
import de.cubeisland.engine.signmarket.storage.SignMarketBlockModel.SignMarketBlockUpdater;

@Entity
@Table(name = "signmarketblocks", uniqueConstraints = @UniqueConstraint(columnNames = {"world", "x", "y", "z"}))
@DBUpdater(SignMarketBlockUpdater.class)
public class SignMarketBlockModel
{
    @javax.persistence.Version
    static final Version version = new Version(2);

    @Id
    @Attribute(type = AttrType.INT, unsigned = true)
    private long id = 0;

    @Column(name = "world", nullable = false)
    @JoinColumn(name = "world")
    @ManyToOne(optional = false, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @Attribute(type = AttrType.INT, unsigned = true)
    private WorldEntity world;
    @Column(nullable = false)
    @Attribute(type = AttrType.INT)
    private int x;
    @Column(nullable = false)
    @Attribute(type = AttrType.INT)
    private int y;
    @Column(nullable = false)
    @Attribute(type = AttrType.INT)
    private int z;

    @Column(nullable = false)
    @Attribute(type = AttrType.BOOLEAN)
    private Boolean signType; // null - invalid | true - buy | false - sell
    @Column(name = "owner")
    @JoinColumn(name = "owner")
    @ManyToOne(optional = false, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    private UserEntity owner; // null - admin-shop | else user-key

    @Column(name = "itemKey", nullable = false)
    @JoinColumn(name = "itemKey")
    @ManyToOne(optional = false, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @Attribute(type = AttrType.INT, unsigned = true)
    private SignMarketItemModel itemModel;
    @Column(nullable = false)
    @Attribute(type = AttrType.SMALLINT, unsigned = true)
    private int amount = 0;
    @Column
    @Attribute(type = AttrType.MEDIUMINT, unsigned = true)
    private Integer demand;
    @Column(nullable = false)
    @Attribute(type = AttrType.INT, unsigned = true)
    private long price;

    // Helper-methods:
    @Transient
    private Location location;

    public SignMarketBlockModel(Location location)
    {
        this.world = CubeEngine.getCore().getWorldManager().getWorldEntity(location.getWorld());
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    /**
     * Copies the values from an other BlockModel into this one
     *
     * @param blockInfo the model to copy the values from
     */
    public void copyValuesFrom(SignMarketBlockModel blockInfo)
    {
        this.signType = blockInfo.signType;
        this.owner = blockInfo.owner;
        this.itemModel = blockInfo.itemModel;
        this.amount = blockInfo.amount;
        this.demand = blockInfo.demand;
        this.price = blockInfo.price;
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
            this.location = new Location(CubeEngine.getCore().getWorldManager().getWorld(world.getWorldName()), x, y, z);
        }
        return this.location;
    }

    /**
     * Returns true if given user is the owner
     *
     * @param user
     * @return
     */
    public boolean isOwner(User user)
    {
        if (this.owner == null) return user == null;
        if (user == null) return false;
        return user.getId().equals(this.owner.getId());
    }

    public SignMarketBlockModel()
    {}

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public WorldEntity getWorld()
    {
        return world;
    }

    public void setWorld(WorldEntity world)
    {
        this.world = world;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getZ()
    {
        return z;
    }

    public void setZ(int z)
    {
        this.z = z;
    }

    public Boolean getSignType()
    {
        return signType;
    }

    public void setSignType(Boolean signType)
    {
        this.signType = signType;
    }

    public UserEntity getOwner()
    {
        return owner;
    }

    public void setOwner(UserEntity owner)
    {
        this.owner = owner;
    }

    public SignMarketItemModel getItemModel()
    {
        return itemModel;
    }

    public void setItemModel(SignMarketItemModel itemModel)
    {
        this.itemModel = itemModel;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    public Integer getDemand()
    {
        return demand;
    }

    public void setDemand(Integer demand)
    {
        this.demand = demand;
    }

    public long getPrice()
    {
        return price;
    }

    public void setPrice(long price)
    {
        this.price = price;
    }

    public static class SignMarketBlockUpdater implements DatabaseUpdater
    {
        @Override
        public void update(Connection connection, Class<?> entityClass, Version dbVersion, Version codeVersion) throws SQLException
        {
            if (codeVersion.getMajor() == 2)
            {
                connection.prepareStatement("RENAME TABLE cube_signmarketblocks TO old_signmarketblocks").execute();
                connection.prepareStatement("CREATE TABLE `cube_signmarketblocks` (  " +
                                                "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,  " +
                                                "`world` int(10) unsigned NOT NULL,  " +
                                                "`x` int(11) NOT NULL,  " +
                                                "`y` int(11) NOT NULL,  " +
                                                "`z` int(11) NOT NULL,  " +
                                                "`signType` tinyint(1) NOT NULL,  " +
                                                "`owner` int(10) unsigned DEFAULT NULL, " +
                                                "`itemKey` int(10) unsigned NOT NULL,  " +
                                                "`amount` smallint(5) unsigned NOT NULL,  " +
                                                "`demand` mediumint(8) unsigned DEFAULT NULL,  " +
                                                "`price` int(10) unsigned NOT NULL,  " +
                                                "PRIMARY KEY (`id`),  " +
                                                "KEY `loc` (`world`,`x`,`y`,`z`),  " +
                                                "FOREIGN KEY f_worldid(`world`) REFERENCES `cube_worlds` (`key`) ON DELETE CASCADE,  " +
                                                "FOREIGN KEY f_signitemid(`itemKey`) REFERENCES `cube_signmarketitem` (`id`) ON DELETE CASCADE,  " +
                                                "FOREIGN KEY f_ownerid(`owner`) REFERENCES `cube_user` (`key`) ON DELETE CASCADE)" +
                                                "DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT = '2.0.0'").execute() ;
                connection.prepareStatement("INSERT INTO `cube_signmarketblocks` (`id`, `world`, `x`,`y`,`z`,`signType`,`owner`,`itemKey`,`amount`,`demand`,`price`) " +
                                                "SELECT `key`, `world`, `x`,`y`,`z`,`signType`,`owner`,`itemKey`,`amount`,`demand`,`price` FROM `old_signmarketblocks`").execute();
                connection.prepareStatement("DROP TABLE old_signmarketblocks").execute();
                // DROP old related table
                connection.prepareStatement("DROP TABLE old_signmarketitem").execute();
            }
        }
    }
}
