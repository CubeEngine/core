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
package org.cubeengine.service.user;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import com.google.common.base.Optional;
import de.cubeisland.engine.converter.ConverterManager;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.marker.Disable;
import de.cubeisland.engine.modularity.core.marker.Enable;
import de.cubeisland.engine.reflect.Reflector;
import org.cubeengine.module.core.sponge.CoreModule;
import org.cubeengine.module.core.sponge.EventManager;
import org.cubeengine.module.core.util.converter.UserConverter;
import org.cubeengine.module.core.util.matcher.StringMatcher;
import org.cubeengine.service.command.CommandManager;
import org.cubeengine.service.database.Database;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.task.TaskManager;
import org.jooq.Record1;
import org.jooq.types.UInteger;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.profile.GameProfileResolver;
import org.spongepowered.api.service.user.UserStorage;

import static java.lang.System.currentTimeMillis;
import static org.cubeengine.service.user.TableUser.TABLE_USER;

/**
 * This Manager provides methods to access the Users and saving/loading from database.
 */
@ServiceProvider(UserManager.class)
@Version(1)
public class UserManager
{
    @Inject private Database database;
    @Inject private CoreModule core;
    @Inject private CommandManager cm;
    @Inject private TaskManager tm;
    @Inject private EventManager em;
    @Inject private I18n i18n;
    @Inject private Reflector reflector;
    @Inject private Game game;

    protected ScheduledExecutorService nativeScheduler;
    protected Map<UUID, UUID> scheduledForRemoval = new HashMap<>();

    private Map<UUID, CachedUser> byUUIDs = new ConcurrentHashMap<>();
    private Map<UInteger, CachedUser> byIds = new ConcurrentHashMap<>();

    @Enable
    public void onEnable()
    {
        database.registerTable(TableUser.class);

        final long delay = 10; // TODO (long)core.getConfiguration().usermanager.cleanup;
        this.nativeScheduler = Executors.newSingleThreadScheduledExecutor(core.getProvided(ThreadFactory.class));
        this.nativeScheduler.scheduleAtFixedRate(new UserCleanupTask(), delay, delay, TimeUnit.MINUTES);

        em.registerListener(core, new UserListener(this, tm, core));

        ConverterManager manager = reflector.getDefaultConverterManager();
        manager.registerConverter(new UserConverter(this), User.class);

    }

    public CachedUser getByUUID(UUID uuid)
    {
        CachedUser cachedUser = byUUIDs.get(uuid);
        if (cachedUser != null)
        {
            return cachedUser;
        }
        UserEntity entity = database.getDSL().selectFrom(TABLE_USER).where(TABLE_USER.LEAST.eq(
            uuid.getLeastSignificantBits()).and(TABLE_USER.MOST.eq(uuid.getMostSignificantBits()))).fetchOne();
        if (entity == null)
        {
            User user = getUser(uuid);
            if (user == null)
            {
                throw new IllegalArgumentException("Could not get User with UUID: " + uuid.toString());
            }
            entity = database.getDSL().newRecord(TABLE_USER).newUser(user);
            entity.store();
        }
        return cachedUser(entity).get();
    }

    private Optional<CachedUser> cachedUser(UserEntity entity)
    {
        if (entity == null)
        {
            return Optional.absent();
        }
        CachedUser cachedUser = new CachedUser(entity, game.getServiceManager().provideUnchecked(UserStorage.class).get(entity.getUniqueId()).orNull());
        cacheUser(cachedUser);
        return Optional.of(cachedUser);
    }

    public Optional<CachedUser> getById(UInteger id)
    {
        CachedUser cachedUser = byIds.get(id);
        if (cachedUser != null)
        {
            return Optional.of(cachedUser);
        }
        UserEntity entity = this.database.getDSL().selectFrom(TABLE_USER).where(TABLE_USER.KEY.eq(id)).fetchOne();
        return cachedUser(entity);
    }

    private User getOfflinePlayer(String name)
    {
        Optional<Player> player = core.getGame().getServer().getPlayer(name);
        return player.orNull();
        // TODO actually get User when offline
    }


    public String getUserName(UInteger key)
    {
        Record1<String> record1 = this.database.getDSL().select(TABLE_USER.LASTNAME).from(TABLE_USER)
                                               .where(TABLE_USER.KEY.eq(key)).fetchOne();
        return record1 == null ? null : record1.value1();
    }

