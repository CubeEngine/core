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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.marker.Disable;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.module.authorization.storage.TableAuth;
import org.cubeengine.module.core.util.StringUtils;
import org.cubeengine.module.core.util.Triplet;
import org.cubeengine.service.command.CommandManager;
import org.cubeengine.service.database.Database;
import org.cubeengine.service.filesystem.FileManager;
import org.cubeengine.service.filesystem.FileUtil;
import org.cubeengine.service.permission.PermissionManager;
import org.cubeengine.service.user.TableUser;
import org.cubeengine.service.user.MultilingualPlayer;
import org.cubeengine.service.user.UserManager;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.ban.BanService;

import static org.cubeengine.module.authorization.storage.TableAuth.TABLE_AUTH;

@ModuleInfo(name = "Authorization", description = "Provides password authorization")
public class Authorization extends Module
{
    @Inject private FileManager fm;
    @Inject private Database db;
    @Inject private CommandManager cm;
    @Inject private Game game;
    @Inject private BanService bs;
    @Inject private PermissionManager pm;
    @Inject private AuthManager am; // TODO circular dependency? on providing module

    String salt;
    final MessageDigest messageDigest;

    private AuthPerms perms;
    private final Map<UUID, Triplet<Long, String, Integer>> failedLogins = new HashMap<>();
    private AuthConfiguration config;

    public Authorization()
    {
        try
        {
            messageDigest = MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-512 hash algorithm not available!");
        }
    }

    @Enable
    public void onEnable()
    {
        db.registerTable(TableAuth.class);

        loadSalt();

        perms = new AuthPerms(this);
        config = fm.loadConfig(this, AuthConfiguration.class);
        cm.addCommands(this, new AuthCommands(this, game, bs));
    }


    @Disable
    public void onDisable()
    {
        cm.removeCommands(this);
        pm.cleanup(this);
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

    public Triplet<Long, String, Integer> getFailedLogin(Player user)
    {
        return this.failedLogins.get(user.getUniqueId());
    }

    protected void addFailedLogin(Player user)
    {
        Triplet<Long, String, Integer> loginFail = this.getFailedLogin(user);
        if (loginFail == null)
        {
            loginFail = new Triplet<>(System.currentTimeMillis(), user.getConnection().getAddress().getAddress().getHostAddress(), 1);
            this.failedLogins.put(user.getUniqueId(), loginFail);
        }
        else
        {
            loginFail.setFirst(System.currentTimeMillis());
            loginFail.setSecond(user.getConnection().getAddress().getAddress().getHostAddress());
            loginFail.setThird(loginFail.getThird() + 1);
        }
    }

    protected void removeFailedLogins(MultilingualPlayer user)
    {
        this.failedLogins.remove(user.getUniqueId());
    }

    public void resetAllPasswords()
    {
        this.db.getDSL().update(TableUser.TABLE_USER).set(TABLE_AUTH.PASSWD, (byte[])null).execute();
        am.reset();
    }

    public AuthPerms perms()
    {
        return perms;
    }

    public AuthConfiguration getConfig()
    {
        return config;
    }

    public AuthManager getManager()
    {
        return am;
    }
}
