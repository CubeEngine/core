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
package de.cubeisland.cubeengine.travel.storage;

import de.cubeisland.cubeengine.core.storage.TwoKeyModel;
import de.cubeisland.cubeengine.core.storage.database.*;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

import java.util.List;

@TwoKeyEntity(tableName = "teleportinvites", firstPrimaryKey = "teleportpoint", secondPrimaryKey = "userkey",
    indices = {
            @Index(value = Index.IndexType.FOREIGN_KEY, fields = "teleportpoint", f_table = "teleportpoints", f_field = "key"),
            @Index(value = Index.IndexType.FOREIGN_KEY, fields = "userkey", f_table = "user", f_field = "key")
    })
public class TeleportInvite implements TwoKeyModel<Long, Long>
{
    @Attribute(type = AttrType.INT, unsigned = true, name = "teleportpoint")
    public Long teleportPoint;
    @Attribute(type = AttrType.INT, unsigned = true, name = "userkey")
    public Long userKey;

    @DatabaseConstructor
    public TeleportInvite(List<Object> args) throws ConversionException
    {
        this.teleportPoint = Long.valueOf(args.get(0).toString());
        this.userKey = Long.valueOf(args.get(1).toString());
    }

    public TeleportInvite(Long teleportPoint, Long userKey)
    {
        this.teleportPoint = teleportPoint;
        this.userKey = userKey;
    }

    /**
     * Check if the current and the teleportinvite supplied is equal in their contents.
     * This does not care if they are the same instance of TeleportInvite
     * @param tpI
     * @return
     */
    public boolean semiEquals(TeleportInvite tpI)
    {
        return tpI.teleportPoint == this.teleportPoint && tpI.userKey == this.userKey;
    }

    @Override
    public Pair<Long, Long> getKey()
    {
        return new Pair<Long, Long>(teleportPoint, userKey);
    }

    @Override
    public void setKey(Pair<Long, Long> key)
    {
        this.teleportPoint = key.getLeft();
        this.userKey = key.getRight();
    }
}
