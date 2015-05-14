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
package de.cubeisland.engine.module.service.user;

import de.cubeisland.engine.modularity.core.Module;

import de.cubeisland.engine.module.core.sponge.CubeEvent;

/**
 * This is a base for user related events.
 */
public abstract class UserEvent extends CubeEvent
{
    private final User user;

    public UserEvent(Module core, User user)
    {
        super(core);
        this.user = user;
    }

    /**
     * Returns the user corresponding to this event
     *
     * @return a user
     */
    public User getUser()
    {
        return this.user;
    }
}
