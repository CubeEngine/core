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
package de.cubeisland.engine.log.action.newaction.player.entity.vehicle;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.newaction.player.PlayerActionType;

import static de.cubeisland.engine.log.action.ActionTypeCategory.VEHICLE;

public abstract class PlayerVehicleActionType extends PlayerActionType<PlayerVehicleListener>
{
    public UUID vehicleUUID;
    public EntityType vehicleType;

    public void setVehicle(Entity entity)
    {
        this.vehicleUUID = entity.getUniqueId();
        this.vehicleType = entity.getType();
    }

    @Override
    public ActionTypeCategory getCategory()
    {
        return VEHICLE;
    }
}
