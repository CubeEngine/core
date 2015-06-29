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
import de.cubeisland.engine.modularity.asm.marker.Enable;
import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.authorization.storage.TableAuth;
import de.cubeisland.engine.service.filesystem.FileManager;
import de.cubeisland.engine.service.filesystem.FileUtil;
import de.cubeisland.engine.module.core.util.StringUtils;
import de.cubeisland.engine.module.core.util.Triplet;
import de.cubeisland.engine.service.ban.BanManager;
import de.cubeisland.engine.service.command.CommandManager;
import de.cubeisland.engine.service.database.Database;
import de.cubeisland.engine.service.user.TableUser;
import de.cubeisland.engine.service.user.User;
import de.cubeisland.engine.service.user.UserManager;
import org.spongepowered.api.Game;

import static de.cubeisland.engine.module.authorization.storage.TableAuth.TABLE_AUTH;

@ModuleInfo(name = "Authorizazion", description = "Provides password authorization")
public class Authorization extends Module
{
    @Inject private FileManager fm;
    @Inject private Database db;
    @Inject private UserManager um;
    @Inject private CommandManager cm;
    @Inject private Game game;
    @Inject private BanManager bm;

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
        cm.addCommands(this, new AuthCommands(this, game, bm));

        um.addDefaultAttachment(AuthAttachment.class, this);
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

    public Triplet<Long, String, Integer> getFailedLogin(User user)
    {
        return this.failedLogins.get(user.getUniqueId());
    }

    protected void addFailedLogin(User user)
    {
        Triplet<Long, String, Integer> loginFail = this.getFailedLogin(user);
        if (loginFail == null)
        {
            loginFail = new Triplet<>(System.currentTimeMillis(), user.getAddress().getAddress().getHostAddress(), 1);
            this.failedLogins.put(user.getUniqueId(), loginFail);
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
        this.failedLogins.remove(user.getUniqueId());
    }

    public void resetAllPasswords()
    {
        this.db.getDSL().update(TableUser.TABLE_USER).set(TABLE_AUTH.PASSWD, (byte[])null).execute();
        for (User user : um.getLoadedUsers())
        {
            user.getEntity().refresh();
        }
    }

    public AuthPerms perms()
    {
        return perms;
    }

    public AuthConfiguration getConfig()
    {
        return config;
    }
}
