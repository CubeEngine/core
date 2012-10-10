package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.ConfigurationUpdater;
import de.cubeisland.cubeengine.core.config.annotations.*;
import de.cubeisland.cubeengine.test.TestConfig2.TestConfig2Updater;
import java.util.Map;

/**
 *
 * @author Anselm Brehme
 */
@Codec("yml")
@Revision(2)
@Updater(TestConfig2Updater.class)
public class TestConfig2 extends Configuration
{
    @Option("string2")
    @Comment("string1 on revision 1 will update to string2")
    public String string = "This will update!";

    public static class TestConfig2Updater implements ConfigurationUpdater
    {
        public Map<String, Object> update(Map<String, Object> loadedConfig, int fromRevision)
        {
            if (fromRevision == 1)
            {
                loadedConfig.put("string2", loadedConfig.get("string1"));
            }
            return loadedConfig;
        }
    }
}
