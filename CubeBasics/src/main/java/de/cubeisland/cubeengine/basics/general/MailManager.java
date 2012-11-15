package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.BasicUser;
import de.cubeisland.cubeengine.basics.BasicUserManager;
import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;

public class MailManager extends BasicStorage<Mail>
{
    private final BasicUserManager bUserManager;
    private static final int       REVISION = 1;

    public MailManager(Database database, BasicUserManager bUserManager)
    {
        super(database, Mail.class, REVISION);
        this.bUserManager = bUserManager;
        this.initialize();
    }

    @Override
    protected void initialize()
    {
        try
        {
            super.initialize();
            QueryBuilder builder = this.database.getQueryBuilder();
            this.database.prepareAndStoreStatement(modelClass, "getallByUser", builder
                .select().wildcard()
                .from(this.table)
                .where().field("userId").is(EQUAL).value()
                .end()
                .end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the mail-manager!", e);
        }
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
            if (mail.senderId == sender.key)
            {
                mails.add(mail);
            }
        }
        return mails;
    }

    public void addMail(User user, User from, String message)
    {
        BasicUser bUser = this.getBasicUserWithMails(user);
        Mail mail;
        if (from == null)
        {
            mail = new Mail(user.key, 0, message);
        }
        else
        {
            mail = new Mail(user.key, from.key, message);
        }
        bUser.mailbox.add(mail);
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
        bUser.mailbox = new ArrayList();
    }

    public void removeMail(User user, User sendBy)
    {//TODO if null then key = 0 (send by CONSOLE)
        BasicUser bUser = this.getBasicUserWithMails(user);
        for (Mail mail : bUser.mailbox)
        {
            if (mail.senderId == sendBy.key)
            {
                this.delete(mail);
            }
        }
        bUser.mailbox = new ArrayList(); // will have to read again from database
    }

    public List<Mail> getAll(User user)
    {
        List<Mail> loadedModels = new ArrayList<Mail>();
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getallByUser", user.key);

            while (resulsSet.next())
            {
                ArrayList<Object> values = new ArrayList<Object>();
                values.add(resulsSet.getObject(this.key));
                for (String name : this.attributes)
                {
                    values.add(resulsSet.getObject(name));
                }
                Mail loadedModel = this.modelConstructor.newInstance(values);
                loadedModels.add(loadedModel);
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while getting Model from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
        return loadedModels;
    }
}
