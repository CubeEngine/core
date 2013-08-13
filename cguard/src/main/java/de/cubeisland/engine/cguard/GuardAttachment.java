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
package de.cubeisland.engine.cguard;

import java.util.HashSet;
import java.util.Set;

import de.cubeisland.engine.cguard.storage.Guard;
import de.cubeisland.engine.core.user.UserAttachment;

public class GuardAttachment extends UserAttachment
{
    private Set<Long> unlockedProtections = new HashSet<>();

    public void addUnlock(Guard guard)
    {
        unlockedProtections.add(guard.getId());
    }

    public boolean hasUnlocked(Guard guard)
    {
        return this.unlockedProtections.contains(guard.getId());
    }
}
