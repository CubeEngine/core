package de.cubeisland.cubeengine.core.config;

import java.util.Map;

/**
 *
 * @author Anselm Brehme
 */
public interface ConfigurationUpdater
{
    public Map<String, Object> update(Map<String, Object> loadedConfig, int fromRevision);
}