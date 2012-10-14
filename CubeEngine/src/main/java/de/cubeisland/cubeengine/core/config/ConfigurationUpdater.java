package de.cubeisland.cubeengine.core.config;

import java.util.Map;

//TODO DOCU
public interface ConfigurationUpdater
{
    public Map<String, Object> update(Map<String, Object> loadedConfig, int fromRevision);
}