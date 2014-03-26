package de.cubeisland.engine.log.action.newaction.player.entity;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.log.action.newaction.player.PlayerActionType;

public abstract class PlayerEntityActionType extends PlayerActionType<PlayerEntityListener>
{
    public UUID entityUUID;
    public EntityType entityType;

    public void setEntity(Entity entity)
    {
        this.entityUUID = entity.getUniqueId();
        this.entityType = entity.getType();
    }
}
