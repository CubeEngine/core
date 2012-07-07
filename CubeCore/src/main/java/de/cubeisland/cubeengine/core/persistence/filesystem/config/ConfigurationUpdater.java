package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import java.util.LinkedHashMap;

/**
 *
 * @author Faithcaio
 */
public interface ConfigurationUpdater
{
    public LinkedHashMap<String, Object> update(LinkedHashMap<String, Object> loadedConfig, int fromRevision);
}
