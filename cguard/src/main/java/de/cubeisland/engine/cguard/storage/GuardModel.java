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
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.cguard.storage.TableGuards.TABLE_GUARD;

public class GuardModel extends UpdatableRecordImpl<GuardModel> implements Record9<UInteger, UInteger, Short, Byte, Byte, byte[], Byte, Long, Long>
{

    public GuardModel()
    {
        super(TABLE_GUARD);
    }

    public void setId(UInteger value) {
        setValue(0, value);
    }

    public UInteger getId() {
        return (UInteger) getValue(0);
    }

    public void setOwnerId(UInteger value) {
        setValue(1, value);
    }

    public UInteger getOwnerId() {
        return (UInteger) getValue(1);
    }

    public void setFlags(Short value) {
        setValue(2, value);
    }

    public Short getFlags() {
        return (Short) getValue(2);
    }

    public void setType(Byte value) {
        setValue(3, value);
    }

    public Byte getType() {
        return (Byte) getValue(3);
    }

    public void setGuardType(Byte value) {
        setValue(4, value);
    }

    public Byte getGuardType() {
        return (Byte) getValue(4);
    }

    public void setPassword(byte[] value) {
        setValue(5, value);
    }

    public byte[] getPassword() {
        return (byte[]) getValue(5);
    }

    public void setDroptransfer(Byte value) {
        setValue(6, value);
    }

    public Byte getDroptransfer() {
        return (Byte) getValue(6);
    }

    public void setEntityUidLeast(Long value) {
        setValue(7, value);
    }

    public Long getEntityUidLeast() {
        return (Long) getValue(7);
    }

    public void setEntityUidMost(Long value) {
        setValue(8, value);
    }

    public Long getEntityUidMost() {
        return (Long) getValue(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row9<UInteger, UInteger, Short, Byte, Byte, byte[], Byte, Long, Long> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    @Override
    public Row9<UInteger, UInteger, Short, Byte, Byte, byte[], Byte, Long, Long> valuesRow() {
        return (Row9) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_GUARD.ID;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_GUARD.OWNER_ID;
    }

    @Override
    public Field<Short> field3() {
        return TABLE_GUARD.FLAGS;
    }

    @Override
    public Field<Byte> field4() {
        return TABLE_GUARD.GUARDED_TYPE;
    }

    @Override
    public Field<Byte> field5() {
        return TABLE_GUARD.GUARD_TYPE;
    }

    @Override
    public Field<byte[]> field6() {
        return TABLE_GUARD.PASSWORD;
    }

    @Override
    public Field<Byte> field7() {
        return TABLE_GUARD.DROPTRANSFER;
    }

    @Override
    public Field<Long> field8() {
        return TABLE_GUARD.ENTITY_UID_LEAST;
    }

    @Override
    public Field<Long> field9() {
        return TABLE_GUARD.ENTITY_UID_MOST;
    }

    @Override
    public UInteger value1() {
        return getId();
    }

    @Override
    public UInteger value2() {
        return getOwnerId();
    }

    @Override
    public Short value3() {
        return getFlags();
    }

    @Override
    public Byte value4() {
        return getType();
    }

    @Override
    public Byte value5() {
        return getGuardType();
    }

    @Override
    public byte[] value6() {
        return getPassword();
    }

    @Override
    public Byte value7() {
        return getDroptransfer();
    }

    @Override
    public Long value8() {
        return getEntityUidLeast();
    }

    @Override
    public Long value9() {
        return getEntityUidMost();
    }
}
