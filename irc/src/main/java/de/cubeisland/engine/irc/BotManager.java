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
package de.cubeisland.engine.irc;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.util.Cleanable;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

public class BotManager implements Cleanable
{
    private final IrcConfig config;
    private final SocketFactory socketFactory;
    private final PircBotX mainBot;
    private final ConcurrentMap<String, PircBotX> players;
    private final BotListener listener;

    public BotManager(IrcConfig config)
    {
        this.config = config;
        this.mainBot = new PircBotX();
        this.mainBot.setName(config.botName);
        this.listener = new BotListener();
        this.mainBot.getListenerManager().addListener(this.listener);
        if (config.ssl)
        {
            if (config.trustAllCerts)
            {
                this.socketFactory = new UtilSSLSocketFactory().trustAllCertificates();
            }
            else
            {
                this.socketFactory = SSLSocketFactory.getDefault();
            }
        }
        else
        {
            this.socketFactory = SocketFactory.getDefault();
        }
        this.players = config.botPerUser ? new ConcurrentHashMap<String, PircBotX>() : null;
    }

    public void addPlayer(Player player)
    {
        if (this.players != null)
        {
            PircBotX bot = new PircBotX();
            bot.setName(player.getName());
            bot.getListenerManager().addListener(this.listener);
            this.players.put(player.getName(), bot);
        }
    }

    public void removePlayer(Player player)
    {
        if (this.players != null)
        {
            PircBotX bot = this.players.remove(player.getName());
            if (bot != null)
            {
                bot.disconnect();
            }
        }
    }

    private boolean connect(PircBotX bot)
    {
        try
        {
            bot.connect(this.config.host, this.config.port, this.config.password, this.socketFactory);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public void connect()
    {
        this.connect(this.mainBot);
        if (this.players != null)
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                this.addPlayer(player);
            }
        }
    }

    public void shutdown()
    {
        this.mainBot.shutdown();
        if (this.players != null)
        {
            Iterator<Map.Entry<String, PircBotX>> iter = this.players.entrySet().iterator();
            while (iter.hasNext())
            {
                iter.next().getValue().shutdown();
                iter.remove();
            }
        }
    }

    public void clean()
    {
        this.shutdown();
        if (this.players != null)
        {
            this.players.clear();
        }
    }

    public void sendMessage(Player player, String message)
    {
        PircBotX bot = null;
        if (this.players != null)
        {
            bot = this.players.get(player.getName());
        }
        if (bot == null)
        {
            bot = this.mainBot;
            message = player.getName() + ": " + message;
        }
        for (Channel chan : bot.getChannels())
        {
            bot.sendMessage(chan, message);
        }
    }

    private final class BotListener extends ListenerAdapter
    {
        @Override
        public void onPrivateMessage(PrivateMessageEvent event) throws Exception
        {
            Player player = Bukkit.getPlayer(event.getBot().getName());
            if (player != null)
            {
                player.sendMessage("[Irc] " + event.getUser().getNick() + ": " + event.getMessage());
            }
        }

        @Override
        public void onMessage(MessageEvent event) throws Exception
        {
            Bukkit.broadcastMessage("[Irc] " + event.getMessage());
        }
    }
}
