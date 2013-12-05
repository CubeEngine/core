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
package de.cubeisland.engine.powersigns.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.TableCreator;
import de.cubeisland.engine.core.storage.database.mysql.Keys;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.core.world.WorldEntity;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;
import org.jooq.util.mysql.MySQLDataType;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public class TablePowerSign extends TableImpl<PowerSignModel> implements TableCreator<PowerSignModel>
{
    public static TablePowerSign TABLE_POWER_SIGN;

    private TablePowerSign(String prefix)
    {
        super(prefix + "powersign");
        IDENTITY = Keys.identity(this, this.ID);
        PRIMARY_KEY = Keys.uniqueKey(this, this.ID);
        UNIQUE_LOC = Keys.uniqueKey(this, this.WORLD, this.X, this.Y, this.Z);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.getPrimaryKey(), this, this.OWNER_ID);
        FOREIGN_WORLD = Keys.foreignKey(TABLE_WORLD.PRIMARY_KEY, this, this.WORLD);
    }

    public static TablePowerSign initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_POWER_SIGN = new TablePowerSign(config.tablePrefix);
        return TABLE_POWER_SIGN;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`owner_id` int(10) unsigned DEFAULT NULL,\n" +
                                        "`PSID` varchar(6) DEFAULT NULL,\n" +
                                        "`world` int(10) unsigned DEFAULT NULL,\n" +
                                        "`x` int(10) NOT NULL,\n" +
                                        "`y` int(10) NOT NULL,\n" +
                                        "`z` int(10) NOT NULL,\n" +
                                        "`chunkx` int(10) NOT NULL,\n" +
                                        "`chunkz` int(10) NOT NULL,\n" +
                                        "`data` TEXT DEFAULT NULL,\n" +
                                        "PRIMARY KEY (`id`),\n" +
                                        "UNIQUE KEY `loc` (`world`,`x`,`y`,`z`),\n" +
                                        "FOREIGN KEY `f_user`(`owner_id`) REFERENCES " + TABLE_USER.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY `f_world`(`world`) REFERENCES " + TABLE_WORLD.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<PowerSignModel, UInteger> IDENTITY;
    public final UniqueKey<PowerSignModel> PRIMARY_KEY;
    public final UniqueKey<PowerSignModel> UNIQUE_LOC;
    public final ForeignKey<PowerSignModel, UserEntity> FOREIGN_USER;
    public final ForeignKey<PowerSignModel, WorldEntity> FOREIGN_WORLD;

    public final TableField<PowerSignModel, UInteger> ID = createField("id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<PowerSignModel, UInteger> OWNER_ID = createField("owner_id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<PowerSignModel, String> PSID = createField("psid", SQLDataType.VARCHAR.length(6), this);
    public final TableField<PowerSignModel, UInteger> WORLD = createField("world", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<PowerSignModel, Integer> X = createField("x", SQLDataType.INTEGER, this);
    public final TableField<PowerSignModel, Integer> Y = createField("y", SQLDataType.INTEGER, this);
    public final TableField<PowerSignModel, Integer> Z = createField("z", SQLDataType.INTEGER, this);

    public final TableField<PowerSignModel, Integer> CHUNKX = createField("chunkx", SQLDataType.INTEGER, this);
    public final TableField<PowerSignModel, Integer> CHUNKZ = createField("chunkz", SQLDataType.INTEGER, this);

    public final TableField<PowerSignModel, String> DATA = createField("data", MySQLDataType.TEXT, this);

    @Override
    public Identity<PowerSignModel, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<PowerSignModel> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<PowerSignModel>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY, UNIQUE_LOC);
    }

    @Override
    public List<ForeignKey<PowerSignModel, ?>> getReferences() {
        return Arrays.<ForeignKey<PowerSignModel, ?>>asList(FOREIGN_USER, FOREIGN_WORLD);
    }

    @Override
    public Class<PowerSignModel> getRecordType() {
        return PowerSignModel.class;
    }
}
