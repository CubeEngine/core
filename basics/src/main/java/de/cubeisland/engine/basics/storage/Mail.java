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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Version;

@Entity
@Table(name = "mail")
public class Mail
{
    @javax.persistence.Version
    static final Version version = new Version(1);

    @Id
    @Attribute(type = AttrType.INT, unsigned = true)
    private Long key;
    @Column(length = 100, nullable = false)
    @Attribute(type = AttrType.VARCHAR)
    private String message;

    @Column(name = "userId", nullable = false)
    @JoinColumn(name = "userid") // ebean needs this
    @ManyToOne(optional = false, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @Attribute(type = AttrType.INT, unsigned = true)
    private UserEntity userEntity;
    @Column(name = "senderId")
    @JoinColumn(name = "senderId") // ebean needs this
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @Attribute(type = AttrType.INT, unsigned = true)
    private UserEntity senderEntity;

    public Mail(UserEntity userId, UserEntity senderId, String message)
    {
        this.message = message;
        this.userEntity = userId;
        this.senderEntity = senderId;
    }

    public Mail() {}

    public Long getKey()
    {
        return key;
    }

    public void setKey(Long key)
    {
        this.key = key;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public UserEntity getUserEntity()
    {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity)
    {
        this.userEntity = userEntity;
    }

    public UserEntity getSenderEntity()
    {
        return senderEntity;
    }

    public void setSenderEntity(UserEntity senderEntity)
    {
        this.senderEntity = senderEntity;
    }

    public String readMail()
    {
        if (this.getSenderEntity() == null || this.getSenderEntity().getKey().longValue() == 0)
        {
            return "&cCONSOLE&f: " + this.getMessage();
        }
        User user = CubeEngine.getUserManager().getUser(this.getSenderEntity().getKey().longValue());
        return "&2" + user.getName() + "&f: " + this.getMessage();
    }
}
