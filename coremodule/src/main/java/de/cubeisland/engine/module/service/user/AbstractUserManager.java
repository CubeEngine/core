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
package de.cubeisland.engine.module.service.user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.service.command.CommandManager;
import de.cubeisland.engine.module.service.command.CommandSender;
import de.cubeisland.engine.module.service.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.module.core.filesystem.FileManager;
import de.cubeisland.engine.module.core.filesystem.FileUtil;
import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.service.permission.Permission;
import de.cubeisland.engine.module.core.sponge.EventManager;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.service.database.Database;
import de.cubeisland.engine.module.core.util.StringUtils;
import de.cubeisland.engine.module.core.util.Triplet;
import de.cubeisland.engine.module.core.util.formatter.MessageType;
import de.cubeisland.engine.module.core.util.matcher.StringMatcher;
import org.jooq.Record1;
import org.jooq.types.UInteger;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.BaseFormatting;
import org.spongepowered.api.text.format.TextColors;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.NONE;

/**
 * This Manager provides methods to access the Users and saving/loading from
 * database.
 */
public abstract class AbstractUserManager implements UserManager
{
    private final CoreModule core;
    protected List<User> onlineUsers;
    protected ConcurrentHashMap<UUID, User> cachedUserByUUID = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<UInteger, User> cachedUserByDbId = new ConcurrentHashMap<>();
    protected Set<DefaultAttachment> defaultAttachments;
    protected String salt;
    protected final MessageDigest messageDigest;

    protected final Database database;
    private CommandManager cm;
    private FileManager fm;
    private EventManager em;
    private I18n i18n;

    public AbstractUserManager(final CoreModule core, Database database, CommandManager cm, I18n i18n, FileManager fm, EventManager em)
    {
        this.database = database;
        this.core = core;
        this.cm = cm;
        this.fm = fm;
        this.em = em;
        this.i18n = i18n;

        this.onlineUsers = new CopyOnWriteArrayList<>();

        this.defaultAttachments = new HashSet<>();

        this.loadSalt();

        try
        {
            messageDigest = MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-512 hash algorithm not available!");
        }
    }

    public void onEnable()
    {
        database.registerTable(TableUser.class);
    }

    @Override
    public boolean login(User user, String password)
    {
        if (!user.isLoggedIn())
        {
            user.loggedInState = this.checkPassword(user, password);
        }
        em.fireEvent(new UserAuthorizedEvent(this.core, user));
        return user.isLoggedIn();
    }

    @Override
    public boolean checkPassword(User user, String password)
    {
        synchronized (this.messageDigest)
        {
            messageDigest.reset();
            password += this.salt;
            password += user.getEntity().getValue(TableUser.TABLE_USER.FIRSTSEEN).toString();
            return Arrays.equals(user.getEntity().getValue(TableUser.TABLE_USER.PASSWD), messageDigest.digest(password.getBytes()));
        }
    }

    @Override
    public void setPassword(User user, String password)
    {
        synchronized (this.messageDigest)
        {
            this.messageDigest.reset();
            password += this.salt;
            password += user.getEntity().getValue(TableUser.TABLE_USER.FIRSTSEEN).toString();
            user.getEntity().setValue(TableUser.TABLE_USER.PASSWD, this.messageDigest.digest(password.getBytes()));
            user.getEntity().updateAsync();
        }
    }

    @Override
    public void resetPassword(User user)
    {
        user.getEntity().setValue(TableUser.TABLE_USER.PASSWD, null);
        user.getEntity().updateAsync();
    }

    @Override
    public void resetAllPasswords()
    {
        this.database.getDSL().update(TableUser.TABLE_USER).set(TableUser.TABLE_USER.PASSWD, (byte[])null).execute();
        for (User user : this.getLoadedUsers())
        {
            user.getEntity().refresh();
        }
    }

    @Override
    public void removeUser(final User user)
    {
        user.getEntity().deleteAsync();
        this.removeCachedUser(user);
    }

    @Override
    public User getExactUser(String name)
    {
        return this.getUser(name, true);
    }

