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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.filesystem.FileManager;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.Triplet;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.THashSet;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

/**
 * This Manager provides methods to access the Users and saving/loading from
 * database.
 */
public abstract class AbstractUserManager implements UserManager
{
    private final Core core;
    protected List<User> onlineUsers;
    protected ConcurrentHashMap<Object, User> cachedUsers;
    protected Set<DefaultAttachment> defaultAttachments;
    protected String salt;
    protected MessageDigest messageDigest;
    private Random random;

    protected Database database;

    public AbstractUserManager(final Core core)
    {
        this.database = core.getDB();

        this.core = core;

        this.cachedUsers = new ConcurrentHashMap<>();
        this.onlineUsers = new CopyOnWriteArrayList<>();

        this.defaultAttachments = new THashSet<>();

        this.random = new Random();
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

    public boolean login(User user, String password)
    {
        if (!user.isLoggedIn())
        {
            user.loggedInState = this.checkPassword(user, password);
        }
        core.getEventManager().fireEvent(new UserAuthorizedEvent(this.core, user));
        return user.isLoggedIn();
    }

    public boolean checkPassword(User user, String password)
    {
        synchronized (this.messageDigest)
        {
            messageDigest.reset();
            password += this.salt;
            password += user.getEntity().getFirstseen().toString();
            return Arrays.equals(user.getEntity().getPasswd(), messageDigest.digest(password.getBytes()));
        }
    }

    public void setPassword(User user, String password)
    {
        synchronized (this.messageDigest)
        {
            this.messageDigest.reset();
            password += this.salt;
            password += user.getEntity().getFirstseen().toString();
            user.getEntity().setPasswd(this.messageDigest.digest(password.getBytes()));
            user.getEntity().update();
        }
    }

    public void resetPassword(User user)
    {
        user.getEntity().setPasswd(null);
        user.getEntity().update();
    }

    public void resetAllPasswords()
    {
        this.database.getDSL().update(TABLE_USER)
            .set(DSL.row(TABLE_USER.PASSWD), DSL.row(new byte[0]))
            .execute();
        for (User user : this.getLoadedUsers())
        {
            user.getEntity().refresh();
        }
    }

    public void removeUser(final User user)
    {
        user.getEntity().delete();
        this.removeCachedUser(user);
    }

    public User getExactUser(String name)
    {
        return this.getUser(name, true);
    }

    public User getExactUser(CommandSender sender)
    {
        if (sender == null)
        {
            return null;
        }
        if (sender instanceof User)
        {
            return this.getExactUser(sender.getName());
        }
        return null;
    }

    public synchronized User getUser(long id)
    {
        User user = this.cachedUsers.get(id);
        if (user != null)
        {
            return user;
        }
        UserEntity entity = this.database.getDSL().select().from(TABLE_USER).where(TABLE_USER.KEY.eq(UInteger.valueOf(id))).fetchOne().into(TABLE_USER);
        if (entity == null)
        {
            return null;
        }
        user = new User(entity);
        this.cacheUser(user);
        return user;
    }

    public User getUser(String name)
    {
        return this.getUser(name, false);
    }

    public synchronized User getUser(String name, boolean create)
    {
        if (name == null)
        {
            throw new NullPointerException();
        }
        User user = this.cachedUsers.get(name.toLowerCase());
        if (user == null)
        {
            user = this.loadUser(name);
        }
        if (user == null && create)
        {
            user = this.createUser(name);
        }
        return user;
    }

    protected synchronized User loadUser(String name)
    {
        UserEntity entity = this.database.getDSL().select().from(TABLE_USER).where(TABLE_USER.PLAYER.eq(name)).fetchOne().into(TABLE_USER);
        if (entity != null)
        {
            User user = new User(entity);
            this.cacheUser(user);
            return user;
        }
        return null;
    }

    /**
     * Adds a new User
     *
     * @return the created User
     */
    protected synchronized User createUser(String name)
    {
        User user = this.cachedUsers.get(name.toLowerCase());
        if (user != null)
        {
            //User was already added
            return user;
        }
        user = new User(this.core, name);
        user.getEntity().insert();
        this.cacheUser(user);

        return user;
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
        this.core.getLog().debug("User "+ user.getName()+ " cached!");
        this.cachedUsers.put(user.getName().toLowerCase(), user);
        this.cachedUsers.put(user.getId(), user);
        this.attachDefaults(user);
    }

    protected synchronized void removeCachedUser(User user)
    {
        this.core.getLog().debug("Removed cached user "+ user.getName()+ "!");
        this.cachedUsers.remove(user.getName().toLowerCase());
        this.cachedUsers.remove(user.getId());
        user.detachAll();
    }

    public synchronized Set<User> getOnlineUsers()
    {
        return new THashSet<User>(this.onlineUsers);
    }

    public synchronized Set<User> getLoadedUsers()
    {
        return new THashSet<User>(this.cachedUsers.values());
    }

    public User findOnlineUser(String name)
    {
        User user = this.findUser(name);
        if (user != null && user.isOnline())
        {
            return user;
        }
        return null;
    }

    public void broadcastMessage(String message, Permission perm, Object... params)
    {
        for (User user : this.onlineUsers)
        {
            if (perm == null || perm.isAuthorized(user))
            {
                user.sendTranslated(message, params);
            }
        }
        this.core.getCommandManager().getConsoleSender().sendTranslated(message, params);
    }

    public void broadcastMessage(String message, Object... args)
    {
        this.broadcastMessage(message, null, args);
    }

    public void broadcastStatus(ChatFormat starColor, String message, CommandSender sender, Object... args)
    {
        message = ChatFormat.parseFormats(message);
        if (args != null && args.length != 0)
        {
            message = String.format(message,args); //TODO is allowed to use color ?
        }
        String name = sender.getDisplayName();
        for (User user : this.onlineUsers)
        {
            user.sendTranslated(starColor.toString() + "* &2%s &f%s", name, message);
        }
    }

    public void broadcastStatus(String message, CommandSender sender, Object... args)
    {
        this.broadcastStatus(ChatFormat.WHITE, message, sender, args);
    }

    private void loadSalt()
    {
        File file = new File(this.core.getFileManager().getDataFolder(), ".salt");
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            this.salt = reader.readLine();
            reader.close();
        }
        catch (FileNotFoundException e)
        {
            if (this.salt == null)
            {
                try
                {
                    this.salt = StringUtils.randomString(this.random, 32);
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(this.salt);
                    fileWriter.close();
                }
                catch (Exception inner)
                {
                    throw new IllegalStateException("Could not store the static salt in '" + file.getAbsolutePath() + "'!", inner);
                }
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not store the static salt in '" + file.getAbsolutePath() + "'!", e);
        }
        FileManager.hideFile(file);
        file.setReadOnly();
    }

    private TLongObjectHashMap<Triplet<Long, String, Integer>> failedLogins = new TLongObjectHashMap<Triplet<Long, String, Integer>>();

    public Triplet<Long, String, Integer> getFailedLogin(User user)
    {
        return this.failedLogins.get(user.getId());
    }

    protected void addFailedLogin(User user)
    {
        Triplet<Long, String, Integer> loginFail = this.getFailedLogin(user);
        if (loginFail == null)
        {
            loginFail = new Triplet<Long, String, Integer>(System.currentTimeMillis(), user.getAddress().getAddress().getHostAddress(), 1);
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

    public synchronized void kickAll(String message)
    {
        for (User user : this.cachedUsers.values())
        {
            user.kickPlayer(message);
        }
    }

    public synchronized void kickAll(String message, Object... params)
    {
        for (User user : this.cachedUsers.values())
        {
            user.kickPlayer(user.translate(message, params));
        }
    }

    public void attachToAll(Class<? extends UserAttachment> attachmentClass, Module module)
    {
        for (User user : this.getLoadedUsers())
        {
            user.attach(attachmentClass, module);
        }
    }

    public void detachFromAll(Class<? extends UserAttachment> attachmentClass)
    {
        for (User user : this.getLoadedUsers())
        {
            user.detach(attachmentClass);
        }
    }

    public void detachAllOf(Module module)
    {
        for (User user : this.getLoadedUsers())
        {
            user.detachAll(module);
        }
    }

    public synchronized void addDefaultAttachment(Class<? extends UserAttachment> attachmentClass, Module module)
    {
        DefaultAttachment attachment = new DefaultAttachment(attachmentClass, module);
        this.defaultAttachments.add(attachment);
        for (User user : this.getLoadedUsers())
        {
            attachment.attachTo(user);
        }
    }

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

    public synchronized void removeDefaultAttachments()
    {
        this.defaultAttachments.clear();
    }

    public Set<Long> getAllIds()
    {
        Set<Long> ids = new HashSet<>();
        for (Record1<UInteger> id : this.database.getDSL().select(TABLE_USER.KEY).from(TABLE_USER).fetch())
        {
            ids.add(id.value1().longValue());
        }
        return ids;
    }

    public synchronized void cleanup(Module module)
    {
        this.removeDefaultAttachments(module);
        this.detachAllOf(module);
    }

    @Override
    public void shutdown()
    {
        this.clean();

        this.onlineUsers.clear();
        this.onlineUsers = null;

        this.cachedUsers.clear();
        this.cachedUsers = null;

        this.removeDefaultAttachments();
        this.defaultAttachments.clear();
        this.defaultAttachments = null;

        this.salt = null;

        this.messageDigest = null;

        this.random = null;
    }

    @Override
    public void clean()
    {
        Timestamp time = new Timestamp(System.currentTimeMillis() - core.getConfiguration().userManagerCleanupDatabase.toMillis());
        this.database.getDSL().delete(TABLE_USER).where(TABLE_USER.LASTSEEN.le(time), TABLE_USER.NOGC.isFalse()).execute();
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
