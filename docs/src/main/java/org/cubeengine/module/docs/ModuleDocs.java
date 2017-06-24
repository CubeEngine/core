package org.cubeengine.module.docs;

import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.permission.Permission;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.cubeengine.reflect.Reflector;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModuleDocs
{
    private final Module module;
    private final Info config;
    private final Permission basePermission;
    private final Set<Permission> permissions = new HashSet<>();
    private final Set<CommandBase> commands = new HashSet<>();

    public ModuleDocs(Module module, Reflector reflector, PermissionManager pm, CommandManager cm) {
        this.module = module;
        InputStream is = module.getClass().getResourceAsStream(module.getInformation().getName().toLowerCase() + "-info.yml");
        this.config = reflector.load(Info.class, new InputStreamReader(is));
        this.basePermission = pm.getBasePermission(module.getClass());
        for (Map.Entry<String, Permission> entry : pm.getPermissions().entrySet()) {
            if (entry.getKey().startsWith(this.basePermission.getId())) {
                this.permissions.add(entry.getValue());
            }
        }
        for (CommandBase base : cm.getCommands()) {
            if (base.getDescriptor().getOwner().equals(module.getClass())) {
                this.commands.add(base);
            }
        }
    }
}
