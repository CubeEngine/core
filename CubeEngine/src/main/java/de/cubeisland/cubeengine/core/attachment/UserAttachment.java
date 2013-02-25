package de.cubeisland.cubeengine.core.attachment;

import de.cubeisland.cubeengine.core.user.User;

public interface UserAttachment extends Attachment<User>
{
    void onJoin(String joinMessage);
    void onQuit(String quitMessage);
    void onKick(String kickMessage);
    void onChat(String message);
    void onCommand(String commandline);
}