    protected synchronized void cacheUser(CachedUser user)
    {
        byIds.put(user.getEntity().getId(), user);
        byUUIDs.put(user.getEntity().getUniqueId(), user);
        updateLastName(user);
        this.core.getLog().debug("User {} cached!", user.getUser().getName());
    }

    protected void updateLastName(CachedUser user)
    {
        UserEntity entity = user.getEntity();
        if (!entity.getValue(TABLE_USER.LASTNAME).equals(user.getUser().getName()))
        {
            entity.setValue(TABLE_USER.LASTNAME, user.getUser().getName());
            entity.updateAsync();
        }
    }

    protected synchronized void removeCached(UUID user)
    {
        CachedUser removed = byUUIDs.remove(user);
        if (removed != null)
        {
            byIds.remove(removed.getEntity().getId());
            this.core.getLog().debug("Removed cached user {}!", removed.getUser().getName());
        }
    }



    // return database.getDSL().select(TABLE_USER.KEY).from(TABLE_USER).fetch().stream().map(Record1::value1).collect(toSet());


    public User findUser(String name)
    {
        return this.findUser(name, false);
    }

    public Optional<User> getByName(String name)
    {
        if (name == null)
        {
            return null;
        }
        // Direct Match Online Players:
        Optional<Player> player = game.getServer().getPlayer(name);
        if (player.isPresent())
        {
            return player.transform(p -> (User)p);
        }

        // Lookup in saved users
        UserEntity entity = this.database.getDSL().selectFrom(TABLE_USER).where(TABLE_USER.LASTNAME.eq(name)).fetchOne();
        return cachedUser(entity).transform(CachedUser::getUser);
    }

    public User findUser(String name, boolean searchDatabase)
    {

        if (name == null)
        {
            return null;
        }
        // Direct Match Online Players:
        Optional<Player> player = game.getServer().getPlayer(name);
        if (player.isPresent())
        {
            return player.get();
        }

        // Find Online Players with similar name
        Map<String, Player> onlinePlayerMap = new HashMap<>();
        for (Player onlineUser : game.getServer().getOnlinePlayers())
        {
            onlinePlayerMap.put(onlineUser.getName(), onlineUser);
        }
        String foundUser = core.getModularity().provide(StringMatcher.class).matchString(name, onlinePlayerMap.keySet());
        if (foundUser != null)
        {
            return onlinePlayerMap.get(foundUser);
        }
        // Lookup in saved users
        UserEntity entity = this.database.getDSL().selectFrom(TABLE_USER).where(TABLE_USER.LASTNAME.eq(name)).fetchOne();
        if (entity == null && searchDatabase)
        {
            // Startswith in saved users
            entity = this.database.getDSL().selectFrom(TABLE_USER).where(TABLE_USER.LASTNAME.like(name + "%")).limit(1).fetchOne();
        }

        Optional<CachedUser> cachedUser = cachedUser(entity);
        if (cachedUser.isPresent())
        {
            return cachedUser.get().getUser();
        }
        return null;
    }

    @Disable
    protected void shutdown()
    {
        Timestamp time = new Timestamp(currentTimeMillis() - core.getConfiguration().usermanager.garbageCollection.getMillis());
        this.database.getDSL().delete(TABLE_USER).where(TABLE_USER.LASTSEEN.le(time), TABLE_USER.NOGC.isFalse()).execute();

        this.byIds.clear();
        this.byUUIDs.clear();

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

    public User getUser(UUID uuid)
    {
        Optional<Player> player = core.getGame().getServer().getPlayer(uuid);
        if (player.isPresent())
        {
            return player.get();
        }
        UserStorage storage = core.getGame().getServiceManager().provide(UserStorage.class).get();

        User user = storage.get(uuid).orNull();
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
            byUUIDs.values().stream()
                   .map(CachedUser::getUser)
                   .filter(user -> !user.isOnline())
                   .map(User::getUniqueId)
                   .filter(scheduledForRemoval::containsKey)
                   .forEach(UserManager.this::removeCached);
        }
    }



    /*
public boolean canSee(Player player)
{
    // TODO impls of this is missing at other locations
    return getPlayer().isPresent() && player.get(INVISIBILITY_DATA).transform(
        p -> p.invisibleToPlayerIds().contains(getUniqueId())).or(false);
}
*/


}
