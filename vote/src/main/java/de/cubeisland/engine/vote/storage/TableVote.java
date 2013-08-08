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
package de.cubeisland.engine.vote.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import org.jooq.types.UShort;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableVote extends TableImpl<VoteModel> implements TableCreator<VoteModel>
{
    public static TableVote TABLE_VOTE;

    private TableVote(String prefix)
    {
        super(prefix + "votes");
        IDENTITY = Keys.identity(this, this.USERID);
        PRIMARY_KEY = Keys.uniqueKey(this, this.USERID);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.PRIMARY_KEY, this, this.USERID);
    }

    public static TableVote initTable(Database database)
    {
        if (TABLE_VOTE != null) throw new IllegalStateException();
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_VOTE = new TableVote(config.tablePrefix);
        return TABLE_VOTE;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`userid` int(10) unsigned NOT NULL,\n" +
                                        "`lastvote` datetime NOT NULL,\n" +
                                        "`voteamount` smallint(5) unsigned NOT NULL,)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
        connection.prepareStatement("ALTER TABLE " + this.getName() +
                                        "\nADD FOREIGN KEY `f_user`(`user_id`) " +
                                        "REFERENCES " + TABLE_USER.getName() + "(`userid`) ON UPDATE CASCADE ON DELETE CASCADE;");
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<VoteModel, UInteger> IDENTITY;
    public final UniqueKey<VoteModel> PRIMARY_KEY;
    public final ForeignKey<VoteModel, UserEntity> FOREIGN_USER;

    public final TableField<VoteModel, UInteger> USERID = createField("userid", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<VoteModel, Timestamp> LASTVOTE = createField("lastvote", SQLDataType.TIMESTAMP, this);
    public final TableField<VoteModel, UShort> VOTEAMOUNT = createField("voteamount", SQLDataType.SMALLINTUNSIGNED, this);

    @Override
    public Identity<VoteModel, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<VoteModel> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<VoteModel>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY);
    }

    @Override
    public List<ForeignKey<VoteModel, ?>> getReferences() {
        return Arrays.<ForeignKey<VoteModel, ?>>asList(FOREIGN_USER);
    }

    @Override
    public Class<VoteModel> getRecordType() {
        return VoteModel.class;
    }
}
