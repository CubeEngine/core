package org.cubeengine.service.command;

import org.cubeengine.service.command.exception.PermissionDeniedException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.permission.PermissionDescription;

public class CommandUtil
{
    public static void ensurePermission(CommandSource src, PermissionDescription perm) throws PermissionDeniedException
    {
        if (!src.hasPermission(perm.getId()))
        {
            throw new PermissionDeniedException(perm);
        }
    }
}
