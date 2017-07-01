package org.cubeengine.module.docs;

import de.cubeisland.engine.logscribe.Log;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.libcube.service.permission.Permission;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Set;

public interface Generator
{
    String generate(Log log, String name, PluginContainer pc, Info info, Set<Permission> permissions, Set<CommandBase> commands);
}
