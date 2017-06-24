package org.cubeengine.module.docs;


import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.LifeCycle;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.cubeengine.libcube.service.task.TaskManager;
import org.cubeengine.reflect.Reflector;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

@ModuleInfo(name = "docs", description = "Generate Documentation for CubeEngine on the fly.")
public class Docs extends Module
{
    @Inject private TaskManager tm;
    @Inject private Reflector reflector;
    @Inject private PermissionManager pm;
    @Inject private CommandManager cm;

    @Enable
    public void onEnable()
    {
        tm.runTaskDelayed(Docs.class, this::generateDocumentation, 20);
    }

    private void generateDocumentation()
    {
        Map<String, ModuleDocs> docs = new HashMap<>();
        for (LifeCycle lifeCycle : getModularity().getModules()) {
            Module instance = (Module) lifeCycle.getInstance();
            docs.put(instance.getInformation().getName().toLowerCase(), new ModuleDocs(instance, reflector, pm, cm));
        }



        // TODO actually generate stuff

    }
}
