package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Codec;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Comment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;

/**
 *
 * @author Anselm Brehme
 */
@Codec("yml")
public class DatabaseConfiguration extends Configuration
{
    @Option("host")
    @Comment("The host to connect with. Default: localhost")
    public String host = "localhost";
    
    @Option("port")
    @Comment("The port to connect with. Default 3306")
    public short port = 3306;
    
    @Option("user")
    @Comment("The user using the database")
    public String user = "minecraft";
    
    @Option("password")
    @Comment("The password for the specified user")
    public String pass = "12345678";
    
    @Option("database")
    @Comment("The name of the database")
    public String database = "minecraft";
    
    @Option("tableprefix")
    @Comment("The tableprefix to use for all CubeEngine tables")
    public String tableprefix = "cube_";
}