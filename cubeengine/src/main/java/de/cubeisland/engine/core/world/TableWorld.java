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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.TableUpdateCreator;
import de.cubeisland.engine.core.storage.database.mysql.Keys;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import de.cubeisland.engine.core.util.Version;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

public class TableWorld extends TableImpl<WorldEntity> implements TableUpdateCreator<WorldEntity>
{
    public static TableWorld TABLE_WORLD;

    private TableWorld(String prefix)
    {
        super(prefix + "worlds");
        INDENTIFY_WORLD = Keys.identity(this, this.KEY);
        PRIMARY_KEY = Keys.uniqueKey(this, this.KEY);
        UNIQUE_UUID = Keys.uniqueKey(this, this.LEAST, this.MOST);
    }

    public static TableWorld initTable(Database database)
    {
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
                                        "`UUIDleast` bigint NOT NULL,\n" +
                                        "`UUIDmost` bigint NOT NULL,\n" +
                                        "PRIMARY KEY (`key`)," +
                                        "UNIQUE `u_UUID` (`UUIDleast`, `UUIDmost`)) " +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    @Override
    public void update(Connection connection, Version dbVersion) throws SQLException
    {
        if (dbVersion.getMajor() == 1)
        {
            connection.prepareStatement("ALTER TABLE " + this.getName() +
                                              "\nADD COLUMN `UUIDleast` BIGINT NOT NULL," +
                                              "\nADD COLUMN `UUIDmost` BIGINT NOT NULL").execute();
            ResultSet resultSet = connection.prepareStatement("SELECT `key`, `worldUUID` FROM " + this.getName()).executeQuery();
            connection.setAutoCommit(false);
            PreparedStatement updateBatch = connection.prepareStatement("UPDATE " + this.getName() +
                            "\nSET `UUIDleast` = ?, `UUIDmost` = ?" +
                            "\nWHERE `key` = ?");
            while (resultSet.next())
            {
                UUID uuid = UUID.fromString(resultSet.getString("worldUUID"));
                updateBatch.setObject(1, uuid.getLeastSignificantBits());
                updateBatch.setObject(2, uuid.getMostSignificantBits());
                updateBatch.setObject(3, resultSet.getLong("key"));
                updateBatch.addBatch();
            }
            updateBatch.executeBatch();
            connection.setAutoCommit(true);
            connection.prepareStatement("ALTER TABLE " + this.getName() + " ADD UNIQUE `uuid` ( `UUIDleast` , `UUIDmost` )").execute();
            connection.prepareStatement("ALTER TABLE " + this.getName() + " DROP `worldUUID`").execute();
        }
        else
        {
            throw new IllegalStateException("Unknown old TableVersion!");
        }
    }

    private static final Version version = new Version(2);

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

    public final TableField<WorldEntity, Long> LEAST = createField("UUIDleast", SQLDataType.BIGINT, this);
    public final TableField<WorldEntity, Long> MOST = createField("UUIDmost", SQLDataType.BIGINT, this);

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

    @Override
    public Class<WorldEntity> getRecordType() {
        return WorldEntity.class;
    }
}
