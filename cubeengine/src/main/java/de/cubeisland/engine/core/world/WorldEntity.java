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
package de.cubeisland.engine.core.world;

import org.bukkit.World;

import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;


// TODO change from String UUID -> 2 Longs
public class WorldEntity extends UpdatableRecordImpl<WorldEntity> implements Record3<UInteger, String, String>
{
    public WorldEntity() {
        super(TABLE_WORLD);
    }

    public WorldEntity newWorld(World world)
    {
        this.setWorldname(world.getName());
        this.setWorldUUID(world.getUID().toString());
        return this;
    }

    public void setKey(UInteger value) {
        setValue(0, value);
    }

    public UInteger getKey() {
        return (UInteger) getValue(0);
    }

    public void setWorldname(String value) {
        setValue(1, value);
    }

    public String getWorldname() {
        return (String) getValue(1);
    }

    public void setWorldUUID(String value) {
        setValue(2, value);
    }

    public String getWorldUUID() {
        return (String) getValue(2);
    }

    @Override
    public org.jooq.Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public org.jooq.Row3<UInteger, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public org.jooq.Row3<UInteger, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public org.jooq.Field<UInteger> field1() {
        return TABLE_WORLD.KEY;
    }

    @Override
    public org.jooq.Field<String> field2() {
        return TABLE_WORLD.WORLDNAME;
    }

    @Override
    public org.jooq.Field<String> field3() {
        return TABLE_WORLD.WORLDUUID;
    }

    @Override
    public UInteger value1() {
        return getKey();
    }

    @Override
    public String value2() {
        return getWorldname();
    }

    @Override
    public String value3() {
        return getWorldUUID();
    }
}