    @Override
    public User getExactUser(UUID uuid)
    {
        User user = this.cachedUserByUUID.get(uuid);
        if (user == null)
        {
            user = this.loadUserFromDatabase(uuid);
            if (user == null)
            {
                // TODO user = new User(core, Bukkit.getOfflinePlayer(uuid));
                user.getEntity().insertAsync();
            }
            this.cacheUser(user);
        }
        return user;
    }

    protected User loadUserFromDatabase(UUID uuid)
    {
        UserEntity entity = this.database.getDSL().selectFrom(TableUser.TABLE_USER).where(TableUser.TABLE_USER.LEAST.eq(uuid.getLeastSignificantBits()).and(
            TableUser.TABLE_USER.MOST.eq(uuid.getMostSignificantBits()))).fetchOne();
        return entity == null ? null : new User(core, entity);
    }

    @Override
    public synchronized User getUser(UInteger id)
    {
        User user = this.cachedUserByDbId.get(id);
        if (user != null)
        {
            return user;
        }
        UserEntity entity = this.database.getDSL().selectFrom(TableUser.TABLE_USER).where(TableUser.TABLE_USER.KEY.eq(id)).fetchOne();
        if (entity == null)
        {
            return null;
        }
        user = new User(core, entity);
        this.cacheUser(user);
        return user;
    }

    @Override
    public User findExactUser(String name)
    {
        return this.getUser(name, false);
    }

    /**
     * Gets a user by his name
     *
     * @param name   the name to get the user by
     * @param create whether to create the user if not found
     *
     * @return the user or null if not found and create is false
     */
    protected abstract User getUser(String name, boolean create);

    @Override
    public String getUserName(UInteger key)
    {
        Record1<String> record1 = this.database.getDSL().select(TableUser.TABLE_USER.LASTNAME).from(TableUser.TABLE_USER)
                                               .where(TableUser.TABLE_USER.KEY.eq(key)).fetchOne();
        return record1 == null ? null : record1.value1();
    }

    protected synchronized void attachDefaults(User user)
    {
        for (DefaultAttachment defaultAttachment : this.defaultAttachments)
        {
            defaultAttachment.attachTo(user);
        }
    }

    protected synchronized void cacheUser(User user)
    {
        updateLastName(user);
        this.cachedUserByUUID.put(user.getUniqueId(), user);
        this.cachedUserByDbId.put(user.getEntity().getKey(), user);
        this.core.getLog().debug("User {} cached!", user.getName());
        this.attachDefaults(user);
    }

    protected void updateLastName(User user)
    {
        if (!user.getName().equalsIgnoreCase(user.getEntity().getValue(TableUser.TABLE_USER.LASTNAME)))
        {
            user.getEntity().setValue(TableUser.TABLE_USER.LASTNAME, user.getName());
            user.getEntity().updateAsync();
        }
    }

    protected synchronized void removeCachedUser(User user)
    {
        this.cachedUserByUUID.remove(user.getUniqueId());
        this.cachedUserByDbId.remove(user.getEntity().getKey());
        this.core.getLog().debug("Removed cached user {}!", user.getName());
        user.detachAll();
    }

    @Override
    public synchronized Set<User> getOnlineUsers()
    {
        return new HashSet<>(this.onlineUsers); // TODO this is not working as it should
    }

    @Override
    public synchronized Set<User> getLoadedUsers()
    {
        return new HashSet<>(this.cachedUserByUUID.values());
    }

    @Override
    public void broadcastTranslatedWithPerm(MessageType messageType, String message, Permission perm, Object... params)
    {
        if (message.isEmpty())
        {
            return;
        }

        for (User user : this.onlineUsers)
        {
            if (perm == null || perm.isAuthorized(user))
            {
                user.sendTranslated(messageType, message, params);
            }
        }
        cm.getConsoleSender().sendTranslated(messageType, message, params);
    }

    @Override
    public void broadcastMessageWithPerm(MessageType type, String message, Permission perm, Object... params)
    {
        if (message.isEmpty())
        {
            return;
        }
        for (User user : this.onlineUsers)
        {
            if (perm == null || perm.isAuthorized(user))
            {
                user.sendMessage(NONE, message, params);
            }
        }
        ConsoleCommandSender cSender = cm.getConsoleSender();
        cSender.sendMessage(i18n.composeMessage(cSender.getLocale(), type, message, params));
    }

