package de.cubeisland.engine.stats.storage;

import java.sql.Connection;
import java.sql.SQLException;

import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.TableCreator;
import de.cubeisland.engine.core.storage.database.mysql.Keys;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import de.cubeisland.engine.core.util.Version;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

public class TableStats extends TableImpl<StatsModel> implements TableCreator<StatsModel>
{
    public static TableStats TABLE_STATS;

    private TableStats(String prefix)
    {
        super(prefix + "stats");
        IDENTITY = Keys.identity(this, this.KEY);
        PRIMARY_KEY = Keys.uniqueKey(this, this.KEY);
    }

    public final Identity<StatsModel, UInteger> IDENTITY;
    public final UniqueKey<StatsModel> PRIMARY_KEY;

    public final TableField<StatsModel, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<StatsModel, String> STAT = createField("stat", SQLDataType.VARCHAR.length(20), this);

    public static TableStats initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_STATS = new TableStats(config.tablePrefix);
        return TABLE_STATS;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`key` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`stat` varchar(20) DEFAULT NULL,\n" +
                                        "PRIMARY KEY (`key`),\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }
}
