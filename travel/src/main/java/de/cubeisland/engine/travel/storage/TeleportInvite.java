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
package de.cubeisland.engine.travel.storage;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.travel.storage.TableInvite.TABLE_INVITE;

public class TeleportInvite extends UpdatableRecordImpl<TeleportInvite> implements Record2<UInteger, UInteger>
{
    public TeleportInvite()
    {
        super(TABLE_INVITE);
    }

    public TeleportInvite newInvite(UInteger teleportPoint, UInteger userKey)
    {
        this.setTeleportpoint(teleportPoint);
        this.setUserkey(userKey);
        return this;
    }

    public void setTeleportpoint(UInteger value)
    {
        setValue(0, value);
    }

    public UInteger getTeleportpoint()
    {
        return (UInteger)getValue(0);
    }

    public void setUserkey(UInteger value)
    {
        setValue(1, value);
    }

    public UInteger getUserkey()
    {
        return (UInteger)getValue(1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Record2<UInteger, UInteger> key()
    {
        return (Record2)super.key();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Row2<UInteger, UInteger> fieldsRow()
    {
        return (Row2)super.fieldsRow();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Row2<UInteger, UInteger> valuesRow()
    {
        return (Row2)super.valuesRow();
    }

    @Override
    public Field<UInteger> field1()
    {
        return TABLE_INVITE.TELEPORTPOINT;
    }

    @Override
    public Field<UInteger> field2()
    {
        return TABLE_INVITE.USERKEY;
    }

    @Override
    public UInteger value1()
    {
        return getTeleportpoint();
    }

    @Override
    public UInteger value2()
    {
        return getUserkey();
    }
}
