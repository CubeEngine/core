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
import java.util.UUID;

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.storage.database.TableUpdateCreator;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.types.UInteger;

import static org.jooq.impl.SQLDataType.BIGINT;
import static org.jooq.impl.SQLDataType.VARCHAR;

public class TableWorld extends AutoIncrementTable<WorldEntity, UInteger> implements TableUpdateCreator<WorldEntity>
{
    public static TableWorld TABLE_WORLD;
    public final TableField<WorldEntity, UInteger> KEY = createField("key", U_INTEGER.nullable(false), this);
    public final TableField<WorldEntity, String> WORLDNAME = createField("worldName", VARCHAR.length(64).nullable(false), this);
    public final TableField<WorldEntity, Long> LEAST = createField("UUIDleast", BIGINT.nullable(false),this);
    public final TableField<WorldEntity, Long> MOST = createField("UUIDmost", BIGINT.nullable(false), this);

    public TableWorld(String prefix)
    {
        super(prefix + "worlds", new Version(2));
        this.setAIKey(KEY);
        this.addUniqueKey(LEAST, MOST);
        this.addFields(KEY, WORLDNAME, LEAST, MOST);
        TABLE_WORLD = this;
    }

    @Override
    public void update(Connection connection, Version dbVersion) throws SQLException
    {
        if (dbVersion.getMajor() == 1)
        {
            connection.prepareStatement("ALTER TABLE " + this.getName() +
                                            "\nADD COLUMN `UUIDleast` BIGINT NOT NULL," +
                                            "\nADD COLUMN `UUIDmost` BIGINT NOT NULL").execute();
            ResultSet resultSet = connection.prepareStatement(
                "SELECT `key`, `worldUUID` FROM " + this.getName()).executeQuery();
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
            connection.prepareStatement(
                "ALTER TABLE " + this.getName() + " ADD UNIQUE `uuid` ( `UUIDleast` , `UUIDmost` )").execute();
            connection.prepareStatement("ALTER TABLE " + this.getName() + " DROP `worldUUID`").execute();
        }
        else
        {
            throw new IllegalStateException("Unknown old TableVersion!");
        }
    }

    @Override
    public Class<WorldEntity> getRecordType()
    {
        return WorldEntity.class;
    }
}
