package org.cubeengine.module.docs;


import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.LifeCycle;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.libcube.service.task.TaskManager;
import org.cubeengine.reflect.Reflector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@ModuleInfo(name = "docs", description = "Generate Documentation for CubeEngine on the fly.")
public class Docs extends Module
{
    @Inject private TaskManager tm;
    @Inject private Reflector reflector;

    @Enable
    public void onEnable()
    {
        tm.runTaskDelayed(Docs.class, this::generateDocumentation, 20);
    }

    private void generateDocumentation()
    {
        List<ModuleDocs> docs = new ArrayList<>();
        for (LifeCycle lifeCycle : getModularity().getModules()) {
            Module instance = (Module) lifeCycle.getInstance();
            docs.add(new ModuleDocs(instance, reflector));
        }

        // TODO actually generate stuff

    }
}
