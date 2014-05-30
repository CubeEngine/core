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
package de.cubeisland.engine.core.user;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.TableUpdateCreator;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import de.cubeisland.engine.core.util.McUUID;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.types.UInteger;

import static org.jooq.impl.SQLDataType.*;
import static org.jooq.util.mysql.MySQLDataType.DATETIME;

public class TableUser extends AutoIncrementTable<UserEntity, UInteger> implements TableUpdateCreator<UserEntity>
{
    public static TableUser TABLE_USER;
    public final TableField<UserEntity, UInteger> KEY = createField("key", U_INTEGER.nullable(false), this);
    public final TableField<UserEntity, String> LASTNAME = createField("lastname", VARCHAR.length(16).nullable(false),
                                                                       this);
    public final TableField<UserEntity, Boolean> NOGC = createField("nogc", BOOLEAN.nullable(false), this);
    public final TableField<UserEntity, Timestamp> LASTSEEN = createField("lastseen", DATETIME.nullable(false), this);
    public final TableField<UserEntity, byte[]> PASSWD = createField("passwd", VARBINARY.length(128), this);
    public final TableField<UserEntity, Timestamp> FIRSTSEEN = createField("firstseen", DATETIME.nullable(false), this);
    public final TableField<UserEntity, String> LANGUAGE = createField("language", VARCHAR.length(5), this);
    public final TableField<UserEntity, Long> LEAST = createField("UUIDleast", BIGINT.nullable(false), this);
    public final TableField<UserEntity, Long> MOST = createField("UUIDmost", BIGINT.nullable(false), this);

    public TableUser(String prefix)
    {
        super(prefix + "user", new Version(2));
        this.setAIKey(this.KEY);
        this.addUniqueKey(LEAST, MOST);
        this.addFields(KEY, LASTNAME, NOGC, LASTSEEN, PASSWD, FIRSTSEEN, LANGUAGE, LEAST, MOST);
        TABLE_USER = this;
    }

    public static TableUser initTable(Database database)
    {
        if (TABLE_USER == null)
        {
            MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
            TABLE_USER = new TableUser(config.tablePrefix);
        }
        return TABLE_USER;
    }

    @Override
    public Class<UserEntity> getRecordType()
    {
        return UserEntity.class;
    }

    @Override
    public void update(Connection connection, Version dbVersion) throws SQLException
    {
        if (dbVersion.getMajor() == 1)
        {
            CubeEngine.getLog().info("Updating {} to Version 2", this.getName());

            CubeEngine.getLog().info("Get all names with missing UUID values");
            ResultSet resultSet = connection.prepareStatement("SELECT `player` FROM " + this.getName()).executeQuery();
            List<String> list = new ArrayList<>();
            while (resultSet.next())
            {
                String lastname = resultSet.getString("player");
                list.add(lastname);
            }
            CubeEngine.getLog().info("Query MojangAPI to get UUIDs");
            Map<String, UUID> uuids = McUUID.getUUIDForNames(list);

            CubeEngine.getLog().info("Adding UUID columns");
            connection.prepareStatement("ALTER TABLE " + this.getName() +
                                            "\nADD COLUMN `UUIDleast` BIGINT NOT NULL," +
                                            "\nADD COLUMN `UUIDmost` BIGINT NOT NULL").execute();

            PreparedStatement stmt = connection.prepareStatement("UPDATE " + this.getName() +
                                                                     " SET `UUIDleast`=? , `UUIDmost`=?" +
                                                                     " WHERE `player` = ?");
            CubeEngine.getLog().info("Update UUIDs in database");
            for (Entry<String, UUID> entry : uuids.entrySet())
            {
                if (entry.getValue() == null)
                {
                    PreparedStatement deleteStmt = connection.prepareStatement(
                        "DELETE FROM " + this.getName() + " WHERE `player` = ?");
                    deleteStmt.setString(1, entry.getKey());
                    deleteStmt.execute();
                    continue;
                }
                stmt.setLong(1, entry.getValue().getLeastSignificantBits());
                stmt.setLong(2, entry.getValue().getMostSignificantBits());
                stmt.setString(3, entry.getKey());
                stmt.addBatch();
            }
            stmt.executeBatch();

            CubeEngine.getLog().info("Create unique index on uuids and rename to lastname");
            connection.prepareStatement("CREATE UNIQUE INDEX `uuid` ON " + this.getName() +
                                            " (`UUIDleast`,`UUIDmost`)").execute();

            CubeEngine.getLog().info("Drop unique index on player and rename to lastname");
            connection.prepareStatement("DROP INDEX `player` ON " + this.getName()).execute();
            connection.prepareStatement("ALTER TABLE " + this.getName() +
                                            " CHANGE `player` `lastname` VARCHAR(16) " +
                                            "CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL").execute();
        }
    }
}
