package de.cubeisland.engine.log.action.newaction.player.entity;

import org.bukkit.entity.Entity;

import de.cubeisland.engine.log.action.newaction.block.entity.EntityBlockActionType.EntitySection;
import de.cubeisland.engine.log.action.newaction.player.PlayerActionType;

public abstract class PlayerEntityActionType extends PlayerActionType<PlayerEntityListener>
{
    public EntitySection entity;

    public void setEntity(Entity entity)
    {
        this.entity = new EntitySection(entity);
    }
}
