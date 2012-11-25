package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.ConfigurationUpdater;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;
import de.cubeisland.cubeengine.core.config.annotations.Updater;
import de.cubeisland.cubeengine.test.TestConfig2.TestConfig2Updater;
import java.util.Map;

/**
 * This TestConfig is used for testing the updating process of configurations.
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
        @Override
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
