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
package de.cubeisland.engine.locker;

import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.locker.storage.LockType;
import de.cubeisland.engine.locker.storage.ProtectedType;
import de.cubeisland.engine.locker.storage.ProtectionFlag;

public abstract class LockerSubConfig<This extends LockerSubConfig,T>
{
    protected final ProtectedType protectedType;
    protected boolean autoProtect = false;
    protected LockType autoProtectType;
    protected List<ProtectionFlag> defaultFlags;
    protected boolean enable = true;
    protected T type;

    public LockerSubConfig(ProtectedType protectedType)
    {
        this.protectedType = protectedType;
    }

    public This autoProtect(LockType type)
    {
        this.autoProtectType = type;
        this.autoProtect = type != null;
        return (This)this;
    }

    public This defaultFlags(ProtectionFlag... flags)
    {
        this.defaultFlags = Arrays.asList(flags);
        return (This)this;
    }

    public boolean isType(T type)
    {
        return this.type.equals(type);
    }

    public abstract String getTitle();
}
