package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.prevention.Prevention;
import de.cubeisland.cubeengine.guests.Guests;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Prevents users from writing with too many capital letters
 *
 * @author Phillip Schichtel
 */
public class CapsPrevention extends Prevention
{
    private double maxCapsRatio;
    
    public CapsPrevention(Guests guests)
    {
        super("caps", guests);
        this.maxCapsRatio = 0.80;
    }

    @Override
    public String getConfigHeader()
    {
        return super.getConfigHeader() +
                "Configuration info:\n" +
                "    max-caps-ratio: the rate of capital letters compared to the length of the message in percent (0 - 100)\n";
    }

    @Override
    public void enable()
    {
        super.enable();
        this.maxCapsRatio = (double)Math.min(100, Math.abs(getConfig().getInt("max-caps-ratio"))) / 100D;
    }

    @Override
    public Configuration getDefaultConfig()
    {
        Configuration config = super.getDefaultConfig();
        
        config.set("max-caps-ratio", (int)(this.maxCapsRatio * 100));
        
        return config;
    }
    
    
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event)
    {
        if (!can(event.getPlayer()))
        {
            String message = event.getMessage();
            final int length = message.length();
            
            int caps = 0;
            for (int i = 0; i < message.length(); ++i)
            {
                if (Character.isUpperCase(message.charAt(i)))
                {
                    ++caps;
                }
            }
            
            if ((double)caps / (double)length > this.maxCapsRatio)
            {
                prevent(event, event.getPlayer());
            }
        }
    }
}
