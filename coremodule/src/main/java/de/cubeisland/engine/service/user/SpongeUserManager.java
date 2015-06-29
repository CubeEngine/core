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
package de.cubeisland.engine.service.user;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import com.google.common.base.Optional;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.sponge.EventManager;
import de.cubeisland.engine.module.core.util.converter.UserConverter;
import de.cubeisland.engine.module.core.util.formatter.MessageType;
import de.cubeisland.engine.module.core.util.matcher.StringMatcher;
import de.cubeisland.engine.reflect.Reflector;
import de.cubeisland.engine.service.command.CommandManager;
import de.cubeisland.engine.service.command.CommandSender;
import de.cubeisland.engine.service.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.service.database.Database;
import de.cubeisland.engine.service.i18n.I18n;
import de.cubeisland.engine.service.permission.Permission;
import de.cubeisland.engine.service.task.TaskManager;
import org.jooq.Record1;
import org.jooq.types.UInteger;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.service.profile.GameProfileResolver;
import org.spongepowered.api.service.user.UserStorage;
import org.spongepowered.api.text.format.BaseFormatting;
import org.spongepowered.api.text.format.TextColors;

import static de.cubeisland.engine.module.core.util.ChatFormat.fromLegacy;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.NONE;
import static de.cubeisland.engine.service.user.TableUser.TABLE_USER;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toSet;

/**
 * This Manager provides methods to access the Users and saving/loading from
 * database.
 */
@ServiceImpl(UserManager.class)
@Version(1)
public class SpongeUserManager implements UserManager
{
    @Inject private Database database;
    @Inject private CoreModule core;
    @Inject private CommandManager cm;
    @Inject private TaskManager tm;
    @Inject private EventManager em;
    @Inject private I18n i18n;
    @Inject private Reflector reflector;

    protected List<User> onlineUsers = new CopyOnWriteArrayList<>();
    protected Set<DefaultAttachment> defaultAttachments = new HashSet<>();

    private final Map<UUID, User> userByUUID = new ConcurrentHashMap<>();
    private final Map<UInteger, User> userByUInteger = new ConcurrentHashMap<>();

    protected ScheduledExecutorService nativeScheduler;
    protected Map<UUID, UUID> scheduledForRemoval = new HashMap<>();

    public void onEnable()
    {
        database.registerTable(TableUser.class);

        final long delay = (long)core.getConfiguration().usermanager.cleanup;
        this.nativeScheduler = Executors.newSingleThreadScheduledExecutor(core.getProvided(ThreadFactory.class));
        this.nativeScheduler.scheduleAtFixedRate(new UserCleanupTask(), delay, delay, TimeUnit.MINUTES);

        em.registerListener(core, new UserListener(this, tm, core));
        em.registerListener(core, new AttachmentHookListener(this));

        reflector.getDefaultConverterManager().registerConverter(new UserConverter(this), User.class);
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
        User user = userByUUID.get(uuid);
        if (user != null)
        {
            return user;
        }
        user = new User(i18n, this, uuid);
        cacheUser(user);
        return user;
    }

    public CompletableFuture<UserEntity> loadEntity(UUID uuid)
    {
        return database.queryOne(database.getDSL().selectFrom(TABLE_USER)
                                  .where(TABLE_USER.LEAST.eq(uuid.getLeastSignificantBits())
                                    .and(TABLE_USER.MOST.eq(uuid.getMostSignificantBits()))));
    }

