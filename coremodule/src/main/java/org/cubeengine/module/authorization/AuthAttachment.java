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
import org.cubeengine.module.authorization.storage.Auth;
import org.cubeengine.module.authorization.storage.TableAuth;
import org.cubeengine.module.core.sponge.EventManager;
import org.cubeengine.service.database.Database;
import org.cubeengine.service.user.UserAttachment;
import org.cubeengine.service.user.UserAuthorizedEvent;
import org.jooq.DSLContext;

import static org.cubeengine.service.user.TableUser.TABLE_USER;

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
        getModule().getModularity().provide(EventManager.class).fireEvent(new UserAuthorizedEvent(getModule(), getHolder()));
        return isLoggedIn;
    }

    public boolean checkPassword(String password)
    {
        synchronized (getModule().messageDigest)
        {
            getModule().messageDigest.reset();
            password += getModule().salt;
            password += salt2();
            return Arrays.equals(auth.getValue(TableAuth.TABLE_AUTH.PASSWD), getModule().messageDigest.digest(password.getBytes()));
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
            auth.setValue(TableAuth.TABLE_AUTH.PASSWD, getModule().messageDigest.digest(password.getBytes()));
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
        auth.setValue(TableAuth.TABLE_AUTH.PASSWD, null);
        auth.updateAsync();
    }

    @Override
    public void onAttach()
    {
        DSLContext dsl = getModule().getModularity().provide(Database.class).getDSL();
        auth = dsl.selectFrom(TableAuth.TABLE_AUTH).where(TableAuth.TABLE_AUTH.ID.eq(getHolder().getEntity().getId())).fetchOne();
        if (auth == null)
        {
            auth = dsl.newRecord(TableAuth.TABLE_AUTH).newAuth(getHolder());
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
        return auth.getValue(TableAuth.TABLE_AUTH.PASSWD) != null;
    }
}
