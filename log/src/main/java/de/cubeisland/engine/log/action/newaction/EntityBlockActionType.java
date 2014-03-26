package de.cubeisland.engine.log.action.newaction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public abstract class EntityBlockActionType<ListenerType> extends BlockActionType<ListenerType>
{
    public UUID entityUUID;
    public EntityType entityType;

    public void setEntity(Entity entity)
    {
        this.entityUUID = entity.getUniqueId();
        this.entityType = entity.getType();
    }

    protected final int countUniqueEntities()
    {
        Set<UUID> uuids = new HashSet<>();
        uuids.add(this.entityUUID);
        int count = 1;
        for (ActionTypeBase action : this.getAttached())
        {
            if (!uuids.contains(((EntityBlockActionType)action).entityUUID))
            {
                uuids.add(((EntityBlockActionType)action).entityUUID);
                count++;
            }
        }
        return count;
    }

    // TODO additional
}
