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
package de.cubeisland.engine.conomy.account.storage;

import java.sql.Connection;
import java.sql.SQLException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Version;

@Entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
public class AccountModel
{
    @javax.persistence.Version
    static final Version version = new Version(2);

    @Id
    @Column()
    @Attribute(type = AttrType.INT, unsigned = true)
    private long id;
    @Column(name = "user_id")
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "user_id")
    @Attribute(type = AttrType.INT, unsigned = true)
    private UserEntity userEntity;
    @Column(length = 64)
    @Attribute(type = AttrType.VARCHAR)
    private String name;
    @Column(nullable = false)
    @Attribute(type = AttrType.BIGINT)
    private long value;
    @Column()
    @Attribute(type = AttrType.TINYINT)
    private int mask = 0;

    public AccountModel()
    {}

    public AccountModel(User user, String name, long balance, boolean hidden, boolean needsInvite)
    {
        this.userEntity = user == null ? null : user.getEntity();
        this.name = name;
        this.value = balance;
        this.mask = (byte)((hidden ? 1 : 0) + (needsInvite ? 2 : 0));
    }

    public AccountModel(User user, String name, long balance, boolean hidden)
    {
        this(user, name, balance, hidden, false);
    }

    public boolean needsInvite()
    {
        return (this.mask & 2) == 2;
    }

    public boolean isHidden()
    {
        return (this.mask & 1) == 1;
    }

    public void setNeedsInvite(boolean set)
    {
        if (set)
        {
            this.mask |= 2;
        }
        else
        {
            this.mask &= ~2;
        }
    }

    public void setHidden(boolean set)
    {
        if (set)
        {
            this.mask |= 1;
        }
        else
        {
            this.mask &= ~1;
        }
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public UserEntity getUserEntity()
    {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity)
    {
        this.userEntity = userEntity;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getValue()
    {
        return value;
    }

    public void setValue(long value)
    {
        this.value = value;
    }

    public int getMask()
    {
        return mask;
    }

    public void setMask(int mask)
    {
        this.mask = mask;
    }

    public static class AccountUpdater
    {
        public void update(Connection connection, Class<?> entityClass, Version dbVersion, Version codeVersion) throws SQLException
        {
            if (codeVersion.getMajor() == 2)
            {
                // Copy Table but do not delete temp_table yet
                connection.prepareStatement("RENAME TABLE cube_accounts TO old_accounts").execute();
                connection.prepareStatement("CREATE TABLE `cube_accounts` (  " +
                                                "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n  " +
                                                "`user_id` int(10) unsigned DEFAULT NULL,\n  " +
                                                "`name` varchar(64) DEFAULT NULL,\n  " +
                                                "`value` bigint(20) NOT NULL,\n  " +
                                                "`mask` tinyint(4) DEFAULT NULL,\n  " +
                                                "PRIMARY KEY (`id`),  \n  " +
                                                "UNIQUE KEY `user_id` (`user_id`,`name`),  \n  " +
                                                "FOREIGN KEY f_userid(`user_id`) REFERENCES `cube_user` (`key`) ON DELETE CASCADE) \n  " +
                                                "DEFAULT CHARSET=utf8 COMMENT='2.0.0'").execute() ;
                connection.prepareStatement("INSERT INTO cube_accounts (id, user_id, name, value, mask) SELECT `key`, user_id, name, value, mask FROM old_accounts").execute();
                // Save data from related table
                connection.prepareStatement("CREATE  TABLE  `old_account_access` \n" +
                                                "(`id` int( 10  )  unsigned NOT  NULL  AUTO_INCREMENT ,\n" +
                                                "`userId` int( 10  )  unsigned NOT  NULL ,\n" +
                                                "`accountId` int( 10  )  unsigned NOT  NULL ,\n" +
                                                "`accessLevel` tinyint( 4  )  NOT  NULL ,\n" +
                                                "PRIMARY  KEY (  `id`  ) ,\n" +
                                                "UNIQUE  KEY  `userId` (  `userId` ,  `accountId`  ) ,\n" +
                                                "KEY  `accountId` (  `accountId`  )  ) " +
                                                "ENGINE  = InnoDB  DEFAULT CHARSET  = utf8").execute();
                connection.prepareStatement("INSERT INTO `old_account_access` SELECT * FROM `cube_account_access`").execute();
                // Drop related table and refill data
                connection.prepareStatement("DROP TABLE cube_account_access").execute();
                connection.prepareStatement("CREATE  TABLE  `cube_account_access` \n" +
                                                "(`id` int( 10  )  unsigned NOT  NULL  AUTO_INCREMENT ,\n" +
                                                "`userId` int( 10  )  unsigned NOT  NULL ,\n" +
                                                "`accountId` int( 10  )  unsigned NOT  NULL ,\n" +
                                                "`accessLevel` tinyint( 4  )  NOT  NULL ,\n" +
                                                "PRIMARY  KEY (  `id`  ) ,\n" +
                                                "UNIQUE  KEY  `userId` (  `userId` ,  `accountId`  ) ,\n" +
                                                "FOREIGN KEY f_accountId (`accountId`) REFERENCES `cube_accounts`(  `id`  ) ON DELETE CASCADE ON UPDATE CASCADE, \n" +
                                                "FOREIGN KEY f_userId (`userId`) REFERENCES `cube_user` (`key`) ON DELETE CASCADE  ON UPDATE CASCADE )\n" +
                                                "DEFAULT CHARSET  = utf8 COLLATE=utf8_unicode_ci COMMENT ='1.0.0'").execute();
                connection.prepareStatement("INSERT INTO `cube_account_access` SELECT * FROM `old_account_access`").execute();
                // drop temp_tables
                connection.prepareStatement("DROP TABLE old_account_access").execute();
                connection.prepareStatement("DROP TABLE old_accounts").execute();
            }
        }
    }
}
