package de.cubeisland.engine.stats.storage;

import java.sql.Connection;
import java.sql.SQLException;

import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.TableCreator;
import de.cubeisland.engine.core.storage.database.mysql.Keys;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import de.cubeisland.engine.core.util.Version;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.stats.storage.TableStats.TABLE_STATS;

public class TableStatsData extends TableImpl<StatsDataModel> implements TableCreator<StatsDataModel>
{
    public static TableStatsData TABLE_STATSDATA;

    private TableStatsData(String prefix)
    {
        super(prefix + "statsdata");
        IDENTITY = Keys.identity(this, this.KEY);
        PRIMARY_KEY = Keys.uniqueKey(this, this.KEY);
        FOREIGN_STAT = Keys.foreignKey(TABLE_STATS.PRIMARY_KEY, this, this.STAT);
    }

    public final Identity<StatsDataModel, UInteger> IDENTITY;
    public final UniqueKey<StatsDataModel> PRIMARY_KEY;
    public final ForeignKey<StatsDataModel, StatsModel> FOREIGN_STAT;

    public final TableField<StatsDataModel, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<StatsDataModel, UInteger> STAT = createField("stat", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<StatsDataModel, String> DATA = createField("data", SQLDataType.VARCHAR.length(64), this);
    
    public static TableStatsData initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_STATSDATA = new TableStatsData(config.tablePrefix);
        return TABLE_STATSDATA;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`key` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`stat` int(10) unsigned NOT NULL,\n" +
                                        "`data` varchar(64) DEFAULT NULL,\n" +
                                        "PRIMARY KEY (`key`),\n" +
                                        "FOREIGN KEY `f_stat`(`stat`) REFERENCES " + TABLE_STATS.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
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
