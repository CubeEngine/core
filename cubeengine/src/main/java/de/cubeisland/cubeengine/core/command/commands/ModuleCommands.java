/**
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
package de.cubeisland.cubeengine.core.command.commands;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.ModuleInfo;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.module.exception.ModuleException;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Version;



public class ModuleCommands extends ContainerCommand
{
    private final ModuleManager mm;

    public ModuleCommands(ModuleManager mm)
    {
        super(mm.getCoreModule(), "module", "Provides ingame module plugin management functionality");
        this.mm = mm;
    }

    @Alias(names = {
        "modules"
    })
    @Command(names = {
        "list", "show"
    }, desc = "Lists all the loaded modules", max = 0)
    public void list(CommandContext context)
    {
        Collection<Module> modules = this.mm.getModules();

        if (!modules.isEmpty())
        {
            context.sendTranslated("These are the loaded modules.");
            context.sendTranslated("&aGreen (+)&r stands for enabled, &cred (-)&r for disabled.");
            context.sendMessage(" ");

            for (Module module : modules)
            {
                if (module.isEnabled())
                {
                    context.sendMessage(" + " + ChatFormat.BRIGHT_GREEN + module.getName());
                }
                else
                {
                    context.sendMessage(" - " + ChatFormat.RED + module.getName());
                }
            }
        }
        else
        {
            context.sendTranslated("&eThere are no modules loaded!");
        }
    }

    @Command(desc = "Enables a module", usage = "<module>", min = 1, max = 1)
    public void enable(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendTranslated("The given module could not be found!");
        }
        else if (this.mm.enableModule(module))
        {
            context.sendTranslated("The given module was successfully enabled!");
        }
        else
        {
            context.sendTranslated("An error occurred while enabling the module!");
        }
    }

    @Command(desc = "Disables a module", usage = "<module>", min = 1, max = 1)
    public void disable(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendTranslated("The given module could not be found!");
        }
        else
        {
            this.mm.disableModule(module);
            context.sendTranslated("The given module was successfully disabled!");
        }
    }

    @Command(desc = "Unloaded a module and all the modules that depend on it", usage = "<module>", min = 1, max = 1)
    public void unload(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendTranslated("The given module could not be found!");
        }
        else
        {
            this.mm.unloadModule(module);
            context.sendTranslated("The given module was successfully unloaded!");
        }
    }

    @Command(desc = "Reloads a module", usage = "<module> [-f]", min = 1, max = 1, flags = @Flag(name = "f", longName = "file"))
    public void reload(ParameterizedContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendTranslated("The given module could not be found!");
        }
        else
        {
            try
            {
                this.mm.reloadModule(module, context.hasFlag("f"));
            }
            catch (ModuleException e)
            {
                context.sendTranslated("&cFailed to reload the module!");
                context.sendTranslated("&eCheck the server log for info.");
                context.getCore().getLog().error("Failed to reload the module " + module.getName() +": "
                                                     + e.getLocalizedMessage(), e);
            }
        }
    }

    @Command(desc = "Loads a module from the modules directory.", usage = "<file name>", min = 1, max = 1)
    public void load(CommandContext context)
    {
        String moduleFileName = context.getString(0);
        if (moduleFileName.contains(".") || moduleFileName.contains("/") || moduleFileName.contains("\\"))
        {
            context.sendTranslated("&cThe given file name is invalid!");
            return;
        }

        File modulesDir = context.getCore().getFileManager().getModulesDir();

        File moduleFile = new File(modulesDir, context.getString(0) + ".jar");
        if (!moduleFile.exists())
        {
            context.sendTranslated("&cThe given module file was not found! The name might be case sensitive.");
            return;
        }

        if (!moduleFile.canRead())
        {
            context.sendTranslated("&cThe module exists, but cannot be read! Check the file permissions.");
            return;
        }

        try
        {
            Module module = context.getCore().getModuleManager().loadModule(moduleFile);
            context.sendTranslated("&aThe module &6%s&a has been successfully loaded!", module.getName());
        }
        catch (ModuleException e)
        {
            context.sendTranslated("&cThe module failed to load! Check the server log for info.");
            context.getCore().getLog().error("Failed to load a module from file " + moduleFile.getPath() + ": "
                                                 + e.getLocalizedMessage(), e);
        }
    }

    @Command(desc = "Get info about a module", flags = {
        @Flag(name = "s", longName = "source")
    }, usage = "<module> [-s]", min = 1, max = 1)
    public void info(ParameterizedContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendTranslated("The given module could not be found!");
            return;
        }
        ModuleInfo moduleInfo = module.getInfo();
        context.sendTranslated("Name: %s", moduleInfo.getName());
        context.sendTranslated("Description: %s", moduleInfo.getDescription());
        context.sendTranslated("Version: %s", moduleInfo.getVersion());
        VanillaCommands.showSourceVersion(context, moduleInfo.getSourceVersion());

        Map<String, Version> dependencies = moduleInfo.getDependencies();
        Map<String, Version> softDependencies = moduleInfo.getSoftDependencies();
        Set<String> pluginDependencies = moduleInfo.getPluginDependencies();
        if (!dependencies.isEmpty())
        {
            context.sendTranslated("Module dependencies:");
            for (String dependency : dependencies.keySet())
            {
                context.sendMessage("   - " + dependency);
            }
        }
        if (!softDependencies.isEmpty())
        {
            context.sendTranslated("Module soft-dependencies:");
            for (String dependency : softDependencies.keySet())
            {
                context.sendMessage("   - " + dependency);
            }
        }
        if (!pluginDependencies.isEmpty())
        {
            context.sendTranslated("Plugin dependencies:");
            for (String dependency : pluginDependencies)
            {
                context.sendMessage("   - " + dependency);
            }
        }
    }
}
