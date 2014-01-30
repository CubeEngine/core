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
package de.cubeisland.engine.locker.storage;

import java.util.Arrays;
import java.util.Collection;

import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;

import static de.cubeisland.engine.locker.storage.ProtectedType.*;

public enum LockType
{
    PRIVATE(1, CONTAINER, DOOR, BLOCK, ENTITY_CONTAINER, ENTITY_LIVING, ENTITY_VEHICLE, ENTITY, ENTITY_CONTAINER_LIVING),
    PUBLIC(2, CONTAINER, DOOR, BLOCK, ENTITY_CONTAINER, ENTITY_LIVING, ENTITY_VEHICLE, ENTITY, ENTITY_CONTAINER_LIVING),
    GUARDED(3, CONTAINER, ENTITY_CONTAINER, ENTITY_CONTAINER_LIVING),
    DONATION(4, CONTAINER, ENTITY_CONTAINER, ENTITY_CONTAINER_LIVING),
    FREE(5, CONTAINER, ENTITY_CONTAINER, ENTITY_CONTAINER_LIVING);

    private final static TByteObjectMap<LockType> lockTypes = new TByteObjectHashMap<>();

    public final byte id;
    public final Collection<ProtectedType> supportedTypes;


    static
    {
        for (LockType lockType : LockType.values())
        {
            lockTypes.put(lockType.id, lockType);
        }
    }

    private LockType(int id, ProtectedType... supportedTypes)
    {
        this.id = (byte)id;
        this.supportedTypes = Arrays.asList(supportedTypes);
    }

    public static LockType forByte(Byte lockType)
    {
        return lockTypes.get(lockType);
    }
}
