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
package de.cubeisland.engine.basics.storage;

import java.sql.Timestamp;

import de.cubeisland.engine.core.user.User;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.basics.storage.TableBasicsUser.TABLE_BASIC_USER;

public class BasicsUserEntity extends UpdatableRecordImpl<BasicsUserEntity> implements Record3<UInteger, Timestamp, Boolean>
{
    public BasicsUserEntity()
    {
        super(TABLE_BASIC_USER);
    }

    public BasicsUserEntity newBasicUser(User user)
    {
        this.setKey(user.getEntity().getKey());
        this.setGodmode(false);
        return this;
    }

    public void setKey(UInteger value) {
        setValue(0, value);
    }

    public UInteger getKey() {
        return (UInteger) getValue(0);
    }

    public void setMuted(Timestamp value) {
        setValue(1, value);
    }

    public Timestamp getMuted() {
        return (Timestamp) getValue(1);
    }

    public void setGodmode(boolean set) {
        setValue(2, set);
    }

    public Boolean getGodmode() {
        return (Boolean) getValue(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<UInteger, Timestamp, Boolean> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<UInteger, Timestamp, Boolean> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_BASIC_USER.KEY;
    }

    @Override
    public Field<Timestamp> field2() {
        return TABLE_BASIC_USER.MUTED;
    }

    @Override
    public Field<Boolean> field3() {
        return TABLE_BASIC_USER.GODMODE;
    }

    @Override
    public UInteger value1() {
        return getKey();
    }

    @Override
    public Timestamp value2() {
        return getMuted();
    }

    @Override
    public Boolean value3() {
        return getGodmode();
    }
}
