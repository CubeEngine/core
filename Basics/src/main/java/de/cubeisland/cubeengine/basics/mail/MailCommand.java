package de.cubeisland.cubeengine.basics.mail;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.storage.BasicUser;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.List;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.paramNotFound;

public class MailCommand extends ContainerCommand
{
    private Basics basics;
    private MailManager mailManager;

    public MailCommand(Basics basics)
    {
        super(basics, "mail", "Manages your server-mails.");
        this.basics = basics;
        this.mailManager = basics.getMailManager();
    }

    @Alias(names = "readmail")
    @Command(desc = "Reads your mails.", usage = "[player]")
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
                context.sendMessage("basics", "&eIf you wanted to look into other players mails use: &6/mail spy %s&e.\n&cOtherwise be quiet!", context.getString(0));
                return;
            }
            mailof = context.getUser(0);
            if (mailof == null)
            {
                if (!context.getString(0).equalsIgnoreCase("CONSOLE"))
                {
                    paramNotFound(context, "basics", "&cUser %s not found!", context.getString(0));
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
                context.sendMessage("basics", "&eLog into the game to check your mailbox!");
                return;
            }
        }
        BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(sender);
        if (bUser.mailbox.isEmpty())
        {
            context.sendMessage("basics", "&eYou do not have any message!");
        }

        List<Mail> mails;
        if (mailof == null) //get mails
        {
            mails = mailManager.getMails(sender);
        }
        else
        //Search for mail of that user
        {
            mails = mailManager.getMails(sender, mailof);
        }
        if (mails.isEmpty()) // Mailbox is not empty but no message from that player
        {
            context.sendMessage("basics", "&eYou do not have any mail from &2%s&e.", nameMailOf);
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Mail mail : mails)
            {
                i++;
                sb.append("\n&f").append(i).append(": ").append(mail.toString());
            }
            context.sendMessage("basics", "&aYour mails:%s", sb.toString());
        }
    }

    @Alias(names = "spymail")
    @Command(desc = "Shows the mails of other players.", usage = "<player>", max = 1)
    public void spy(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context, "basics", "&cUser %s not found!", context.getString(0));
        }
        mailManager.getMails(user);
        List<Mail> mails = mailManager.getMails(user);
        if (mails.isEmpty()) // Mailbox is not empty but no message from that player
        {
            context.sendMessage("basics", "&2%s &edo not have any mails!", user.getName());
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Mail mail : mails)
            {
                i++;
                sb.append("\n&f").append(i).append(": ").append(mail.toString());
            }
            context.sendMessage("basics", "&2%s's mails:%s", user.getName(), sb.toString());
        }
    }

    @Alias(names = "sendmail")
    @Command(desc = "Sends mails to other players.", usage = "<player> <message>", min = 2)
    public void send(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context, "basics", "&cUser %s not found!", context.getString(0));
        }
        String message = context.getStrings(1);
        this.mail(message, (User)context.getSender(), user);
        context.sendMessage("basics", "&aMail send to &2%s&a!", user.getName());
    }

    @Alias(names = "sendallmail")
    @Command(desc = "Sends mails to all players.", usage = "<message>")
    public void sendAll(CommandContext context)
    {
        List<User> users = this.basics.getUserManager().getOnlineUsers();
        final TLongSet alreadySend = new TLongHashSet();
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        final String message = context.getStrings(0);
        for (User user : users)
        {
            this.mailManager.addMail(user, sender, message);
            alreadySend.add(user.key);
        }
        final User sendingUser = sender;
        this.basics.getTaskManger().getExecutorService().submit(new Runnable()
        {
            public void run() // Async sending to all Users ever
            {
                for (User user : basics.getUserManager().getAll())
                {
                    if (!alreadySend.contains(user.key))
                    {
                        mailManager.addMail(user, sendingUser, message);
                    }
                }
            }
        });
        context.sendMessage("basics", "&aMail send to everyone!");
    }

    @Command(names = {
        "clear", "remove"
    }, desc = "Clears your mails.", usage = "[player]")
    public void clear(CommandContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendMessage("basics", "&cYou will never have mails here!");
            return;
        }
        if (!context.hasArg(0))
        {
            this.mailManager.removeMail(sender);
            context.sendMessage("basics", "&eCleared all mails!");
        }
        else
        {
            User from = context.getUser(0);
            if (from == null)
            {
                if (!context.getString(0).equalsIgnoreCase("Console"))
                {
                    paramNotFound(context, "basics", "&cUser %s not found!", context.getString(0));
                }
            }
            this.mailManager.removeMail(sender, from);
            context.sendMessage("basics", "&eCleared all mails from &2%s&e!", from.getName());
        }
    }

    private void mail(String message, User from, User... users)
    {
        for (User user : users)
        {
            mailManager.addMail(user, from, message);
            if (user.isOnline())
            {
                user.sendMessage("basics", "&eYou just got a mail from &2%s&e!", from.getName());
            }
        }
    }
}
