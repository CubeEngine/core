/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.stats.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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

    public final TableField<StatsModel, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED.length(10), this);
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
                                        "`stat` varchar(20) NOT NULL,\n" +
                                        "PRIMARY KEY (`key`))\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    @Override
    public Identity<StatsModel, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<StatsModel> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<StatsModel>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY);
    }

    @Override
    public List<ForeignKey<StatsModel, ?>> getReferences() {
        return Arrays.asList();
    }

    @Override
    public Class<StatsModel> getRecordType() {
        return StatsModel.class;
    }
}
