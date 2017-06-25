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

import static java.util.stream.Collectors.toMap;

import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.Dispatcher;
import org.cubeengine.butler.StringUtils;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.alias.AliasConfiguration;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.command.CubeDescriptor;
import org.cubeengine.libcube.service.command.HelpCommand;
import org.cubeengine.libcube.service.permission.Permission;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.cubeengine.reflect.Reflector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ModuleDocs
{
    private final Module module;
    private final String name;
    private final Info config;
    private final Permission basePermission;
    private final Set<Permission> permissions = new HashSet<>();
    private final Set<CommandBase> commands = new HashSet<>();

    public ModuleDocs(Module module, Reflector reflector, PermissionManager pm, CommandManager cm)
    {
        this.module = module;
        this.name = module.getInformation().getName();
        InputStream is = module.getClass().getResourceAsStream(module.getInformation().getName().toLowerCase() + "-info.yml");
        if (is == null)
        {
            this.config = reflector.create(Info.class);
        }
        else
        {
            this.config = reflector.load(Info.class, new InputStreamReader(is));
        }
        this.basePermission = pm.getBasePermission(module.getClass());
        for (Map.Entry<String, Permission> entry : pm.getPermissions().entrySet())
        {
            if (entry.getKey().startsWith(this.basePermission.getId()))
            {
                this.permissions.add(entry.getValue());
            }
        }
        for (CommandBase base : cm.getCommands())
        {
            if (base.getDescriptor().getOwner().equals(module.getClass()))
            {
                this.commands.add(base);
            }
        }
    }

    public void generate(Path modulePath, DocType docType)
    {
        String generated = null;
        switch (docType) {
            case MARKDOWN:
                generated = generateMarkDown();
        }

        Path file = modulePath.resolve(this.name + ".md");
        try
        {
            Files.write(file, generated.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String generateMarkDown()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(this.name);
        sb.append("\n\n");
        sb.append(this.module.getInformation().getDescription());
        sb.append("\n\n");
        if (this.config.features.isEmpty())
        {
            System.out.println("Missing Features for " + this.name + "!");
        }
        else
        {
            sb.append("## Features:\n");
            for (String feature : this.config.features)
            {
                sb.append(" - ").append(feature).append("\n");
            }
        }

        TreeMap<String, Permission> addPerms = new TreeMap<>(this.permissions.stream().collect(toMap(Permission::getId, p -> p)));
        if (!this.commands.isEmpty())
        {
            sb.append("\n\n").append("## Commands").append("\n");
            for (CommandBase command : this.commands)
            {
                generateCommandDocs(sb, addPerms, command, new Stack<>());
            }

        }

        if (!addPerms.values().isEmpty())
        {
            sb.append("\n## Additional Permissions\n");

            for (Permission perm : addPerms.values())
            {
                sb.append(" - ##### `").append(perm.getId()).append("`\n");
                sb.append("   ").append(perm.getDesc()).append("\n\n");
            }
        }

        return sb.toString();
    }

    private void generateCommandDocs(StringBuilder sb, Map<String, Permission> addPerms, CommandBase command, Stack<String> commandStack)
    {
        if (command instanceof AliasCommand || command instanceof HelpCommand)
        {
            return;
        }
        commandStack.push(command.getDescriptor().getName());
        sb.append("\n#### ").append(StringUtils.join(" ", commandStack)).append("\n");
        sb.append(command.getDescriptor().getDescription()).append("\n\n");
        sb.append("Usage: `").append(command.getDescriptor().getUsage(null)).append("`");

        if (!command.getDescriptor().getAliases().isEmpty())
        {
            sb.append("\n\nAlias:");
            for (AliasConfiguration alias : command.getDescriptor().getAliases())
            {
                String[] dispatcher = alias.getDispatcher();
                List<String> labels = new ArrayList<>();
                if (dispatcher == null) {
                    labels.add(alias.getName()); // local alias
                } else {
                    labels.addAll(Arrays.asList(alias.getDispatcher()));
                    labels.add(alias.getName());
                    labels.set(0, "/" + labels.get(0));
                }
                sb.append("\n`").append(StringUtils.join(" ", labels)).append("`");
            }
        }
        sb.append("\n\n");

        if (command.getDescriptor() instanceof CubeDescriptor)
        {
            Permission perm = ((CubeDescriptor) command.getDescriptor()).getPermission().getRegistered();
            sb.append("Permission: `").append(perm.getId()).append("`\n");
            addPerms.remove(perm.getId());

            // TODO parameter permission?
            // TODO parameter description?
            // TODO Butler Parser with default parameter descriptions
        }

        if (command instanceof Dispatcher)
        {
            StringBuilder subBuilder = new StringBuilder();
            List<CommandBase> subCommands = new ArrayList<>(((Dispatcher) command).getCommands());
            subCommands.sort(Comparator.comparing(o -> o.getDescriptor().getName()));
            for (CommandBase sub : subCommands)
            {
                if (!(sub instanceof HelpCommand) && !(sub instanceof AliasCommand))
                {
                    subBuilder.append("\n`").append(sub.getDescriptor().getName()).append("`");
                }
            }

            if (subBuilder.length() != 0)
            {
                sb.append("\nChild Commands:").append(subBuilder.toString());
            }

            for (CommandBase sub : subCommands)
            {
                this.generateCommandDocs(sb, addPerms, sub, commandStack);
            }
        }
        commandStack.pop();
    }
}
