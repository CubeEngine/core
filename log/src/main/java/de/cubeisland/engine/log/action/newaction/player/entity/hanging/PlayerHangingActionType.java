package de.cubeisland.engine.log.action.newaction.player.entity.hanging;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.log.action.newaction.player.PlayerActionType;

public abstract class PlayerHangingActionType extends PlayerActionType<PlayerHangingListener>
{
    public UUID hangingUUID;
    public EntityType hangingType;

    public void setHanging(Entity entity)
    {
        this.hangingUUID = entity.getUniqueId();
        this.hangingType = entity.getType();
    }
}
