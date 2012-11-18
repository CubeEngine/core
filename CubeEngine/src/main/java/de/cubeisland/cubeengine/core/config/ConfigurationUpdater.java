package de.cubeisland.cubeengine.core.config;

import java.util.Map;

/**
 * This ConfigurationUpdater can be used to update a configuration from an older revision.
 */
public interface ConfigurationUpdater
{
    public Map<String, Object> update(Map<String, Object> loadedConfig, int fromRevision);
}
