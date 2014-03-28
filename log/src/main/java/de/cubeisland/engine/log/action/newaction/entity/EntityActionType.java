package de.cubeisland.engine.log.action.newaction.entity;

import org.bukkit.entity.Entity;

import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.entity.EntityBlockActionType.EntitySection;

public abstract class EntityActionType<ListenerType> extends ActionTypeBase<ListenerType>
{
    public EntitySection entity;

    public void setEntity(Entity entity)
    {
        this.entity = new EntitySection(entity);
    }
}
