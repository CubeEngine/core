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
package de.cubeisland.engine.roles.storage;

import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.roles.storage.TableData.TABLE_META;

public class UserMetaData extends UpdatableRecordImpl<UserMetaData> implements Record4<UInteger, UInteger, String, String>
{
    public UserMetaData()
    {
        super(TABLE_META);
    }

    public UserMetaData newMeta(UInteger userId, UInteger worldId, String key, String value)
    {
        this.setUserid(userId);
        this.setWorldid(worldId);
        this.setKey(key);
        this.setValue(value);
        return this;
    }

    public void setUserid(UInteger value) {
        setValue(0, value);
    }

    public UInteger getUserid() {
        return (UInteger) getValue(0);
    }

    public void setWorldid(UInteger value) {
        setValue(1, value);
    }

    public UInteger getWorldid() {
        return (UInteger) getValue(1);
    }

    public void setKey(String value) {
        setValue(2, value);
    }

    public String getKey() {
        return (String) getValue(2);
    }

    public void setValue(String value) {
        setValue(3, value);
    }

    public String getValue() {
        return (String) getValue(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record3<UInteger, UInteger, String> key() {
        return (Record3) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<UInteger, UInteger, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<UInteger, UInteger, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_META.USERID;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_META.WORLDID;
    }

    @Override
    public Field<String> field3() {
        return TABLE_META.KEY;
    }

    @Override
    public Field<String> field4() {
        return TABLE_META.VALUE;
    }

    @Override
    public UInteger value1() {
        return getUserid();
    }

    @Override
    public UInteger value2() {
        return getWorldid();
    }

    @Override
    public String value3() {
        return getKey();
    }

    @Override
    public String value4() {
        return getValue();
    }
}
