package de.cubeisland.cubeengine.core.config.codec;

import de.cubeisland.cubeengine.core.config.InvalidConfigurationException;
import de.cubeisland.cubeengine.core.config.MultiConfiguration;
import de.cubeisland.cubeengine.core.config.annotations.Revision;
import de.cubeisland.cubeengine.core.config.annotations.Updater;
import de.cubeisland.cubeengine.core.config.node.IntNode;
import de.cubeisland.cubeengine.core.config.node.MapNode;

import java.io.File;
import java.io.InputStream;

/**
 * This abstract Codec can be implemented to read and write configurations that allow child-configs
 */
public abstract class MultiConfigurationCodec<Config extends MultiConfiguration> extends ConfigurationCodec<Config>
{
    public void saveChildConfig(Config parentConfig, Config config, File file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            MultiCodecContainer container = new MultiCodecContainer(this);
            Revision a_revision = config.getClass().getAnnotation(Revision.class);
            container.values = MapNode.emptyMap();
            if (a_revision != null)
            {
                container.values.setNodeAt("revision", PATH_SEPARATOR, new IntNode(a_revision.value()));
            }
            container.fillFromFields(parentConfig, config, container.values);
            container.saveIntoFile(config, file);
        }
        catch (Exception ex)
        {
            throw new InvalidConfigurationException("Error while saving Configuration!", ex);
        }
    }

    /**
     * Loads in the given configuration using the InputStream
     *
     * @param config the config to load
     * @param is the InputStream to load from
     */
    public void loadChildConfig(MultiConfiguration config, InputStream is) throws InstantiationException, IllegalAccessException
    {
        MultiCodecContainer container = new MultiCodecContainer(this);
        container.fillFromInputStream(is);
        Revision a_revision = config.getClass().getAnnotation(Revision.class);
        if (a_revision != null)
        {
            if (config.getClass().isAnnotationPresent(Updater.class))
            {
                if (a_revision.value() > container.revision)
                {
                    Updater updater = config.getClass().getAnnotation(Updater.class);
                    updater.value().newInstance().update(container.values, container.revision);
                }
            }
        }
        container.dumpIntoFields(config, container.values, config.getParent());
    }
}
