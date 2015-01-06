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
import javax.persistence.Transient;

import org.bukkit.World;

import org.jooq.impl.UpdatableRecordImpl;

import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;


public class WorldEntity extends UpdatableRecordImpl<WorldEntity>
{
    @Transient
    private UUID uid = null;

    public WorldEntity()
    {
        super(TABLE_WORLD);
    }

    public WorldEntity newWorld(World world)
    {
        this.setValue(TABLE_WORLD.WORLDNAME, world.getName());
        this.setWorldUUID(world.getUID());
        return this;
    }

    public UUID getWorldUUID()
    {
        if (uid == null)
        {
            uid = new UUID(this.getValue(TABLE_WORLD.MOST), this.getValue(TABLE_WORLD.LEAST));
        }
        return uid;
    }

    public void setWorldUUID(UUID uid)
    {
        this.uid = uid;
        this.setValue(TABLE_WORLD.LEAST, uid.getLeastSignificantBits());
        this.setValue(TABLE_WORLD.MOST, uid.getMostSignificantBits());
    }
}
