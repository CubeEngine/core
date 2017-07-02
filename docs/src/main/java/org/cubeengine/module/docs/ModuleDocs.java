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

import de.cubeisland.engine.logscribe.Log;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.permission.Permission;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModuleDocs
{
    private final PluginContainer pc;
    private final String name;
    private final Info config;
    private final Set<Permission> permissions = new HashSet<>();
    private final Set<CommandBase> commands = new HashSet<>();

    public ModuleDocs(PluginContainer plugin, Class module, Reflector reflector, PermissionManager pm, CommandManager cm)
    {
        this.pc = plugin;
        this.name = plugin.getName();
        InputStream is = plugin.getClass().getResourceAsStream(plugin.getId() + "-info.yml");
        if (is == null)
        {
            this.config = reflector.create(Info.class);
        }
        else
        {
            this.config = reflector.load(Info.class, new InputStreamReader(is));
        }
        Permission basePermission = pm.getBasePermission(module);
        for (Map.Entry<String, Permission> entry : pm.getPermissions().entrySet())
        {
            if (entry.getKey().startsWith(basePermission.getId()))
            {
                this.permissions.add(entry.getValue());
            }
        }
        for (CommandBase base : cm.getCommands())
        {

            if (base.getDescriptor().getOwner().equals(module))
            {
                this.commands.add(base);
            }
        }
    }

    public void generate(Path modulePath, DocType docType, Log log)
    {
        String generated = docType.getGenerator()
                .generate(log, this.name, this.pc, this.config, this.permissions, this.commands);

        Path file = modulePath.resolve(this.name + docType.getFileExtension());
        try
        {
            Files.write(file, generated.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
