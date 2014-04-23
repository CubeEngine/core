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
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.roles.storage.TableRole.TABLE_ROLE;

public class AssignedRole extends UpdatableRecordImpl<AssignedRole> implements Record3<UInteger, UInteger, String>
{
    public AssignedRole()
    {
        super(TABLE_ROLE);
    }

    public AssignedRole newAssignedRole(UInteger userId, UInteger worldId, String roleName)
    {
        this.setUserid(userId);
        this.setWorldid(worldId);
        this.setRolename(roleName);
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

    public void setRolename(String value) {
        setValue(2, value);
    }

    public String getRolename() {
        return (String) getValue(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record3<UInteger, UInteger, String> key() {
        return (Record3) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<UInteger, UInteger, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<UInteger, UInteger, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    
    @Override
    public Field<UInteger> field1() {
        return TABLE_ROLE.USERID;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_ROLE.WORLDID;
    }

    @Override
    public Field<String> field3() {
        return TABLE_ROLE.ROLENAME;
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
        return getRolename();
    }
}
