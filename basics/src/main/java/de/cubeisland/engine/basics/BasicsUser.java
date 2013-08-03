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
package de.cubeisland.engine.basics;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.EbeanServer;
import de.cubeisland.engine.basics.storage.BasicsUserEntity;
import de.cubeisland.engine.basics.storage.Mail;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.user.User;

public class BasicsUser
{
    private EbeanServer ebeanServer;

    public BasicsUserEntity getbUEntity()
    {
        return bUEntity;
    }

    private BasicsUserEntity bUEntity;
    private List<Mail> mailbox = new ArrayList<>();

    public BasicsUser(EbeanServer ebeanServer, User user)
    {
        this.ebeanServer = ebeanServer;
        this.bUEntity = ebeanServer.find(BasicsUserEntity.class).where().eq("userid", user.getEntity().getId()).findUnique();
        if (bUEntity == null)
        {
            this.bUEntity = new BasicsUserEntity(user);
            ebeanServer.save(this.bUEntity);
        }
    }

    public void loadMails()
    {
        this.mailbox = this.ebeanServer.find(Mail.class).where().eq("senderId", bUEntity.getEntity().getId()).findList();
    }

    public List<Mail> getMails()
    {
        if (this.mailbox.isEmpty())
        {
            this.loadMails();
        }
        return this.mailbox;
    }

    public List<Mail> getMailsFrom(User sender)
    {
        List<Mail> mails = new ArrayList<>();
        for (Mail mail : this.getMails())
        {
            if (mail.getSenderEntity().getId() == sender.getId())
            {
                mails.add(mail);
            }
        }
        return mails;
    }

    public void addMail(CommandSender from, String message)
    {
        this.getMails(); // load if not loaded
        Mail mail;
        if (from instanceof User)
        {
            mail = new Mail(this.bUEntity.getEntity(), ((User)from).getEntity(), message);
        }
        else
        {
            mail = new Mail(this.bUEntity.getEntity(), null, message);
        }
        this.mailbox.add(mail);
        this.ebeanServer.save(mail);
    }

    public int countMail()
    {
        return this.getMails().size();
    }

    public void clearMail()
    {
        this.ebeanServer.delete(this.getMails());
        this.mailbox = new ArrayList<>();
    }

    public void clearMailFrom(User sender)
    {
        List<Mail> mailsFrom = this.getMailsFrom(sender);
        this.mailbox.removeAll(mailsFrom);
        this.ebeanServer.delete(mailsFrom);
    }
}
