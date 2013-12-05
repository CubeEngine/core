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
package de.cubeisland.engine.basics.storage;

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
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableMail extends TableImpl<Mail> implements TableCreator<Mail>
{
    public static TableMail TABLE_MAIL;

    private TableMail(String prefix)
    {
        super(prefix + "mail");
        IDENTITY = Keys.identity(this, this.KEY);
        PRIMARY_KEY = Keys.uniqueKey(this, this.KEY);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.getPrimaryKey(), this, this.USERID);
        FOREIGN_SENDER = Keys.foreignKey(TABLE_USER.getPrimaryKey(), this, this.SENDERID);
    }

    public static TableMail initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_MAIL = new TableMail(config.tablePrefix);
        return TABLE_MAIL;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`key` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`message` varchar(100) NOT NULL,\n" +
                                        "`userId` int(10) unsigned NOT NULL,\n" +
                                        "`senderId` int(10) unsigned DEFAULT NULL,\n" +
                                        "PRIMARY KEY (`key`),\n" +
                                        "FOREIGN KEY f_user (`userId`) REFERENCES " + TABLE_USER.getName() + " (`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY f_sender (`senderId`) REFERENCES " + TABLE_USER.getName() + " (`key`) ON UPDATE CASCADE ON DELETE CASCADE)" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<Mail, UInteger> IDENTITY;
    public final UniqueKey<Mail> PRIMARY_KEY;
    public final ForeignKey<Mail, UserEntity> FOREIGN_USER;
    public final ForeignKey<Mail, UserEntity> FOREIGN_SENDER;

    public final TableField<Mail, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final org.jooq.TableField<Mail, java.lang.String> MESSAGE = createField("message", SQLDataType.VARCHAR.length(100), this);
    public final TableField<Mail, UInteger> USERID = createField("userId", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<Mail, UInteger> SENDERID = createField("senderId", SQLDataType.INTEGERUNSIGNED, this);

    @Override
    public Identity<Mail, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<Mail> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<Mail>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY);
    }

    @Override
    public List<ForeignKey<Mail, ?>> getReferences() {
        return Arrays.<ForeignKey<Mail, ?>>asList(FOREIGN_USER, FOREIGN_SENDER);
    }

    @Override
    public Class<Mail> getRecordType() {
        return Mail.class;
    }
}
