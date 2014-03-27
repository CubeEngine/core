package de.cubeisland.engine.log.action.newaction.player.entity.vehicle;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.log.action.newaction.player.PlayerActionType;

public abstract class PlayerVehicleActionType extends PlayerActionType<PlayerVehicleListener>
{
    public UUID vehicleUUID;
    public EntityType vehicleType;

    public void setVehicle(Entity entity)
    {
        this.vehicleUUID = entity.getUniqueId();
        this.vehicleType = entity.getType();
    }
}
