package de.cubeisland.cubeengine.core;

import org.bukkit.configuration.Configuration;

/**
 *
 * @author Faithcaio
 */
class CubeCoreConfiguration {
    
    public final String   core_database_host;            
    public final short    core_database_port;
    public final String   core_database_user;
    public final String   core_database_pass;
    public final String   core_database_name;

    public CubeCoreConfiguration(Configuration config)
    {
        this.core_database_host = config.getString("core.database.host");
        this.core_database_port = ((short)config.getInt("core.database.port"));
        this.core_database_user = config.getString("core.database.user");
        this.core_database_pass = config.getString("core.database.pass");
        this.core_database_name = config.getString("core.database.name");
    }
}
