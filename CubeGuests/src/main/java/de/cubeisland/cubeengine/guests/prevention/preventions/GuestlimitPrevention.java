package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * This prevention limits the number of guests that can join the server.
 */
public class GuestlimitPrevention extends Prevention
{
    private Map<Player, String> kickMessages;
    private int                 minimumPlayers;
    private int                 guestLimit;
    private boolean             kickGuests;
    private Server              server;

    public GuestlimitPrevention(Guests guests)
    {
        super("guestlimit", guests, false);
        this.server = ((BukkitCore)guests.getCore()).getServer();
    }

    @Override
    public void enable()
    {
        super.enable();
        this.kickMessages = new THashMap<Player, String>();

        this.minimumPlayers = getConfig().getInt("minimumPlayers");
        this.guestLimit = getConfig().getInt("guestLimit");
        this.kickGuests = getConfig().getBoolean("kickGuests");
    }

    @Override
    public void disable()
    {
        super.disable();
    }

    @Override
    public String getConfigHeader()
    {
        return super.getConfigHeader() + "\n" +
                "Configuration info:\n" +
                "    minimumPlayer: the number of players on the server to enable the guest limit\n" +
                "    guestLimit: the number of guests that can join the server\n" +
                "    kickGuests: whether to kick a guest from hte full server if a member wants to join\n";
    }

    @Override
    public Configuration getDefaultConfig()
    {
        Configuration defaultConfig = super.getDefaultConfig();

        defaultConfig.set("minimumPlayers", Math.round(this.server.getMaxPlayers() * .7));
        defaultConfig.set("guestLimit", Math.round(this.server.getMaxPlayers() * .3));
        defaultConfig.set("kickGuests", false);

        return defaultConfig;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerLogin(PlayerLoginEvent event)
    {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL)
        {
            event.allow();
            this.kickMessages.put(event.getPlayer(), event.getKickMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerJoin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        final List<User> guests = getGuests();
        final int onlinePlayers = this.server.getOnlinePlayers().length - 1;
        final boolean serverIsFull = onlinePlayers >= this.server.getMaxPlayers();

        // guest ?
        if (!can(player))
        {
            final boolean tooManyGuests = guests.size() >= this.guestLimit && onlinePlayers > minimumPlayers;

            // server full or too many guests?
            if (serverIsFull || tooManyGuests)
            {
                player.kickPlayer(this.kickMessages.remove(player));
            }
        }
        else
        {
            final boolean hasKickableGuests = guests.size() > 0 && this.kickGuests;

            // server full and can kick a guest?
            if (serverIsFull)
            {
                // can kick a guest?
                if (hasKickableGuests)
                {
                    guests.get(0).kickPlayer(getMessage());
                }
                else
                {
                    player.kickPlayer(this.kickMessages.remove(player));
                }
            }
        }
    }

    private List<User> getGuests()
    {
        List<User> guests = new ArrayList<User>();

        for (User user : getModule().getCore().getUserManager().getOnlineUsers())
        {
            if (!can(user))
            {
                guests.add(user);
            }
        }

        return guests;
    }
}
