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

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;
import static de.cubeisland.engine.signmarket.storage.TableSignItem.TABLE_SIGN_ITEM;

public class TableSignBlock extends AutoIncrementTable<SignMarketBlockModel, UInteger>
{
    public static TableSignBlock TABLE_SIGN_BLOCK;

    public TableSignBlock(String prefix)
    {
        super(prefix + "signmarketblocks", new Version(1));
        this.setAIKey(KEY);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), OWNER);
        this.addForeignKey(TABLE_WORLD.getPrimaryKey(), WORLD);
        this.addForeignKey(TABLE_SIGN_ITEM.getPrimaryKey(), ITEMKEY);
        this.TABLE_SIGN_BLOCK = this;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                    "`key`int(10) unsigned NOT NULL AUTO_INCREMENT,\n " +
                                    "`world` int(10) unsigned NOT NULL,\n" +
                                    "`x` int(11) NOT NULL,\n" +
                                    "`y` int(11) NOT NULL,\n" +
                                    "`z` int(11) NOT NULL,\n" +
                                    "`signType` tinyint(1) NOT NULL,\n" +
                                    "`owner` int(10) unsigned DEFAULT NULL,\n" +
                                    "`itemKey` int(10) unsigned NOT NULL,\n" +
                                    "`amount` smallint(5) unsigned NOT NULL,\n" +
                                    "`demand` mediumint(8) unsigned DEFAULT NULL,\n" +
                                    "`price` int(10) unsigned NOT NULL,\n" +
                                    "PRIMARY KEY (`key`),\n" +
                                    "KEY `loc` (`world`,`x`,`y`,`z`),\n" +
                                    "FOREIGN KEY `f_owner`(`owner`) REFERENCES " + TABLE_USER.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                    "FOREIGN KEY `f_world`(`world`) REFERENCES " + TABLE_WORLD.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                    "FOREIGN KEY `f_world`(`itemKey`) REFERENCES " + TABLE_SIGN_ITEM.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                    "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                    "COMMENT='1.0.0'").execute();
    }

    public final TableField<SignMarketBlockModel, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<SignMarketBlockModel, UInteger> WORLD = createField("world", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<SignMarketBlockModel, Integer> X = createField("x", SQLDataType.INTEGER, this);
    public final TableField<SignMarketBlockModel, Integer> Y = createField("y", SQLDataType.INTEGER, this);
    public final TableField<SignMarketBlockModel, Integer> Z = createField("z", SQLDataType.INTEGER, this);
    public final TableField<SignMarketBlockModel, Byte> SIGNTYPE = createField("signType", SQLDataType.TINYINT, this);
    public final TableField<SignMarketBlockModel, UInteger> OWNER = createField("owner", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<SignMarketBlockModel, UInteger> ITEMKEY = createField("itemKey", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<SignMarketBlockModel, UShort> AMOUNT = createField("amount", SQLDataType.SMALLINTUNSIGNED, this);
    public final TableField<SignMarketBlockModel, UInteger> DEMAND = createField("demand", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<SignMarketBlockModel, UInteger> PRICE = createField("price", SQLDataType.INTEGERUNSIGNED, this);

    @Override
    public Class<SignMarketBlockModel> getRecordType() {
        return SignMarketBlockModel.class;
    }
}
