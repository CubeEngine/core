package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import gnu.trove.map.hash.THashMap;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Anselm Brehme
 */
@Codec("yml")
public class LogConfiguration extends Configuration
{

    public LogConfiguration()
    {
        for (LogAction action : LogAction.values())
        {
            
        }
        
    }
    
    public Map<LogAction,String> configNames = new EnumMap<LogAction, String>(LogAction.class);
    
    @Option(value="configs")
    public Map<String,LogSubConfiguration> configs = new THashMap<String, LogSubConfiguration>();


    public boolean isLogging(LogAction name)
    {
        return false; //TODO
    }
    
    public <T extends LogSubConfiguration> T getConfiguration(LogAction action)
    {
        return (T)this.configs.get(this.configNames.get(action));
    }
}