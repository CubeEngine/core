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
@Table(name = "account_access", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "accountId"}))
public class BankAccessModel
{
    @javax.persistence.Version
    static final Version version = new Version(1);

    @Id
    @Attribute(type = AttrType.INT, unsigned = true)
    private long id;
    @Column(name = "userId", nullable = false)
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "userId")
    @Attribute(type = AttrType.INT, unsigned = true)
    private UserEntity userEntity;
    @Column(name = "accountId", nullable = false)
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "accountId")
    @Attribute(type = AttrType.INT, unsigned = true)
    private AccountModel accountModel;
    @Column(nullable = false)
    @Attribute(type = AttrType.TINYINT)
    private byte accessLevel;

    public static final byte OWNER = 1;
    public static final byte MEMBER = 2;
    public static final byte INVITED = 4;

    public BankAccessModel(long id, UserEntity userEntity, AccountModel accountModel, byte accessLevel, String name)
    {
        this.id = id;
        this.userEntity = userEntity;
        this.accountModel = accountModel;
        this.accessLevel = accessLevel;
    }

    public BankAccessModel(AccountModel accountModel, User user, byte type)
    {
        this.userEntity = user.getEntity();
        this.accountModel = accountModel;
        this.accessLevel = type;
    }

    public BankAccessModel()
    {}

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

    public AccountModel getAccountModel()
    {
        return accountModel;
    }

    public void setAccountModel(AccountModel accountModel)
    {
        this.accountModel = accountModel;
    }

    public byte getAccessLevel()
    {
        return accessLevel;
    }

    public void setAccessLevel(byte accessLevel)
    {
        this.accessLevel = accessLevel;
    }
}
