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

import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.stats.storage.TableStatsData.TABLE_STATSDATA;

public class StatsDataModel extends UpdatableRecordImpl<StatsDataModel> implements Record4<UInteger, UInteger, Timestamp, String>
{
    public StatsDataModel()
    {
        super(TABLE_STATSDATA);
    }

    public StatsDataModel newStatsData(UInteger statsID, Timestamp timestamp, String data)
    {
        this.setStatID(statsID);
        this.setTimestamp(timestamp);
        this.setData(data);
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

    public UInteger getStatID()
    {
        return (UInteger)this.getValue(1);
    }

    public void setStatID(UInteger statID)
    {
        this.setValue(1, statID);
    }

    public Timestamp getTimestamp()
    {
        return (Timestamp)this.getValue(2);
    }

    public void setTimestamp(Timestamp timestamp)
    {
        this.setValue(2, timestamp);
    }

    public String getData()
    {
        return (String)this.getValue(3);
    }

    public void setData(String data)
    {
        this.setValue(3, data);
    }

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    @Override
    public Row4<UInteger, UInteger, Timestamp, String> fieldsRow() {
        return (Row4)super.fieldsRow();
    }

    @Override
    public Row4<UInteger, UInteger, Timestamp, String> valuesRow() {
        return (Row4)super.valuesRow();
    }

    @Override
    public Field<UInteger> field1()
    {
        return TABLE_STATSDATA.KEY;
    }

    @Override
    public Field<UInteger> field2()
    {
        return TABLE_STATSDATA.STAT;
    }

    @Override
    public Field<Timestamp> field3()
    {
        return TABLE_STATSDATA.TIMESTAMP;
    }

    @Override
    public Field<String> field4()
    {
        return TABLE_STATSDATA.DATA;
    }

    @Override
    public UInteger value1()
    {
        return getKey();
    }

    @Override
    public UInteger value2()
    {
        return getStatID();
    }

    @Override
    public Timestamp value3()
    {
        return getTimestamp();
    }

    @Override
    public String value4()
    {
        return getData();
    }
}
