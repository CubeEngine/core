package de.cubeisland.cubeengine.shout.task;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import java.util.concurrent.Callable;

/**
 * A class to acutually send a message to a user
 */
public class AnnouncementSender implements Callable
{
    String user;
    String message;

    public AnnouncementSender(String user, String message)
    {
        this.user = user;
        this.message = message;
    }

    public Object call() throws Exception
    {
        User u = CubeEngine.getCore().getUserManager().getUser(user, false);
        u.sendMessage(message);
        return null;
    }
}
