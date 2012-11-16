package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConfiguration;

/**
 * MYSQLDatabaseConfig containing all needed information to connect to a MYSQLDatabse
 */
@Codec("yml")
public class MySQLDatabaseConfiguration extends DatabaseConfiguration
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
    public String tablePrefix = "cube_";

    @Override
    public Class<? extends Database> getDatabaseClass()
    {
        return MySQLDatabase.class;
    }
}
