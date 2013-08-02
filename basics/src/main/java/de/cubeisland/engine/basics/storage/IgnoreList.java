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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.cubeisland.engine.basics.storage.IgnoreList.IgnoreListUpdater;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.DBUpdater;
import de.cubeisland.engine.core.storage.database.DatabaseUpdater;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Version;

@Entity
@Table(name = "ignorelist")
@DBUpdater(IgnoreListUpdater.class)
public class IgnoreList
{
    @javax.persistence.Version
    static final Version version = new Version(2);

    @Id
    @Attribute(type = AttrType.BIGINT)
    public long id; // Ebean requires this
    @Column(name = "userid")
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "senderId") // ebean needs this
    @Attribute(type = AttrType.INT, unsigned = true)
    public UserEntity userEntity;
    @Column(name = "ignoreid", nullable = false)
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "ignoreid") // ebean needs this
    @Attribute(type = AttrType.INT, unsigned = true)
    public UserEntity ignore;

    public IgnoreList()
    {
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public IgnoreList(User user, User ignore)
    {
        this.userEntity = user.getEntity();
        this.ignore = ignore.getEntity();
    }

    public UserEntity getUserEntity()
    {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity)
    {
        this.userEntity = userEntity;
    }

    public UserEntity getIgnore()
    {
        return ignore;
    }

    public void setIgnore(UserEntity ignore)
    {
        this.ignore = ignore;
    }

    public static class IgnoreListUpdater implements DatabaseUpdater
    {
        @Override
        public void update(Connection connection, Class<?> entityClass, Version dbVersion, Version codeVersion) throws SQLException
        {
            if (codeVersion.getMajor() == 2)
            {
                connection.prepareStatement("RENAME TABLE cube_ignorelist TO old_ignorelist").execute();
                connection.prepareStatement("CREATE TABLE IF NOT EXISTS cube_ignorelist \n(" +
                                                "id bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                                                "userid int(10) unsigned NOT NULL,\n" +
                                                "ignoreid int(10) unsigned NOT NULL,\n" +
                                                "PRIMARY KEY (id),\n" +
                                                "FOREIGN KEY f_userid (userid) REFERENCES cube_user(`key`) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
                                                "FOREIGN KEY f_ignoreid (ignoreid) REFERENCES cube_user(`key`) ON DELETE CASCADE ON UPDATE CASCADE)\n" +
                                                "DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                                "COMMENT '2.0.0'").execute() ;
                connection.prepareStatement("INSERT INTO cube_ignorelist (userid, ignoreid) SELECT `key`, `ignore` FROM old_ignorelist").execute();
                connection.prepareStatement("DROP TABLE old_ignorelist").execute();
            }
        }
    }
}
