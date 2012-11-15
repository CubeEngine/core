package de.cubeisland.cubeengine.core.webapi;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Phillip Schichtel
 */
@Codec("yml")
@Revision(1)
public class ApiConfig extends Configuration
{
    @Option("address")
    public final String address = "localhost";
    
    @Option("port")
    public final short port = 6561;
    
    public final String authKey = null; // TODO remove
    
    @Option("max-content-length")
    public final int maxContentLength = 1048576;
    
    @Option("compression.enable")
    public final boolean compression = false;
    
    @Option("compression.level")
    public final int compressionLevel = 9;
    
    @Option("compression.window-bits")
    public final int windowBits = 15;
    
    @Option("compression.memory-level")
    public final int memoryLevel = 9;
    
    @Option("disabled-actions")
    public final Map<String, List<String>> disabledActions = new HashMap<String, List<String>>();
    
    @Option("blacklist.enable")
    public final boolean blacklistEnable = false;
    
    @Option("blacklist.ips")
    public final List<String> blacklist = new ArrayList<String>();
    
    @Option("whitelist.enable")
    public final boolean whitelistEnable = false;
    
    @Option("whitelist.ips")
    public final List<String> whitelist = new ArrayList<String>();
}