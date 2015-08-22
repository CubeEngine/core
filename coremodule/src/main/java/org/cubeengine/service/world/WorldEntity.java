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
package org.cubeengine.service.world;

import java.util.UUID;
import javax.persistence.Transient;
import org.cubeengine.service.database.AsyncRecord;
import org.spongepowered.api.world.World;

public class WorldEntity extends AsyncRecord<WorldEntity>
{
    @Transient
    private UUID uid = null;

    public WorldEntity()
    {
        super(TableWorld.TABLE_WORLD);
    }

    public WorldEntity newWorld(World world)
    {
        this.setValue(TableWorld.TABLE_WORLD.WORLDNAME, world.getName());
        this.setWorldUUID(world.getUniqueId());
        return this;
    }

    public UUID getWorldUUID()
    {
        if (uid == null)
        {
            uid = new UUID(this.getValue(TableWorld.TABLE_WORLD.MOST), this.getValue(TableWorld.TABLE_WORLD.LEAST));
        }
        return uid;
    }

    public void setWorldUUID(UUID uid)
    {
        this.uid = uid;
        this.setValue(TableWorld.TABLE_WORLD.LEAST, uid.getLeastSignificantBits());
        this.setValue(TableWorld.TABLE_WORLD.MOST, uid.getMostSignificantBits());
    }
}