    @Override
    public void broadcastTranslated(MessageType messageType, String message, Object... params)
    {
        this.broadcastTranslatedWithPerm(messageType, message, null, params);
    }

    @Override
    public void broadcastMessage(MessageType messageType, String message, Object... params)
    {
        this.broadcastMessageWithPerm(messageType, message, null, params);
    }

    @Override
    public void broadcastStatus(BaseFormatting starColor, String message, CommandSender sender, Object... params)
    {
        for (User user : this.onlineUsers)
        {
            user.sendMessage(starColor, "* {user} {input#message:color=WHITE}", sender.getDisplayName(), message);
        }
    }

    @Override
    public void broadcastTranslatedStatus(BaseFormatting starColor, String message, CommandSender sender, Object... params)
    {
        for (User user : this.onlineUsers)
        {
            user.sendMessage(starColor, "* {user} {input#message:color=WHITE}", sender.getDisplayName(),
                             user.getTranslation(NONE, message));
        }
    }

    @Override
    public void broadcastStatus(String message, CommandSender sender, Object... params)
    {
        this.broadcastStatus(TextColors.WHITE, message, sender, params);
    }

    private void loadSalt()
    {
        Path file = fm.getDataPath().resolve(".salt");
        try (BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset()))
        {
            this.salt = reader.readLine();
        }
        catch (NoSuchFileException e)
        {
            try
            {
                this.salt = StringUtils.randomString(new SecureRandom(), 32);
                try (BufferedWriter writer = Files.newBufferedWriter(file, Charset.defaultCharset()))
                {
                    writer.write(this.salt);
                }
            }
            catch (Exception inner)
            {
                throw new IllegalStateException("Could not store the static salt in '" + file + "'!", inner);
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not store the static salt in '" + file + "'!", e);
        }
        FileUtil.hideFile(file);
        FileUtil.setReadOnly(file);
    }

    private final Map<Long, Triplet<Long, String, Integer>> failedLogins = new HashMap<>();

    @Override
    public Triplet<Long, String, Integer> getFailedLogin(User user)
    {
        return this.failedLogins.get(user.getId());
    }

    protected void addFailedLogin(User user)
    {
        Triplet<Long, String, Integer> loginFail = this.getFailedLogin(user);
        if (loginFail == null)
        {
            loginFail = new Triplet<>(System.currentTimeMillis(), user.getAddress().getAddress().getHostAddress(), 1);
            this.failedLogins.put(user.getId(), loginFail);
        }
        else
        {
            loginFail.setFirst(System.currentTimeMillis());
            loginFail.setSecond(user.getAddress().getAddress().getHostAddress());
            loginFail.setThird(loginFail.getThird() + 1);
        }
    }

    protected void removeFailedLogins(User user)
    {
        this.failedLogins.remove(user.getId());
    }

    @Override
    public synchronized void kickAll(String message)
    {
        for (User user : this.cachedUserByUUID.values())
        {
            user.kick(Texts.of(message));
        }
    }

    @Override
    public synchronized void kickAll(String message, Object... params)
    {
        for (User user : this.cachedUserByUUID.values())
        {
            user.kick(Texts.of(user.getTranslation(NONE, message).get(user.getLocale())));
        }
    }

    @Override
    public void attachToAll(Class<? extends UserAttachment> attachmentClass, Module module)
    {
        for (User user : this.getLoadedUsers())
        {
            user.attach(attachmentClass, module);
        }
    }

    @Override
    public void detachFromAll(Class<? extends UserAttachment> attachmentClass)
    {
        for (User user : this.getLoadedUsers())
        {
            user.detach(attachmentClass);
        }
    }

    @Override
    public void detachAllOf(Module module)
    {
        for (User user : this.getLoadedUsers())
        {
            user.detachAll(module);
        }
    }

    @Override
    public synchronized void addDefaultAttachment(Class<? extends UserAttachment> attachmentClass, Module module)
    {
        DefaultAttachment attachment = new DefaultAttachment(attachmentClass, module);
        this.defaultAttachments.add(attachment);
        for (User user : this.getLoadedUsers())
        {
            attachment.attachTo(user);
        }
    }

