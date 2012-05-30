package de.cubeisland.cubeengine.core.persistence.filesystem;

import de.cubeisland.cubeengine.core.module.Module;
import java.lang.reflect.Field;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Faithcaio
 */
public class ModuleConfiguration
{
    protected final FileConfiguration config;
    private final Module module;

    public ModuleConfiguration(Module module)
    {
        this.module = module;
        config = module.getConfig();
    }

    public void loadConfiguration(Class<? extends ModuleConfiguration> moduleConfig)
    {
        for (Field field : moduleConfig.getFields())
        {
            this.loadConfigElement(field);
        }
        module.saveConfig();
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
        module.saveConfig();
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
}
