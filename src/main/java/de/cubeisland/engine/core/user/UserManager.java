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
import java.util.UUID;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.Cleanable;
import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.core.util.formatter.MessageType;
import org.jooq.types.UInteger;

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
     */
    void removeUser(User user);

    /**
     * Gets a user by CommandSender (creates new user if not found)
     *
     * @param name the sender
     *
     * @return the User OR null if sender is not a Player
     */
    User getExactUser(String name);

    /**
     * Gets a user by its UUID (creates a new user if not found)
     *
     * @param uuid the uuid
     *
     * @return the user
     */
    User getExactUser(UUID uuid);

    /**
     * Gets a user by his database ID
     *
     * @param id the ID to get the user by
     *
     * @return the user or null if not found
     */
    User getUser(UInteger id);

    /**
     * Gets a user by his name
     *
     * @param name the name to get the user by
     *
     * @return the user or null if not found
     */
    User findExactUser(String name);

    /**
     * Queries the database directly if the user is not loaded to get its name.
     * <p>Only use with valid key!
     *
     * @param key the users key
     */
    String getUserName(UInteger key);

    /**
     * Returns all the users that are currently online
     *
     * @return a unmodifiable List of players
     */
    Set<User> getOnlineUsers();

    Set<User> getLoadedUsers();

    void shutdown();

    /**
     * Finds an User (can create a new User if a found player is online but not
     * yet added)
     *
     * @param name the name
     *
     * @return the found User or null
     */
    User findUser(String name);

    /**
     * Finds an User (can also search for matches in the database)
     *
     * @param name     the name
     * @param database matches in the database too if true
     *
     * @return the found User or null
     */
    User findUser(String name, boolean database);

    /**
     * Broadcasts a translated message
     * @param messageType the messageType
     * @param message the message to broadcast
     * @param perm the permission to check
     * @param params the parameters
     */
    void broadcastTranslatedWithPerm(MessageType messageType, String message, Permission perm, Object... params);

    /**
     * Broadcasts a message (not translated)
     *
     * @param message the message to broadcast
     * @param perm the permission to check
     * @param params the parameters
     */
    void broadcastMessageWithPerm(MessageType messageType, String message, Permission perm, Object... params);

    /**
     * Broadcasts a translated message
     *
     * @param messageType the messageType
     * @param message the message to broadcast
     * @param params the parameters
     */
    void broadcastTranslated(MessageType messageType, String message, Object... params);

    /**
     * Broadcasts a message (not translated)
     *
     * @param messageType the messageType
     * @param message the message to broadcast
     * @param params the parameters
     */
    void broadcastMessage(MessageType messageType, String message, Object... params);

    /**
     * Broadcasts a status message (not translated)
     *
     * @param starColor the color of the prepended star
     * @param message the message
     * @param sender the sender
     * @param params the parameters
     */
    void broadcastStatus(ChatFormat starColor, String message, CommandSender sender, Object... params);

    /**
     * Broadcasts a translated status message
     *
     * @param starColor the color of the prepended star
     * @param message the message
     * @param sender the sender
     * @param params the parameters
     */
    void broadcastTranslatedStatus(ChatFormat starColor, String message, CommandSender sender, Object... params);

    /**
     * Broadcasts a status message (not translated)
     *
     * @param message the message
     * @param sender the sender
     * @param params the parameters
     */
    void broadcastStatus(String message, CommandSender sender, Object... params);

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
