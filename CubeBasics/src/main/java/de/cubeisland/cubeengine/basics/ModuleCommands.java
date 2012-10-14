package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.ModuleManager;

public class ModuleCommands extends ContainerCommand
{
    private final ModuleManager mm;
    
    public ModuleCommands(Basics module)
    {
        super(module, "module", "Provides ingame module plugin management functionality");
        this.mm = module.getModuleManager();
    }
    
    @Alias(names = {"modules"})
    @Command(
        names = {"list", "show"},
        desc = "Lists all the loaded modules",
        max = 0
    )
    public void list(CommandContext context)
    {
        context.sendMessage("basics", "These are the loaded modules.");
        context.sendMessage("basics", "&aGreen&r stands for enabled, &cred&r for disabled.");
        context.sendMessage(" ");
        
        for (Module module : this.mm.getModules())
        {
            if (module.isEnabled())
            {
                context.sendMessage(" + &a" + module.getName());
            }
            else
            {
                context.sendMessage(" - &c" + module.getName());
            }
        }
    }
 
    @Command(
        desc = "Enables a module",
        min = 1,
        max = 1
    )
    public void enable(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendMessage("basics", "The given module could not be found!");
        }
        else if (this.mm.enableModule(module))
        {
            context.sendMessage("basics", "The given module was successfully enabled!");
        }
        else
        {
            context.sendMessage("basics", "An error occurred while enabling the module!");
        }
    }
 
    @Command(
        desc = "Disables a module",
        min = 1,
        max = 1
    )
    public void disable(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendMessage("basics", "The given module could not be found!");
        }
        else
        {
            this.mm.disableModule(module);
            context.sendMessage("basics", "The given module was successfully disabled!");
        }
    }
 
    @Command(
        desc = "Unloaded a module and all the modules that depend on it",
        min = 1,
        max = 1
    )
    public void unload(CommandContext context)
    {
        Module module = this.mm.getModule(context.getString(0));
        if (module == null)
        {
            context.sendMessage("basics", "The given module could not be found!");
        }
        else
        {
            this.mm.unloadModule(module);
            context.sendMessage("basics", "The given module was successfully unloaded!");
        }
    }
}
