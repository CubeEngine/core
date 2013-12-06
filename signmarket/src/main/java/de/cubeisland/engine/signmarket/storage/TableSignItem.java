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

public class TableSignItem extends AutoIncrementTable<SignMarketItemModel, UInteger>
{
    public static TableSignItem TABLE_SIGN_ITEM;

    public TableSignItem(String prefix)
    {
        super(prefix + "signmarketitem", new Version(1));
        this.setAIKey(KEY);
        this.addFields(KEY, STOCK, ITEM, DAMAGEVALUE, CUSTOMNAME, LORE, ENCHANTMENTS, SIZE);
        TABLE_SIGN_ITEM = this;
    }

    public final TableField<SignMarketItemModel, UInteger> KEY = createField("key", U_INTEGER.nullable(false), this);
    public final TableField<SignMarketItemModel, UInteger> STOCK = createField("stock", U_MEDIUMINT, this);
    public final TableField<SignMarketItemModel, String> ITEM = createField("item", SQLDataType.VARCHAR.length(32).nullable(false), this);
    public final TableField<SignMarketItemModel, UShort> DAMAGEVALUE = createField("damageValue", U_SMALLINT.nullable(false), this);
    public final TableField<SignMarketItemModel, String> CUSTOMNAME = createField("customName", SQLDataType.VARCHAR.length(100), this);
    public final TableField<SignMarketItemModel, String> LORE = createField("lore", SQLDataType.VARCHAR.length(1000), this);
    public final TableField<SignMarketItemModel, String> ENCHANTMENTS = createField("enchantments", SQLDataType.VARCHAR.length(255), this);
    public final TableField<SignMarketItemModel, Byte> SIZE = createField("size", SQLDataType.TINYINT.nullable(false), this);

    @Override
    public Class<SignMarketItemModel> getRecordType() {
        return SignMarketItemModel.class;
    }
}
