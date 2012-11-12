package de.cubeisland.cubeengine.core.webapi;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class ApiConfiguration
{
    public final short port;
    public final String authKey;
    public final int maxContentLength;
    public final Map<String, Collection<String>> disabledActions;
    public final boolean blacklistEnabled;
    public final List<String> blacklist;
    public final boolean whitelistEnabled;
    public final List<String> whitelist;

    public ApiConfiguration(Configuration config)
    {
        this.port = (short)config.getInt("Network.port");
        this.authKey = config.getString("Network.authKey");
        this.maxContentLength = config.getInt("Network.maxContentLength");

        this.whitelistEnabled = config.getBoolean("Whitelist.enabled", this.whitelistEnabled);
        this.whitelist = config.getStringList("Whitelist.IPs");

        this.blacklistEnabled = config.getBoolean("Blacklist.enabled");
        this.blacklist = config.getStringList("Blacklist.IPs");

        ConfigurationSection map = config.getConfigurationSection("DisabledActions");
        this.disabledActions = new HashMap<String, Collection<String>>();
        if (map != null)
        {
            for (Map.Entry<String, Object> entry : map.getValues(false).entrySet())
            {
                if (entry.getValue() instanceof List)
                {
                    List<String> list = (List<String>)entry.getValue();
                    disabledActions.put(entry.getKey(), list);
                }
            }
        }
    }
}