    @Override
    public synchronized User getUser(UInteger id)
    {
        User user = this.userByUInteger.get(id);
        if (user != null)
        {
            return user;
        }
        UserEntity entity = this.database.getDSL().selectFrom(TABLE_USER).where(TABLE_USER.KEY.eq(id)).fetchOne();
        if (entity == null)
        {
            return null;
        }
        user = new User(i18n, this, entity.getUniqueId());
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
    protected User getUser(String name, boolean create)
    {
        for (User user : this.getOnlineUsers())
        {
            if (user.getName().equalsIgnoreCase(name))
            {
                return user;
            }
        }
        UserEntity userEntity = database.getDSL().selectFrom(TABLE_USER).where(TABLE_USER.LASTNAME.eq(name.toLowerCase())).fetchOne();
        if (userEntity != null)
        {
            org.spongepowered.api.entity.player.User offlinePlayer = getOfflinePlayer(name);
            if (offlinePlayer.getUniqueId().equals(userEntity.getUniqueId()))
            {
                User user = new User(i18n, this, userEntity.getUniqueId());
                this.cacheUser(user);
                return user;
            }
            userEntity.setValue(TABLE_USER.LASTNAME, this.core.getConfiguration().nameConflict.replace("{name}", userEntity.getValue(TABLE_USER.LASTNAME)));
            userEntity.updateAsync();
        }
        if (create)
        {
            org.spongepowered.api.entity.player.User offlinePlayer = getOfflinePlayer(name);
            User user = new User(i18n, this, userEntity.getUniqueId());
            user.getEntity().insertAsync();
            this.cacheUser(user);
            return user;
        }
        return null;
    }

    private org.spongepowered.api.entity.player.User getOfflinePlayer(String name)
    {
        Optional<Player> player = core.getGame().getServer().getPlayer(name);
        return player.orNull();
        // TODO actually get User when offline
    }


    @Override
    public String getUserName(UInteger key)
    {
        Record1<String> record1 = this.database.getDSL().select(TABLE_USER.LASTNAME).from(TABLE_USER)
                                               .where(TABLE_USER.KEY.eq(key)).fetchOne();
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
        userByUUID.put(user.getUniqueId(), user);
        user.entity().thenAccept(e -> userByUInteger.put(e.getId(), user));
        this.core.getLog().debug("User {} cached!", user.getName());
        this.attachDefaults(user);
    }

    protected void updateLastName(User user)
    {
        user.entity().thenAccept(e -> {
            if (!user.getName().equalsIgnoreCase(e.getValue(TABLE_USER.LASTNAME)))
            {
                e.setValue(TABLE_USER.LASTNAME, user.getName());
                e.updateAsync();
            }
        });
    }

    protected synchronized void removeCachedUser(User user)
    {
        this.userByUUID.remove(user.getUniqueId());
        this.userByUInteger.remove(user.getEntity().getId());
        this.core.getLog().debug("Removed cached user {}!", user.getName());
        user.detachAll();
    }

    @Override
    public synchronized Set<User> getOnlineUsers()
    {
        Set<User> users = new HashSet<>(this.onlineUsers);


        // TODO remove once this is working correclty
        Iterator<User> it = users.iterator();

        User user;
        while (it.hasNext())
        {
            user = it.next();
            if (!user.getPlayer().isPresent())
            {
                core.getLog().warn("Found an offline player in the online players list: {}({})", user.getDisplayName(),
                                   user.getUniqueId());
                this.onlineUsers.remove(user);
                it.remove();
            }
        }

        return users;
    }

    @Override
    public synchronized Set<User> getLoadedUsers()
    {
        return new HashSet<>(userByUUID.values());
    }

    @Override
    public void broadcastTranslatedWithPerm(MessageType messageType, String message, Permission perm, Object... params)
    {
        if (message.isEmpty())
        {
            return;
        }

        onlineUsers.stream().filter(user -> perm == null || perm.isAuthorized(user)).forEach(user -> user.sendTranslated(messageType, message, params));
        cm.getConsoleSender().sendTranslated(messageType, message, params);
    }

    @Override
    public void broadcastMessageWithPerm(MessageType type, String message, Permission perm, Object... params)
    {
        if (message.isEmpty())
        {
            return;
        }
        onlineUsers.stream().filter(user -> perm == null || perm.isAuthorized(user)).forEach(user -> user.sendMessage(
            NONE, message, params));
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

    @Override
    public synchronized void kickAll(String message)
    {
        for (Player player : core.getGame().getServer().getOnlinePlayers())
        {
            player.kick(fromLegacy(i18n.getTranslation(NONE, player.getLocale(), message).getTranslation().get(player.getLocale()), '&'));
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
        this.getLoadedUsers().forEach(attachment::attachTo);
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

    // TODO UInteger?
    public Set<UInteger> getAllIds()
    {
        return database.getDSL().select(TABLE_USER.KEY).from(TABLE_USER).fetch().stream().map(Record1::value1).collect(toSet());
    }

    @Override
    public synchronized void cleanup(Module module)
    {
        // TODO attach to disable from modules
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
        UserEntity entity = this.database.getDSL().selectFrom(TABLE_USER).where(TABLE_USER.LASTNAME.eq(name))
                                         .fetchOne();
        if (entity == null && searchDatabase)
        {
            // Match in saved users
            entity = this.database.getDSL().selectFrom(TABLE_USER).where(TABLE_USER.LASTNAME.like("%" + name + "%"))
                                  .limit(1).fetchOne();
        }
        if (entity != null)
        {
            User user = this.userByUInteger.get(entity.getId());
            if (user == null)
            {
                user = new User(i18n, this, entity.getUniqueId());
                this.cacheUser(user);
            }
            return user;
        }
        return null;
    }

    protected void shutdown()
    {
        this.clean();

        this.onlineUsers.clear();
        this.onlineUsers = null;

        this.userByUInteger.clear();

        this.userByUUID.clear();

        this.removeDefaultAttachments();
        this.defaultAttachments.clear();
        this.defaultAttachments = null;

        for (UUID id : this.scheduledForRemoval.values())
        {
            tm.cancelTask(core, id);
        }

        this.scheduledForRemoval.clear();
        this.scheduledForRemoval = null;

        this.nativeScheduler.shutdown();
        try
        {
            this.nativeScheduler.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException ignored)
        {
            Thread.currentThread().interrupt();
        }
        finally
        {
            this.nativeScheduler.shutdownNow();
            this.nativeScheduler = null;
        }
    }

    @Override
    public void clean()
    {
        Timestamp time = new Timestamp(currentTimeMillis() - core.getConfiguration().usermanager.garbageCollection.getMillis());
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

    @Override
    public UserEntity getEntity(UUID uuid)
    {
        try
        {
            return loadEntity(uuid).get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public org.spongepowered.api.entity.player.User getPlayer(UUID uuid)
    {
        Optional<Player> player = core.getGame().getServer().getPlayer(uuid);
        if (player.isPresent())
        {
            return player.get();
        }
        UserStorage storage = core.getGame().getServiceManager().provide(UserStorage.class).get();

        org.spongepowered.api.entity.player.User user = storage.get(uuid).orNull();
        if (user != null)
        {
            return user;
        }
        GameProfileResolver resolver = core.getGame().getServiceManager().provide(GameProfileResolver.class).get();
        try
        {
            return storage.getOrCreate(resolver.get(uuid).get());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private class UserCleanupTask implements Runnable
    {
        @Override
        public void run()
        {
            getLoadedUsers().stream().filter(user -> !user.getPlayer().isPresent()).filter(
                user -> scheduledForRemoval.containsKey(user.getUniqueId())).forEach(
                SpongeUserManager.this::removeCachedUser);
        }
    }
}
