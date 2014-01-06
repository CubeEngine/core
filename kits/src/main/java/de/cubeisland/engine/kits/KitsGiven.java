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
package de.cubeisland.engine.kits;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

public class KitsGiven extends UpdatableRecordImpl<KitsGiven> implements Record3<UInteger, String, Integer>
{
    public KitsGiven()
    {
        super(TableKitsGiven.TABLE_KITS);
    }

    public void setUserid(UInteger value) {
        setValue(0, value);
    }

    public UInteger getUserid() {
        return (UInteger) getValue(0);
    }

    public void setKitname(String value) {
        setValue(1, value);
    }

    public String getKitname() {
        return (String) getValue(1);
    }

    public void setAmount(Integer value) {
        setValue(2, value);
    }

    public Integer getAmount() {
        return (Integer) getValue(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<UInteger, String> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<UInteger, String, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<UInteger, String, Integer> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TableKitsGiven.TABLE_KITS.USERID;
    }

    @Override
    public Field<String> field2() {
        return TableKitsGiven.TABLE_KITS.KITNAME;
    }

    @Override
    public Field<Integer> field3() {
        return TableKitsGiven.TABLE_KITS.AMOUNT;
    }

    @Override
    public UInteger value1() {
        return getUserid();
    }

    @Override
    public String value2() {
        return getKitname();
    }

    @Override
    public Integer value3() {
        return getAmount();
    }

}
