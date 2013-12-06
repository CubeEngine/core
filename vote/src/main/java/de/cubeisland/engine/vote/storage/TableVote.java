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

import de.cubeisland.engine.core.storage.database.Table;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableVote extends Table<VoteModel>
{
    public static TableVote TABLE_VOTE;

    public TableVote(String prefix)
    {
        super(prefix + "votes", new Version(1));
        this.setPrimaryKey(USERID);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), USERID);
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`userid` int(10) unsigned NOT NULL,\n" +
                                        "`lastvote` datetime NOT NULL,\n" +
                                        "`voteamount` smallint(5) unsigned NOT NULL,\n" +
                                        "PRIMARY KEY (`userid`)," +
                                        "FOREIGN KEY `f_user`(`userid`) REFERENCES " + TABLE_USER.getName() +" (`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    public final TableField<VoteModel, UInteger> USERID = createField("userid", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<VoteModel, Timestamp> LASTVOTE = createField("lastvote", SQLDataType.TIMESTAMP, this);
    public final TableField<VoteModel, UShort> VOTEAMOUNT = createField("voteamount", SQLDataType.SMALLINTUNSIGNED, this);

    @Override
    public Class<VoteModel> getRecordType() {
        return VoteModel.class;
    }
}
