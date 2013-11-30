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
package de.cubeisland.engine.stats.storage;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.stats.storage.TableStats.TABLE_STATS;

public class StatsModel extends UpdatableRecordImpl<StatsModel> implements Record2<UInteger, String>
{

    public StatsModel()
    {
        super(TABLE_STATS);
    }

    public StatsModel newStatsModel(String stat)
    {
        this.setStat(stat);
        return this;
    }

    public UInteger getKey()
    {
        return (UInteger)this.getValue(0);
    }

    public void setKey(UInteger key)
    {
        this.setValue(0, key);
    }

    public String getStat()
    {
        return (String)this.getValue(1);
    }

    public void setStat(String stat)
    {
        this.setValue(1, stat);
    }

    @Override
    public Row2<UInteger, String> fieldsRow() {
        return (Row2)super.fieldsRow();
    }

    @Override
    public Row2<UInteger, String> valuesRow() {
        return (Row2)super.valuesRow();
    }
    @Override
    public Field<UInteger> field1()
    {
        return TABLE_STATS.KEY;
    }

    @Override
    public Field<String> field2()
    {
        return TABLE_STATS.STAT;
    }

    @Override
    public UInteger value1()
    {
        return getKey();
    }

    @Override
    public String value2()
    {
        return getStat();
    }
}
