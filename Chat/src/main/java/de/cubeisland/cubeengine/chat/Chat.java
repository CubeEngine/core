package de.cubeisland.cubeengine.chat;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.roles.Roles;

public class Chat extends Module implements Listener
{
    private static final String DEFAULT_FORMAT = new AsyncPlayerChatEvent(true, null, null, null).getFormat();
    private ChatConfig config;
    private Roles roles;
    private String format;
    private ChatPerm perm;

    @Override
    public void onEnable()
    {
        this.perm = new ChatPerm(this);
        this.getCore().getEventManager().registerListener(this, this);
        this.format = this.config.format;
        if (this.config.parseColors)
        {
            this.format = ChatFormat.parseFormats(this.format);
        }
    }

    @Override
    public void onDisable()
    {
        this.perm.cleanup();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlayerChat(AsyncPlayerChatEvent event)
    {
        // don't overwrite other plugins
        if (!DEFAULT_FORMAT.equals(event.getFormat()))
        {
            return;
        }

        Player player = event.getPlayer();
        String format = this.format;

        if (config.parseColors && ChatPerm.COLOR.isAuthorized(player))
        {
            event.setMessage(ChatFormat.parseFormats(event.getMessage()));
        }

        ChatFormatEvent formatEvent = new ChatFormatEvent(player, event.getMessage(), format, event.isAsynchronous());

        format = format.replace("{NAME}", player.getName());
        // set the placeholder instead of the actual value to allow other plugins to change the value
        format = format.replace("{DISPLAY_NAME}", "%1$s");
        format = format.replace("{WORLD}", player.getWorld().getName());
        // set the placeholder instead of the actual value to allow other plugins to change the value
        format = format.replace("{MESSAGE}", "%2$s");

        if (roles != null)
        {
            User user = this.getCore().getUserManager().getExactUser(player);
            if (format.contains("{ROLE.PREFIX}"))
            {
                String prefix = roles.getApi().getMetaData(user, player.getWorld(), "prefix");
                format = format.replace("{ROLE.PREFIX}", prefix == null ? "" : ChatFormat.parseFormats(prefix));
            }
            if (format.contains("{ROLE.SUFFIX}"))
            {
                String suffix = roles.getApi().getMetaData(user, player.getWorld(), "suffix");
                format = format.replace("{ROLE.SUFFIX}", suffix == null ? "" : ChatFormat.parseFormats(suffix));
            }
        }

        this.getCore().getEventManager().fireEvent(formatEvent);

        for (Map.Entry<String, String> entry : formatEvent.variables.entrySet())
        {
            format = format.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        event.setFormat(format);
    }
}
