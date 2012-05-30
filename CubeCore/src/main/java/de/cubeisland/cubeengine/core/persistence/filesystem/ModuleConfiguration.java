package de.cubeisland.cubeengine.core.persistence.filesystem;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.module.Module;
import java.lang.reflect.Field;

/**
 *
 * @author Faithcaio
 */
public abstract class ModuleConfiguration
{
    protected final CubeConfiguration config;
    private final Module module;

    public ModuleConfiguration(Module module)
    {
        this.module = module;
        config = CubeCore.getInstance().getFileManager().getModuleConfig(module);
    }

    public CubeConfiguration getCubeConfig()
    {
        return this.config;
    }
    
    public void loadConfiguration(Class<? extends ModuleConfiguration> moduleConfig)
    {
        for (Field field : moduleConfig.getFields())
        {
            this.loadConfigElement(field);
        }
        config.safeSave();
    }

    private void loadConfigElement(Field field)
    {
        try
        {
            if (!field.isAnnotationPresent(Option.class))
            {
                return;
            }
            String path = field.getAnnotation(Option.class).value();
            config.addDefault(path, field.get(this));
            Object configElem = config.get(path);
            field.set(this, configElem);
        }
        catch (IllegalAccessException ex)
        {
        }
    }

    public void saveConfiguration(Class<? extends ModuleConfiguration> moduleConfig)
    {
        for (Field field : moduleConfig.getFields())
        {
            this.saveConfigElement(field);
        }
        config.safeSave();
    }

    private void saveConfigElement(Field field)
    {
        try
        {
            if (!field.isAnnotationPresent(Option.class))
            {
                return;
            }
            String path = field.getAnnotation(Option.class).value();
            config.set(path, field.get(this));
        }
        catch (IllegalAccessException ex)
        {
        }
    }
    
    /**
     * this.loadConfiguration(this.getClass())
     */
    abstract public void loadConfig();
    /**
     * this.saveConfiguration(this.getClass())
     */
    abstract public void saveConfig();
    
    
}