    @Override
    public synchronized void removeDefaultAttachment(Class<? extends UserAttachment> attachmentClass)
    {
        Iterator<DefaultAttachment> it = this.defaultAttachments.iterator();
        while (it.hasNext())
        {
            if (it.next().type == attachmentClass)
            {
                it.remove();
                return;
            }
        }
    }

    @Override
    public synchronized void removeDefaultAttachments(Module module)
    {
        Iterator<DefaultAttachment> it = this.defaultAttachments.iterator();
        while (it.hasNext())
        {
            if (it.next().module == module)
            {
                it.remove();
            }
        }
    }

    @Override
    public synchronized void removeDefaultAttachments()
    {
        this.defaultAttachments.clear();
    }

    @Override
    public Set<Long> getAllIds()
    {
        Set<Long> ids = new HashSet<>();
        for (Record1<UInteger> id : this.database.getDSL().select(TableUser.TABLE_USER.KEY).from(TableUser.TABLE_USER).fetch())
        {
            ids.add(id.value1().longValue());
        }
        return ids;
    }

    @Override
    public synchronized void cleanup(Module module)
    {
        this.removeDefaultAttachments(module);
        this.detachAllOf(module);
    }

    @Override
    public User findUser(String name)
    {
        return this.findUser(name, false);
    }

    @Override
    public User findUser(String name, boolean searchDatabase)
    {
        if (name == null)
        {
            return null;
        }
        // Direct Match Online Players:
        for (User onlineUser : this.getOnlineUsers())
        {
            if (name.equals(onlineUser.getName()))
            {
                return this.getExactUser(onlineUser.getUniqueId());
            }
        }
        // Find Online Players with similar name
        Map<String, User> onlinePlayerMap = new HashMap<>();
        for (User onlineUser : this.getOnlineUsers())
        {
            onlinePlayerMap.put(onlineUser.getName(), onlineUser);
        }
        String foundUser = core.getModularity().start(StringMatcher.class).matchString(name, onlinePlayerMap.keySet());
        if (foundUser != null)
        {
            return this.getExactUser(onlinePlayerMap.get(foundUser).getUniqueId());
        }
        // Lookup in saved users
        UserEntity entity = this.database.getDSL().selectFrom(TableUser.TABLE_USER).where(TableUser.TABLE_USER.LASTNAME.eq(name))
                                         .fetchOne();
        if (entity == null && searchDatabase)
        {
            // Match in saved users
            entity = this.database.getDSL().selectFrom(TableUser.TABLE_USER).where(TableUser.TABLE_USER.LASTNAME.like("%" + name + "%"))
                                  .limit(1).fetchOne();
        }
        if (entity != null)
        {
            User user = this.cachedUserByDbId.get(entity.getKey());
            if (user == null)
            {
                user = new User(core, entity);
                this.cacheUser(user);
            }
            return user;
        }
        return null;
    }

    @Override
    public void shutdown() // TODO shutdown Service
    {
        this.clean();

        this.onlineUsers.clear();
        this.onlineUsers = null;

        this.cachedUserByUUID.clear();
        this.cachedUserByUUID = null;

        this.cachedUserByDbId.clear();
        this.cachedUserByDbId = null;

        this.removeDefaultAttachments();
        this.defaultAttachments.clear();
        this.defaultAttachments = null;

        this.salt = null;
    }

    @Override
    public void clean()
    {
        Timestamp time = new Timestamp(System.currentTimeMillis() - core
            .getConfiguration().usermanager.garbageCollection.getMillis());
        this.database.getDSL().delete(TableUser.TABLE_USER).where(
            TableUser.TABLE_USER.LASTSEEN.le(time), TableUser.TABLE_USER.NOGC.isFalse())
                     .execute();
    }

    protected final class DefaultAttachment
    {
        private final Class<? extends UserAttachment> type;
        private final Module module;

        private DefaultAttachment(Class<? extends UserAttachment> type, Module module)
        {
            this.type = type;
            this.module = module;
        }

        public void attachTo(User user)
        {
            user.attach(this.type, this.module);
        }
    }
}
