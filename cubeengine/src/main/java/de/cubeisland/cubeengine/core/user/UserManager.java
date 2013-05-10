package de.cubeisland.cubeengine.core.user;

import java.util.Set;

import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.Triplet;

public interface UserManager extends Cleanable
{
    boolean login(User user, String password);
    boolean checkPassword(User user, String password);
    void setPassword(User user, String password);
    void resetPassword(User user);
    void resetAllPasswords();
    void removeUser(User user);
    User getExactUser(String name);
    User getExactUser(CommandSender sender);
    User getUser(long key);
    User getUser(String name);
    User getUser(String name, boolean create);
    Set<User> getOnlineUsers();
    Set<User> getLoadedUsers();
    void shutdown();
    User findOnlineUser(String name);
    User findUser(String name);
    void broadcastMessage(String message, Permission perm, Object... params);
    void broadcastMessage(String message, Object... args);
    void broadcastStatus(ChatFormat starColor, String message, CommandSender sender, Object... args);
    void broadcastStatus(String message, CommandSender sender, Object... args);
    Triplet<Long, String, Integer> getFailedLogin(User user);
    void kickAll(String message);
    void kickAll(String message, Object... params);
    void attachToAll(Class<? extends UserAttachment> attachmentClass, Module module);
    void detachFromAll(Class<? extends UserAttachment> attachmentClass);
    void detachAllOf(Module module);
    void addDefaultAttachment(Class<? extends UserAttachment> attachmentClass, Module module);
    void removeDefaultAttachment(Class<? extends UserAttachment> attachmentClass);
    void removeDefaultAttachments(Module module);
    void removeDefaultAttachments();
    Set<Long> getAllKeys();
    void cleanup(Module module);
}
