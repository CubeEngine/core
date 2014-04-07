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
package de.cubeisland.engine.log.action;

public interface Recoverable
{
    /**
     * Returns whether this actionType is referring to a specific Block not a location
     * <p>This is mostly true for block-changes and container-transactions
     * <p>default is false override to change this!</p>
     *
     * @return true if this log-action is block-bound
     */
    boolean isBlockBound();

    /**
     * Returns whether this actionType can have more than one changes at a single location.
     * <p>e.g.: Block-Changes like block-break or block-place will return false
     * <p>Container-Transactions, mob-spawns or kills will return true
     * <p>default is true override to change this!</p>
     *
     * @return true if this log-action can stack
     */
    boolean isStackable();
}
