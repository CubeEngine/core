package org.cubeengine.module.docs;

import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.libcube.service.permission.Permission;
import org.cubeengine.reflect.Reflector;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class ModuleDocs
{
    private final Module module;
    private final Info config;
    private final Set<CommandBase> commands = new HashSet<>();
    private final Set<Permission> permissions = new HashSet<>();


    public ModuleDocs(Module module, Reflector reflector) {
        this.module = module;
        InputStream is = module.getClass().getResourceAsStream(module.getInformation().getName().toLowerCase() + "-info.yml");
        this.config = reflector.load(Info.class, new InputStreamReader(is));
    }

    public void addPermission(Permission permission)
    {
        this.permissions.add(permission);
    }
}
