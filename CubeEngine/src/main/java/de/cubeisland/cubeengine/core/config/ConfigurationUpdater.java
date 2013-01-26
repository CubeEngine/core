package de.cubeisland.cubeengine.core.config;

/**
 * This ConfigurationUpdater can be used to update a configuration from an older revision.
 */
public interface ConfigurationUpdater
{
    public Object update(Object loadedConfig, int fromRevision);
}
