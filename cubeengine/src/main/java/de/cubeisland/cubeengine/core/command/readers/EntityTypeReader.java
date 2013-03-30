package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.entity.EntityType;

public class EntityTypeReader extends ArgumentReader<EntityType>
{
    public EntityTypeReader()
    {
        super(EntityType.class);
    }

    @Override
    public EntityType read(String arg)
    {
        return Match.entity().any(arg);
    }
}
