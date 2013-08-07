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

import de.cubeisland.engine.core.user.User;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.basics.storage.TableIgnorelist.TABLE_IGNORE_LIST;

public class IgnoreList extends UpdatableRecordImpl<IgnoreList> implements Record2<UInteger, UInteger>
{
    public IgnoreList()
    {
        super(TABLE_IGNORE_LIST);
    }

    public IgnoreList newIgnore(User user, User ignore)
    {
        this.setKey(user.getEntity().getKey());
        this.setIgnore(ignore.getEntity().getKey());
        return this;
    }

    public void setKey(UInteger value) {
        setValue(0, value);
    }

    public UInteger getKey() {
        return (UInteger) getValue(0);
    }

    public void setIgnore(UInteger value) {
        setValue(1, value);
    }

    public UInteger getIgnore() {
        return (UInteger) getValue(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<UInteger, UInteger> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<UInteger, UInteger> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<UInteger, UInteger> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_IGNORE_LIST.KEY;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_IGNORE_LIST.IGNORE;
    }

    @Override
    public UInteger value1() {
        return getKey();
    }

    @Override
    public UInteger value2() {
        return getIgnore();
    }
}
