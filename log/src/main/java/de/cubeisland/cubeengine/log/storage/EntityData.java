package de.cubeisland.cubeengine.log.storage;

import org.bukkit.entity.EntityType;

import com.fasterxml.jackson.databind.JsonNode;

public class EntityData
{
    public final EntityType entityType;

    public EntityData(EntityType entityType, JsonNode json)
    {
        this.entityType = entityType;
        //TODO get data from json
    }

    @Override
    public String toString()
    {
        return entityType.name(); //TODO
    }
}
