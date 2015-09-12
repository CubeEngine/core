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
package org.cubeengine.module.authorization;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.asm.marker.Version;
import org.cubeengine.module.authorization.storage.Auth;
import org.cubeengine.module.authorization.storage.TableAuth;
import org.cubeengine.module.core.sponge.EventManager;
import org.cubeengine.service.database.Database;
import org.cubeengine.service.user.CachedUser;
import org.cubeengine.service.user.UserManager;
import org.jooq.DSLContext;

import static org.cubeengine.module.authorization.storage.TableAuth.TABLE_AUTH;
import static org.cubeengine.service.user.TableUser.TABLE_USER;

@ServiceProvider(AuthManager.class)
@Version(1)
public class AuthManager
{
    @Inject private Authorization module;
    @Inject private UserManager um;
    @Inject private Database db;
    @Inject private EventManager em;

    private Map<UUID, Auth> auths = new ConcurrentHashMap<>();
    private Set<UUID> loggedIn = new CopyOnWriteArraySet<>();

    public boolean isLoggedIn(UUID player)
    {
        return loggedIn.contains(player);
    }

    public void logout(UUID player)
    {
        loggedIn.remove(player);
    }

    public boolean isPasswordSet(UUID player)
    {
        return getAuth(player).getValue(TABLE_AUTH.PASSWD) != null;
    }

    public void resetPassword(UUID player)
    {
        Auth auth = getAuth(player);
        auth.setValue(TableAuth.TABLE_AUTH.PASSWD, null);
        auth.updateAsync();
    }

    public void setPassword(UUID player, String password)
    {
        synchronized (module.messageDigest)
        {
            module.messageDigest.reset();
            password += module.salt;
            password += salt2(player);
            Auth auth = getAuth(player);
            auth.setValue(TableAuth.TABLE_AUTH.PASSWD, module.messageDigest.digest(password.getBytes()));
            auth.updateAsync();
        }
    }


    public boolean checkPassword(UUID player, String password)
    {
        synchronized (module.messageDigest)
        {
            module.messageDigest.reset();
            password += module.salt;
            password += salt2(player);
            return Arrays.equals(getAuth(player).getValue(TableAuth.TABLE_AUTH.PASSWD), module.messageDigest.digest(password.getBytes()));
        }
    }

    private String salt2(UUID player)
    {
        return um.getByUUID(player).getEntity().getValue(TABLE_USER.FIRSTSEEN).toString();
    }


    private Auth getAuth(UUID player)
    {
        Auth auth = this.auths.get(player);
        if (auth == null)
        {
            DSLContext dsl = db.getDSL();
            CachedUser byUUID = um.getByUUID(player);
            auth = dsl.selectFrom(TABLE_AUTH).where(TABLE_AUTH.ID.eq(byUUID.getEntity().getId())).fetchOne();
            if (auth == null)
            {
                auth = dsl.newRecord(TABLE_AUTH).newAuth(byUUID);
                auth.insert();
            }
            this.auths.put(player, auth);
        }
        return auth;
    }


    public boolean login(UUID player, String password)
    {
        if (!isLoggedIn(player))
        {
            if (this.checkPassword(player, password))
            {
                loggedIn.add(player);
                em.fireEvent(new UserAuthorizedEvent(module, um.getUser(player)));
            }
        }
        return isLoggedIn(player);
    }

    public void reset()
    {
        auths.clear();
        loggedIn.clear();
    }
}
