package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.BasicUser;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;

public class MailCommand extends ContainerCommand
{
    private Basics basics;

    //TODO when user joins check with custom query if there is any mail.
    //If there is load BasicUser + Show amount of unread mails.
    public MailCommand(Basics basics)
    {
        super(basics, "mail", "Manages your server-mails.");
        this.basics = basics;
    }

    @Alias(names = "readmail")
    @Command(
        desc = "Reads your mails.",
    usage = "[player]")
    public void read(CommandContext context)
    {
        User sender;
        User mailof = null;
        String nameMailOf = null;
        if (context.hasIndexed(0))
        {
            sender = context.getSenderAsUser("basics", "If you wanted to look into other players mails use: /mail spy %s."
                                                     + "\nOtherwise be quiet!",context.getString(0));
            //TODO mail spy <player>
            mailof = context.getUser(0);
            if (mailof == null)
            {
                if (!context.getString(0).equalsIgnoreCase("CONSOLE"))
                {
                    illegalParameter(context, "basics", "User not found!");
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
            sender = context.getSenderAsUser("basics", "Log into the game to check your mailbox!");
        }
        BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(sender);
        if (bUser.mailbox.isEmpty())
        {
            context.sendMessage("basics", "You do not have any message!");
        }
        if (mailof == null) // Just read next mail
        {
            bUser.readMail();
        }
        else //Search for mail of that user
        {
            String foundMessage = null;
            for (String message : bUser.mailbox)
            {
                if (message.startsWith(nameMailOf))
                {
                    foundMessage = message;
                    break;
                }
            }
            if (foundMessage == null)
            {
                context.sendMessage("basics", "You do not have any mail from %s", nameMailOf);
            }
            else
            {
                context.sendMessage("basics", "Mail read:\n%s", foundMessage);
            }
        }
    }

    @Alias(names = "sendmail")
    @Command(
    desc = "Sends mails to other players.",
    usage = "<player> <message>",
    min = 2)
    public void send(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        String message = context.getStrings(1);
        this.mail(message, context.getSenderAsUser(), user);
    }

    @Alias(names = "sendmail")
    @Command(
        desc = "Sends mails to all players.",
    usage = "<message>")
    public void sendAll(CommandContext context)
    {
        // Sending the mails sync to loadedUsers
        // then async for getting all Users from database and sending to them
    }

    @Command(names =
    {
        "remove", "delete"
    },
    desc = "Deletes a mail.")
    //TODO alias for deleting all 
    //mail remove -a
    //== mail clear
    public void remove(CommandContext context)
    {
    }

    private void mail(String message, User from, User... users)
    {
        for (User user : users)
        {
            BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(user);
            bUser.addMail(from, message); //If from is null pretend it was the console
            this.basics.getBasicUserManager().update(bUser); // This is async
            if (user.isOnline())
            {
                user.sendMessage("basics", "You just got a mail from %s", from.getName());
            }
        }
    }
}
