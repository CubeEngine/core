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

import de.cubeisland.engine.core.storage.TripletKeyModel;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.Index;
import de.cubeisland.engine.core.storage.database.TripletKeyEntity;
import de.cubeisland.engine.core.util.Triplet;

import static de.cubeisland.engine.core.storage.database.Index.IndexType.FOREIGN_KEY;

@TripletKeyEntity(tableName = "userdata", firstPrimaryKey = "userId", secondPrimaryKey = "worldId", thirdPrimaryKey = "key", indices = {
    @Index(value = FOREIGN_KEY, fields = "userId", f_table = "user", f_field = "key"),
    @Index(value = FOREIGN_KEY, fields = "worldId", f_table = "worlds", f_field = "key")
})
public class UserMetaData implements TripletKeyModel<Long, Long, String>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long userId;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long worldId;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String key;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String value;

    public UserMetaData(long userId, long worldId, String key, String value)
    {
        this.userId = userId;
        this.worldId = worldId;
        this.key = key;
        this.value = value;
    }

    @Override
    public Triplet<Long, Long, String> getId()
    {
        return new Triplet<Long, Long, String>(userId, worldId, key);
    }

    @Override
    public void setId(Triplet<Long, Long, String> id)
    {
        this.userId = id.getFirst();
        this.worldId = id.getSecond();
        this.key = id.getThird();
    }
}
