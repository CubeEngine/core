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
package de.cubeisland.engine.core.storage;

import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;

import static de.cubeisland.engine.core.storage.Registry.TABLE_REGISTRY;

public class RegistryModel extends UpdatableRecordImpl<RegistryModel> implements Record3<String, String, String>
{
    public RegistryModel()
    {
        super(TABLE_REGISTRY);
    }

    public void setKey(String value) {
        setValue(0, value);
    }

    public String getKey() {
        return (String) getValue(0);
    }

    public void setModule(String value) {
        setValue(1, value);
    }

    public String getModule() {
        return (String) getValue(1);
    }

    public void setValue(String value) {
        setValue(2, value);
    }

    public String getValue() {
        return (String) getValue(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public org.jooq.Record2<String, String> key() {
        return (org.jooq.Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<String, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return TABLE_REGISTRY.KEY;
    }

    @Override
    public Field<String> field2() {
        return TABLE_REGISTRY.MODULE;
    }

    @Override
    public Field<String> field3() {
        return TABLE_REGISTRY.VALUE;
    }

    @Override
    public String value1() {
        return getKey();
    }

    @Override
    public String value2() {
        return getModule();
    }

    @Override
    public String value3() {
        return getValue();
    }
}
