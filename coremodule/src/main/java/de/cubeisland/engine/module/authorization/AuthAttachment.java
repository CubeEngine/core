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
package de.cubeisland.engine.module.authorization;

import java.util.Arrays;
import de.cubeisland.engine.module.authorization.storage.Auth;
import de.cubeisland.engine.module.core.sponge.EventManager;
import de.cubeisland.engine.service.database.Database;
import de.cubeisland.engine.service.user.UserAttachment;
import de.cubeisland.engine.service.user.UserAuthorizedEvent;
import org.jooq.DSLContext;

import static de.cubeisland.engine.module.authorization.storage.TableAuth.TABLE_AUTH;
import static de.cubeisland.engine.service.user.TableUser.TABLE_USER;

public class AuthAttachment extends UserAttachment
{
    private boolean isLoggedIn;
    private Auth auth;

    public boolean login(String password)
    {
        if (!isLoggedIn)
        {
            isLoggedIn = this.checkPassword(password);
        }
        getModule().getModularity().start(EventManager.class).fireEvent(new UserAuthorizedEvent(getModule(), getHolder()));
        return isLoggedIn;
    }

    public boolean checkPassword(String password)
    {
        synchronized (getModule().messageDigest)
        {
            getModule().messageDigest.reset();
            password += getModule().salt;
            password += salt2();
            return Arrays.equals(auth.getValue(TABLE_AUTH.PASSWD), getModule().messageDigest.digest(password.getBytes()));
        }
    }

    private String salt2()
    {
        return getHolder().getEntity().getValue(TABLE_USER.FIRSTSEEN).toString();
    }

    public void setPassword(String password)
    {
        synchronized (getModule().messageDigest)
        {
            getModule().messageDigest.reset();
            password += getModule().salt;
            password += salt2();
            auth.setValue(TABLE_AUTH.PASSWD, getModule().messageDigest.digest(password.getBytes()));
            auth.updateAsync();
        }
    }

    @Override
    public Authorization getModule()
    {
        return (Authorization)super.getModule();
    }

    public void resetPassword()
    {
        auth.setValue(TABLE_AUTH.PASSWD, null);
        auth.updateAsync();
    }

    @Override
    public void onAttach()
    {
        DSLContext dsl = getModule().getModularity().start(Database.class).getDSL();
        auth = dsl.selectFrom(TABLE_AUTH).where(TABLE_AUTH.ID.eq(getHolder().getEntity().getId())).fetchOne();
        if (auth == null)
        {
            auth = dsl.newRecord(TABLE_AUTH).newAuth(getHolder());
            auth.insertAsync();
        }
    }

    public boolean isLoggedIn()
    {
        return isLoggedIn;
    }

    public void logout()
    {
        isLoggedIn = false;
    }

    public boolean isPasswordSet()
    {
        return auth.getValue(TABLE_AUTH.PASSWD) != null;
    }
}
