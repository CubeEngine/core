package de.cubeisland.cubeengine.core.persistence.filesystem;

/**
 *
 * @author Faithcaio
 */
public class DatabaseConfiguration extends Configuration
{
    @Option("host")
    public String host = "localhost";

    @Option("port")
    public short port = 3306;

    @Option("user")
    public String user = "minecraft";

    @Option("password")
    public String pass = "12345678";

    @Option("database")
    public String database = "minecraft";
    
    @Option("tableprefix")
    public String tableprefix = "cube_";
}
