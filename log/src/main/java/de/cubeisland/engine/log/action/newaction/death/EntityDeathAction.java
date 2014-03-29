package de.cubeisland.engine.log.action.newaction.death;

import org.bukkit.entity.Entity;

import de.cubeisland.engine.log.action.newaction.block.entity.EntityBlockActionType.EntitySection;

public abstract class EntityDeathAction extends DeathAction
{
    public EntitySection killedEntity;

    public void setKilled(Entity entity)
    {
        this.killedEntity = new EntitySection(entity);
    }
}
