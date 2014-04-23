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
        this.addIndex(WORLD, X, Y, Z);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), OWNER);
        this.addForeignKey(TABLE_WORLD.getPrimaryKey(), WORLD);
        this.addForeignKey(TABLE_SIGN_ITEM.getPrimaryKey(), ITEMKEY);
        this.addFields(KEY, WORLD, X,Y,Z, SIGNTYPE,OWNER, ITEMKEY, AMOUNT, DEMAND, PRICE);
        TABLE_SIGN_BLOCK = this;
    }

    public final TableField<SignMarketBlockModel, UInteger> KEY = createField("key", U_INTEGER.nullable(false), this);
    public final TableField<SignMarketBlockModel, UInteger> WORLD = createField("world", U_INTEGER.nullable(false), this);
    public final TableField<SignMarketBlockModel, Integer> X = createField("x", SQLDataType.INTEGER, this);
    public final TableField<SignMarketBlockModel, Integer> Y = createField("y", SQLDataType.INTEGER, this);
    public final TableField<SignMarketBlockModel, Integer> Z = createField("z", SQLDataType.INTEGER, this);
    public final TableField<SignMarketBlockModel, Byte> SIGNTYPE = createField("signType", SQLDataType.TINYINT, this);
    public final TableField<SignMarketBlockModel, UInteger> OWNER = createField("owner", U_INTEGER, this);
    public final TableField<SignMarketBlockModel, UInteger> ITEMKEY = createField("itemKey", U_INTEGER.nullable(false), this);
    public final TableField<SignMarketBlockModel, UShort> AMOUNT = createField("amount", U_SMALLINT.nullable(false), this);
    public final TableField<SignMarketBlockModel, UInteger> DEMAND = createField("demand", U_MEDIUMINT, this);
    public final TableField<SignMarketBlockModel, UInteger> PRICE = createField("price", U_INTEGER.nullable(false), this);

    @Override
    public Class<SignMarketBlockModel> getRecordType() {
        return SignMarketBlockModel.class;
    }
}
