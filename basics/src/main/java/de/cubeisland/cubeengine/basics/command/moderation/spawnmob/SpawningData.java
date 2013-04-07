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
package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class SpawningData
{
    public Location location;
    public EntityType entityType;
    public int amount = 1;
    private HashMap<EntityDataChanger,Object> data = new HashMap<EntityDataChanger, Object>();

    public void doSpawn()
    {
        if (entityType != null)
        {
            for (int i = 0; i < amount; i++)
            {
                Entity entity = this.location.getWorld().spawnEntity(this.location,this.entityType);
                for (Map.Entry<EntityDataChanger,Object> entry : data.entrySet())
                {
                    entry.getKey().applyTo(entity,entry.getValue());
                }
            }
        }
    }

    public <E,T> void add(EntityDataChanger<E,T> changer, T value)
    {
        this.data.put(changer,value);
    }

    public void showInfo(User user)
    {
    //TODO

    }

    public void clearData() {
        this.data = new HashMap<EntityDataChanger, Object>();
    }
}
