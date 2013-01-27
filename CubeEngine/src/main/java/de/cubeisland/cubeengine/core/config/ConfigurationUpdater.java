package de.cubeisland.cubeengine.core.config;

import de.cubeisland.cubeengine.core.config.node.Node;

/**
 * This ConfigurationUpdater can be used to update a configuration from an older revision.
 */
public interface ConfigurationUpdater
{
    public Object update(Node loadedConfig, int fromRevision);
}
