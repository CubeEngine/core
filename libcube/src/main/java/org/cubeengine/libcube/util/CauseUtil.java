package org.cubeengine.libcube.util;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;

public class CauseUtil
{
    private CauseUtil() {}

    public static Cause spawnCause(CommandSource source)
    {
        return Cause.of(NamedCause.source(SpawnCause.builder().type(SpawnTypes.PLUGIN).build()),
                        NamedCause.notifier(source));
    }
}
