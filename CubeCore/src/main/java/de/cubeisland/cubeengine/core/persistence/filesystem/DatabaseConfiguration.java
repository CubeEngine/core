package de.cubeisland.cubeengine.core.persistence.filesystem;

import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Faithcaio
 */
public class DatabaseConfiguration extends Configuration
{
    @Option("mysql.host")
    public String mysql_host = "localhost";
    @Option("mysql.port")
    public short mysql_port = 3306;
    @Option("mysql.user")
    public String mysql_user = "minecraft";
    @Option("mysql.password")
    public String mysql_pass = "12345678";
    @Option("mysql.database")
    public String mysql_database = "minecraft";
    @Option("mysql.tableprefix")
    public String mysql_tableprefix = "cube_";

    public DatabaseConfiguration(YamlConfiguration config, File file)
    {
        super(config, file);
    }
}
