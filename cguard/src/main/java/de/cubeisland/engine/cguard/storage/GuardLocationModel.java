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
package de.cubeisland.engine.cguard.storage;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.cguard.storage.TableGuardLocations.TABLE_GUARD_LOCATION;

public class GuardLocationModel extends UpdatableRecordImpl<GuardLocationModel> implements Record6<UInteger, UInteger, Integer, Integer, Integer, UInteger>
{
    public GuardLocationModel()
    {
        super(TABLE_GUARD_LOCATION);
    }

    public void setId(UInteger value) {
        setValue(0, value);
    }

    public UInteger getId() {
        return (UInteger) getValue(0);
    }

    public void setWorldId(UInteger value) {
        setValue(1, value);
    }

    public UInteger getWorldId() {
        return (UInteger) getValue(1);
    }

    public void setX(Integer value) {
        setValue(2, value);
    }

    public Integer getX() {
        return (Integer) getValue(2);
    }

    public void setY(Integer value) {
        setValue(3, value);
    }

    public Integer getY() {
        return (Integer) getValue(3);
    }

    public void setZ(Integer value) {
        setValue(4, value);
    }

    public Integer getZ() {
        return (Integer) getValue(4);
    }

    public void setGuardId(UInteger value) {
        setValue(5, value);
    }

    public UInteger getGuardId() {
        return (UInteger) getValue(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<UInteger, UInteger, Integer, Integer, Integer, UInteger> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<UInteger, UInteger, Integer, Integer, Integer, UInteger> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_GUARD_LOCATION.ID;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_GUARD_LOCATION.WORLD_ID;
    }

    @Override
    public Field<Integer> field3() {
        return TABLE_GUARD_LOCATION.X;
    }

    @Override
    public Field<Integer> field4() {
        return TABLE_GUARD_LOCATION.Y;
    }

    @Override
    public Field<Integer> field5() {
        return TABLE_GUARD_LOCATION.Z;
    }

    @Override
    public Field<UInteger> field6() {
        return TABLE_GUARD_LOCATION.GUARD_ID;
    }

    @Override
    public UInteger value1() {
        return getId();
    }

    @Override
    public UInteger value2() {
        return getWorldId();
    }

    @Override
    public Integer value3() {
        return getX();
    }

    @Override
    public Integer value4() {
        return getY();
    }

    @Override
    public Integer value5() {
        return getZ();
    }

    @Override
    public UInteger value6() {
        return getGuardId();
    }
}
