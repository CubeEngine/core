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
import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.EbeanServer;
import de.cubeisland.engine.basics.storage.BasicsUserEntity.BasicsUserUpdater;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.DBUpdater;
import de.cubeisland.engine.core.storage.database.DatabaseUpdater;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Version;

@Entity
@Table(name = "basicuser")
@DBUpdater(BasicsUserUpdater.class)
public class BasicsUserEntity
{
    @javax.persistence.Version
    static final Version version = new Version(2);

    @Id
    @Attribute(type = AttrType.BIGINT)
    public long id; // Ebean requires this
    /*
     * @Id field is always required
     * @Id field cannot be a foreign key at the same time (except EmbeddedId but i am not sure how it works yet)
     */

    @Column(name = "userid")
    @ManyToOne(optional = false, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "userid") // ebean needs this
    @Attribute(type = AttrType.INT, unsigned = true)
    private UserEntity entity; // User Key
    @Column()
    @Attribute(type = AttrType.DATETIME)
    private Timestamp muted;
    @Column(nullable = false)
    @Attribute(type = AttrType.BOOLEAN)
    private boolean godMode = false;

    public BasicsUserEntity()
    {}

    public BasicsUserEntity(User user)
    {
        this.entity = user.getEntity();
    }

    public void update(EbeanServer ebean)
    {
        if (this.muted != null && this.muted.getTime() < System.currentTimeMillis())
        {
            this.muted = null; // remove muted information as it is no longer needed
        }
        ebean.update(this);
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public UserEntity getEntity()
    {
        return entity;
    }

    public void setEntity(UserEntity entity)
    {
        this.entity = entity;
    }

    public Timestamp getMuted()
    {
        return muted;
    }

    public void setMuted(Timestamp muted)
    {
        this.muted = muted;
    }

    public boolean isGodMode()
    {
        return godMode;
    }

    public void setGodMode(boolean godMode)
    {
        this.godMode = godMode;
    }

    public static class BasicsUserUpdater implements DatabaseUpdater
    {
        @Override
        public void update(Connection connection, Class<?> entityClass, Version dbVersion, Version codeVersion) throws SQLException
        {
            if (codeVersion.getMajor() == 2)
            {
                connection.prepareStatement("RENAME TABLE cube_basicuser TO old_basicuser").execute();
                connection.prepareStatement("CREATE TABLE IF NOT EXISTS `cube_basicuser` \n(" +
                         "id bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                         "userid int(10) unsigned NOT NULL,\n" +
                         "muted datetime null default null,\n" +
                         "godmode tinyint(1) NOT NULL,\n" +
                         "PRIMARY KEY (id),\n" +
                         "FOREIGN KEY f_userid (userid) REFERENCES cube_user(`key`) ON DELETE CASCADE ON UPDATE CASCADE)\n" +
                         "DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                         "COMMENT '2.0.0'").execute() ;
                connection.prepareStatement("INSERT INTO cube_basicuser (userid, muted, godmode) SELECT `key`, muted, godmode FROM old_basicuser").execute();
                connection.prepareStatement("DROP TABLE old_basicuser").execute();
            }
        }
    }
}
