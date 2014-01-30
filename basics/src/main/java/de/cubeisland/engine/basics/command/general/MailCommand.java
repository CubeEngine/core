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
package de.cubeisland.engine.basics.command.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.basics.BasicsUser;
import de.cubeisland.engine.basics.storage.Mail;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.basics.storage.TableMail.TABLE_MAIL;
import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;

public class MailCommand extends ContainerCommand
{
    private final Basics module;

    public MailCommand(Basics module)
    {
        super(module, "mail", "Manages your server-mails.");
        this.module = module;
    }

    @Alias(names = "readmail")
    @Command(desc = "Reads your mails.", usage = "[player]", max = 1)
    public void read(CommandContext context)
    {
        User sender;
        User mailof = null;
        String nameMailOf = null;
        if (context.hasArg(0))
        {
            sender = null;
            if (context.getSender() instanceof User)
            {
                sender = (User)context.getSender();
            }
            if (sender == null)
            {
                context.sendTranslated("&eIf you wanted to look into other players mails use: &6/mail spy %s&e.\n&cOtherwise be quiet!", context.getString(0));
                return;
            }
            mailof = context.getUser(0);
            if (mailof == null)
            {
                if (!context.getString(0).equalsIgnoreCase("CONSOLE"))
                {
                    context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
                    return;
                }
                nameMailOf = "CONSOLE";
            }
            else
            {
                nameMailOf = mailof.getName();
            }
        }
        else
        {
            sender = null;
            if (context.getSender() instanceof User)
            {
                sender = (User)context.getSender();
            }
            if (sender == null)
            {
                context.sendTranslated("&eLog into the game to check your mailbox!");
                return;
            }
        }
        BasicsUser bUser = sender.attachOrGet(BasicsAttachment.class, this.module).getBasicsUser();
        if (bUser.countMail() == 0)
        {
            context.sendTranslated("&eYou do not have any mail!");
            return;
        }
        List<Mail> mails;
        if (mailof == null) //get mails
        {
            mails = bUser.getMails();
        }
        else //Search for mail of that user
        {
            mails = bUser.getMailsFrom(mailof);
        }
        if (mails.isEmpty()) // Mailbox is not empty but no message from that player
        {
            context.sendTranslated("&eYou do not have any mail from &2%s&e.", nameMailOf);
            return;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Mail mail : mails)
        {
            i++;
            sb.append("\n&f").append(i).append(": ").append(mail.readMail());
        }
        context.sendTranslated("&aYour mails:%s", ChatFormat.parseFormats(sb.toString()));
    }

    @Alias(names = "spymail")
    @Command(desc = "Shows the mails of other players.", usage = "<player>", min = 1, max = 1)
    public void spy(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        List<Mail> mails = user.attachOrGet(BasicsAttachment.class, this.module).getBasicsUser().getMails();
        if (mails.isEmpty()) // Mailbox is not empty but no message from that player
        {
            context.sendTranslated("&2%s &edoes not have any mails!", user.getName());
            return;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Mail mail : mails)
        {
            i++;
            sb.append("\n&f").append(i).append(": ").append(mail.getMessage());
        }
        context.sendTranslated("&2%s's mails:%s", user.getName(), ChatFormat.parseFormats(sb.toString()));
    }

    @Alias(names = "sendmail")
    @Command(desc = "Sends mails to other players.", usage = "<player> <message>", min = 2, max = NO_MAX)
    public void send(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        String message = context.getStrings(1);
        this.mail(message, context.getSender(), user);
        context.sendTranslated("&aMail send to &2%s&a!", user.getName());
    }

    @Alias(names = "sendallmail")
    @Command(desc = "Sends mails to all players.", usage = "<message>", min = 1 , max = NO_MAX)
    public void sendAll(CommandContext context)
    {
        Set<User> users = this.module.getCore().getUserManager().getOnlineUsers();
        final TLongSet alreadySend = new TLongHashSet();
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        final String message = context.getStrings(0);
        for (User user : users)
        {
            user.attachOrGet(BasicsAttachment.class, module).getBasicsUser().addMail(sender, message);
            alreadySend.add(user.getId());
        }
        final UInteger senderId = sender == null ? null : sender.getEntity().getKey();
        this.module.getCore().getTaskManager().runAsynchronousTaskDelayed(this.getModule(),new Runnable()
        {
            public void run() // Async sending to all Users ever
            {
                DSLContext dsl = module.getCore().getDB().getDSL();
                Collection<Query> queries = new ArrayList<>();
                for (Long userId : module.getCore().getUserManager().getAllIds())
                {
                    if (!alreadySend.contains(userId))
                    {
                        queries.add(dsl.insertInto(TABLE_MAIL, TABLE_MAIL.MESSAGE, TABLE_MAIL.USERID, TABLE_MAIL.SENDERID).values(message, UInteger.valueOf(userId), senderId));
                    }
                }
                dsl.batch(queries).execute();
            }
        },0);
        context.sendTranslated("&aMail send to everyone!");
    }

    @Command(desc = "Removes a single mail", usage = "<mailId>", min = 1, max = 1)
    public void remove(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User user = (User)context.getSender();
            Integer mailId = context.getArg(0, Integer.class, null);
            if (mailId == null)
            {
                context.sendTranslated("&6%s&c is not a number!", context.getString(0));
                return;
            }
            BasicsUser bUser = user.attachOrGet(BasicsAttachment.class, this.module).getBasicsUser();
            if (bUser.countMail() == 0)
            {
                context.sendTranslated("&eYou do not have any mail!");
                return;
            }
            try
            {
                Mail mail = bUser.getMails().get(mailId);
                module.getCore().getDB().getDSL().delete(TABLE_MAIL).where(TABLE_MAIL.KEY.eq(mail.getKey())).execute();
                context.sendTranslated("&aDeleted Mail #%d", mailId);
            }
            catch (IndexOutOfBoundsException e)
            {
                context.sendTranslated("&cInvalid Mail Id!");
            }
        }
        else
        {
            context.sendTranslated("&cThe console has no mails!");
        }
    }

    @Command(names = {"clear", "remove"},
            desc = "Clears your mails.", usage = "[player]", min = 0, max = 1)
    public void clear(CommandContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendTranslated("&cYou will never have mails here!");
            return;
        }
        if (!context.hasArg(0))
        {
            sender.attachOrGet(BasicsAttachment.class, this.module).getBasicsUser().clearMail();
            context.sendTranslated("&eCleared all mails!");
            return;
        }
        User from = context.getUser(0);
        if (from == null && !context.getString(0).equalsIgnoreCase("Console"))
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        sender.attachOrGet(BasicsAttachment.class, this.module).getBasicsUser().clearMailFrom(from);
        context.sendTranslated("&eCleared all mails from &2%s&e!", from == null ? "console" : from.getName());
    }

    private void mail(String message, CommandSender from, User... users)
    {
        for (User user : users)
        {
            user.attachOrGet(BasicsAttachment.class, this.module).getBasicsUser().addMail(from, message);
            if (user.isOnline())
            {
                user.sendTranslated("&eYou just got a mail from &2%s&e!", from.getName());
            }
        }
    }
}
