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
package de.cubeisland.engine.core.world;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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

public class TableWorld extends TableImpl<WorldEntity> implements TableCreator<WorldEntity>
{
    public static TableWorld TABLE_WORLD;

    private TableWorld(String prefix)
    {
        super(prefix + "worlds");
        INDENTIFY_WORLD = Keys.identity(this, this.KEY);
        PRIMARY_KEY = Keys.uniqueKey(this, this.KEY);
        UNIQUE_UUID = Keys.uniqueKey(this, this.WORLDUUID);
    }

    public static TableWorld initTable(Database database)
    {
        if (TABLE_WORLD != null) throw new IllegalStateException();
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_WORLD = new TableWorld(config.tablePrefix);
        return TABLE_WORLD;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " ("+
                                        "`key` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`worldName` varchar(64) NOT NULL,\n" +
                                        "`worldUUID` varchar(64) UNIQUE NOT NULL,\n" +
                                        "PRIMARY KEY (`key`)\n" +
                                        "UNIQUE KEY `worldUUID` (`worldUUID`)) " +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<WorldEntity, UInteger> INDENTIFY_WORLD;
    public final UniqueKey<WorldEntity> PRIMARY_KEY;
    public final UniqueKey<WorldEntity> UNIQUE_UUID;

    public final TableField<WorldEntity, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<WorldEntity, String> WORLDNAME = createField("worldName", SQLDataType.VARCHAR.length(64), this);
    public final TableField<WorldEntity, String> WORLDUUID = createField("worldUUID", SQLDataType.VARCHAR.length(64), this);

    @Override
    public Identity<WorldEntity, UInteger> getIdentity() {
        return this.INDENTIFY_WORLD;
    }

    @Override
    public UniqueKey<WorldEntity> getPrimaryKey() {
        return this.PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<WorldEntity>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY, UNIQUE_UUID);
    }
}
