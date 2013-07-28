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
package de.cubeisland.engine.basics.command.mail;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.cubeisland.engine.basics.storage.BasicUser;
import de.cubeisland.engine.basics.storage.BasicUserManager;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.storage.SingleKeyStorage;
import de.cubeisland.engine.core.storage.StorageException;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.user.User;

public class MailManager extends SingleKeyStorage<Long, Mail>
{
    private final BasicUserManager bUserManager;
    private static final int REVISION = 1;

    public MailManager(Database database, BasicUserManager bUserManager)
    {
        super(database, Mail.class, REVISION);
        this.bUserManager = bUserManager;
        this.initialize();
    }

    @Override
    public void initialize()
    {
        /*
        try
        {
            //TODO DATABASE
            super.initialize();
            QueryBuilder builder = this.database.getQueryBuilder();
            this.database.storeStatement(modelClass, "getallByUser", builder.select().wildcard().from(this.tableName).where().field("userId").is(EQUAL).value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the mail-manager!", e);
        }*/
    }

    public List<Mail> getMails(User user)
    {
        BasicUser bUser = this.getBasicUserWithMails(user);
        return bUser.mailbox;
    }

    public List<Mail> getMails(User user, User sender)
    {
        List<Mail> mails = new ArrayList<Mail>();
        for (Mail mail : this.getMails(user))
        {
            if (mail.senderId == sender.getId())
            {
                mails.add(mail);
            }
        }
        return mails;
    }

    public void addMail(User user, CommandSender from, String message)
    {
        BasicUser bUser = this.getBasicUserWithMails(user);
        Mail mail;
        if (from instanceof User)
        {
            mail = new Mail(user.getId(), ((User)from).getId(), message);
        }
        else
        {
            mail = new Mail(user.getId(), null, message);
        }
        bUser.mailbox.add(mail);
        this.store(mail);
    }

    public void addFastMail(Long userKey, CommandSender from, String message)
    {
        Mail mail;
        if (from instanceof User)
        {
            mail = new Mail(userKey, ((User)from).getId(), message);
        }
        else
        {
            mail = new Mail(userKey, null, message);
        }
        this.store(mail);
    }

    private BasicUser getBasicUserWithMails(User user)
    {
        BasicUser bUser = bUserManager.getBasicUser(user);
        if (bUser.mailbox.isEmpty())
        {
            bUser.mailbox = getAll(user);
        }
        return bUser;
    }

    public int countMail(User user)
    {
        return this.getMails(user).size();
    }

    public void removeMail(User user)
    {
        BasicUser bUser = this.getBasicUserWithMails(user);
        for (Mail mail : bUser.mailbox)
        {
            this.delete(mail);
        }
        bUser.mailbox = new ArrayList<Mail>();
    }

    public void removeMail(User user, User sendBy)
    {
        BasicUser bUser = this.getBasicUserWithMails(user);
        for (Mail mail : bUser.mailbox)
        {
            if (mail.senderId == (sendBy == null ? 0 : sendBy.getId()))
            {
                this.delete(mail);
            }
        }
        bUser.mailbox = new ArrayList<Mail>(); // will have to read again from database
    }

    public List<Mail> getAll(User user)
    {
        List<Mail> loadedModels = new ArrayList<Mail>();
        try
        {
            ResultSet result = this.database.preparedQuery(modelClass, "getallByUser", user.getId());
            while (result.next())
            {
                Mail loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, result.getObject(this.fieldNames.get(field)));
                }
                loadedModels.add(loadedModel);
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while getting Model from Database", ex, this.database.getStoredStatement(modelClass, "getallByUser"));
        }
        catch (Exception ex)
        {
            throw new StorageException("Error while creating fresh Model from Database", ex);
        }
        return loadedModels;
    }
}
