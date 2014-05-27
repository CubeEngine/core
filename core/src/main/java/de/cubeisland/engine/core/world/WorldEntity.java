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

import java.util.UUID;

import org.bukkit.World;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;


public class WorldEntity extends UpdatableRecordImpl<WorldEntity> implements Record4<UInteger, String, Long, Long>
{
    public WorldEntity() {
        super(TABLE_WORLD);
    }

    public WorldEntity newWorld(World world)
    {
        this.setWorldname(world.getName());
        this.setWorldUUID(world.getUID());
        return this;
    }

    private UUID uid = null;

    public UUID getWorldUUID()
    {
        if (uid == null)
        {
            uid = new UUID(this.getWorldUUIDMost(), this.getWorldUUIDLeast());
        }
        return uid;
    }

    public void setWorldUUID(UUID uid)
    {
        this.uid = uid;
        this.setWorldUUIDLeast(uid.getLeastSignificantBits());
        this.setWorldUUIDMost(uid.getMostSignificantBits());
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

    public void setWorldUUIDLeast(Long value) {
        setValue(2, value);
    }

    public Long getWorldUUIDLeast() {
        return (Long) getValue(2);
    }

    public void setWorldUUIDMost(Long value) {
        setValue(3, value);
    }

    public Long getWorldUUIDMost() {
        return (Long) getValue(3);
    }

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<UInteger, String, Long, Long> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<UInteger, String, Long, Long> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_WORLD.KEY;
    }

    @Override
    public Field<String> field2() {
        return TABLE_WORLD.WORLDNAME;
    }

    @Override
    public Field<Long> field3() {
        return TABLE_WORLD.LEAST;
    }

    @Override
    public Field<Long> field4() {
        return TABLE_WORLD.MOST;
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
    public Long value3() {
        return getWorldUUIDLeast();
    }

    @Override
    public Long value4() {
        return getWorldUUIDMost();
    }
}
