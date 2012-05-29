package de.cubeisland.cubeengine.core.persistence.filesystem;

import java.lang.reflect.Field;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Faithcaio
 */
public class ModuleConfiguration
{
    protected final YamlConfiguration config;

    public ModuleConfiguration()
    {
        config = new YamlConfiguration();
    }

    private void loadConfiguration(Class<? extends ModuleConfiguration> moduleConfig)
    {
        for (Field field : moduleConfig.getFields())
        {
            this.loadConfigElement(field);
        }
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
        catch (IllegalAccessException e)
        {
        }
    }
}
