/*
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cubeengine.module.docs;

import static org.cubeengine.module.docs.DocType.MARKDOWN;

import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.LifeCycle;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.butler.alias.Alias;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.cubeengine.libcube.service.task.TaskManager;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.command.CommandSource;

import java.nio.file.Path;
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
    @Inject private Path modulePath;

    @Enable
    public void onEnable()
    {
        tm.runTaskDelayed(Docs.class, this::generateDocumentation, 20);
        cm.addCommands(this, this);
    }

    private void generateDocumentation()
    {
        Map<String, ModuleDocs> docs = new HashMap<>();
        for (LifeCycle lifeCycle : getModularity().getModules())
        {
            Module instance = (Module) lifeCycle.getInstance();
            docs.put(instance.getInformation().getName().toLowerCase(), new ModuleDocs(instance, reflector, pm, cm));
        }

        System.out.println("Generating Module Docs...");
        for (Map.Entry<String, ModuleDocs> entry : docs.entrySet())
        {
            entry.getValue().generate(modulePath, MARKDOWN);
        }
        System.out.println("Done Generating Module Docs!");
    }

    @Alias("gd")
    @Command(desc = "Generates documentation")
    public void generateDocs(CommandSource ctx)
    {
        this.generateDocumentation();
    }

}
