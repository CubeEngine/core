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
package de.cubeisland.engine.core.user;

import java.util.Set;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.Cleanable;
import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.core.util.formatter.MessageType;

public interface UserManager extends Cleanable
{
    boolean login(User user, String password);
    boolean checkPassword(User user, String password);
    void setPassword(User user, String password);
    void resetPassword(User user);
    void resetAllPasswords();

    /**
     * Removes the user permanently. Data cannot be restored later on
     *
     * @param user the User
     * @return fluent interface
     */
    void removeUser(User user);

    /**
     * Gets a user by CommandSender (creates new user if not found)
     *
     * @param name the sender
     * @return the User OR null if sender is not a Player
     */
    User getExactUser(String name);

    /**
     * Gets a User by CommandSender (creates new User if not found)
     *
     * @param sender the sender
     * @return the User OR null if sender is not a Player
     */
    User getExactUser(CommandSender sender);

    /**
     * Gets a user by his database ID
     *
     * @param id the ID to get the user by
     * @return the user or null if not found
     */
    User getUser(long id);

    /**
     * Gets a user by his name
     *
     * @param name the name to get the user by
     * @return the user or null if not found
     */
    User getUser(String name);

    /**
     * Gets a user by his name
     *
     * @param name the name to get the user by
     * @param create whether to create the user if not found
     * @return the user or null if not found and create is false
     */
    User getUser(String name, boolean create);

    /**
     * Queries the database directly if the user is not loaded to get its name.
     * <p>Only use with valid key!
     *
     * @param key the users key
     * @return
     */
    String getUserName(long key);

    /**
     * Returns all the users that are currently online
     *
     * @return a unmodifiable List of players
     */
    Set<User> getOnlineUsers();
    Set<User> getLoadedUsers();
    void shutdown();

    /**
     * Finds an online User
     *
     * @param name the name
     * @return a online User
     */
    User findOnlineUser(String name);

    /**
     * Finds an User (can create a new User if a found player is online but not
     * yet added)
     *
     * @param name the name
     * @return the found User or null
     */
    User findUser(String name);

    /**
     * Finds an User (can also search for matches in the database)
     *
     * @param name the name
     * @param database matches in the database too if true
     * @return the found User or null
     */
    User findUser(String name, boolean database);
    void broadcastMessageWithPerm(MessageType messageType, String message, Permission perm, Object... params);
    void broadcastMessage(MessageType messageType, String message, Object... args);
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
    Set<Long> getAllIds();
    void cleanup(Module module);
}
