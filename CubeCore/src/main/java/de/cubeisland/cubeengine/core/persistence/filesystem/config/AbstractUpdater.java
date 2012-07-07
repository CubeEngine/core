package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import java.util.LinkedHashMap;

/**
 *
 * @author Faithcaio
 */
public abstract class AbstractUpdater
{
    public abstract LinkedHashMap<String, Object> update(LinkedHashMap<String, Object> loadedConfig, int fromRevision);
}
