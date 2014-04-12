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
package de.cubeisland.engine.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.server.v1_7_R3.DedicatedPlayerList;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.PacketPlayOutKeepAlive;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;

import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.test.tests.AsyncCommandTest;
import de.cubeisland.engine.test.tests.ClearChatTest;
import de.cubeisland.engine.test.tests.CommandArgsTest;
import de.cubeisland.engine.test.tests.DatabaseTest;
import de.cubeisland.engine.test.tests.MatchTest;
import de.cubeisland.engine.test.tests.ModuleTest;
import de.cubeisland.engine.test.tests.UtilTest;
import de.cubeisland.engine.test.tests.l18n.l18nTest;
import de.cubeisland.engine.test.tests.listener.ListenerTest;
import de.cubeisland.engine.test.tests.worldgenerator.WorldGeneratorTest;

public class Test extends Module
{
    private Map<String, de.cubeisland.engine.test.tests.Test> tests;
    private Timer timer;

    public Test()
    {
        this.tests = new HashMap<>();

        this.registerTest("l18n test", new l18nTest(this));
        this.registerTest("Listener test", new ListenerTest(this));
        this.registerTest("World generator test", new WorldGeneratorTest(this));
        this.registerTest("Async command test", new AsyncCommandTest(this));
        this.registerTest("Clear chat test", new ClearChatTest(this));
        this.registerTest("Command args test", new CommandArgsTest(this));
        this.registerTest("Database test", new DatabaseTest(this));
        this.registerTest("Match test", new MatchTest(this));
        this.registerTest("Module test", new ModuleTest(this));
        this.registerTest("Util test", new UtilTest(this));
    }

    @Override
    public void onLoad()
    {
        getLog().info("{} tests to run.", tests.size());
        for (Entry<String, de.cubeisland.engine.test.tests.Test> test : this.tests.entrySet())
        {
            try
            {
                test.getValue().onLoad();
            }
            catch (Exception ex)
            {
                test.getValue().setSuccess(false);
                getLog().warn(ex, "{} failed in onLoad", test.getKey());
            }
        }
    }

    @Override
    public void onEnable()
    {
        for (Entry<String, de.cubeisland.engine.test.tests.Test> test : this.tests.entrySet())
        {
            try
            {
                test.getValue().onEnable();
            }
            catch (Exception ex)
            {
                test.getValue().setSuccess(false);
                getLog().warn(ex, "{} failed in onEnable", test.getKey());
            }
        }

        this.timer = new Timer("keepAliveTimer");
        this.timer.schedule(new KeepAliveTimer(), 2 * 1000, 2 * 1000);
    }

    @Override
    public void onStartupFinished()
    {
        int failedTests = 0;
        for (Entry<String, de.cubeisland.engine.test.tests.Test> test : this.tests.entrySet())
        {
            try
            {
                test.getValue().onStartupFinished();
            }
            catch (Exception ex)
            {
                test.getValue().setSuccess(false);
                getLog().warn(ex, "{} failed in onStartupFinished", test.getKey());
            }
            if (!test.getValue().isSuccessSet() || !test.getValue().wasSuccess())
            {
                failedTests++;
            }
        }
        getLog().info("{} out of {} tests succeeded", this.tests.size()-failedTests, this.tests.size());
    }

    @Override
    public void onDisable()
    {
        for (Entry<String, de.cubeisland.engine.test.tests.Test> test : this.tests.entrySet())
        {
            try
            {
                test.getValue().onDisable();
            }
            catch (Exception ex)
            {
                getLog().warn(ex, "{} failed in onDisable", test.getKey());
            }
        }

        this.timer.cancel();
        this.timer = null;
    }

    public void registerTest(String name, de.cubeisland.engine.test.tests.Test test)
    {
        this.tests.put(name, test);
    }

    private class KeepAliveTimer extends TimerTask
    {
        private final DedicatedPlayerList mojangServer;
        private final Random random;

        public KeepAliveTimer()
        {
            this.mojangServer = ((CraftServer)((BukkitCore)getCore()).getServer()).getHandle();
            this.random = new Random();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run()
        {
            for (EntityPlayer player : (List<EntityPlayer>)this.mojangServer.players)
            {
                player.playerConnection.sendPacket(new PacketPlayOutKeepAlive(random.nextInt()));
            }
        }
    }
}
