package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.YamlConfiguration;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@DefaultConfig()
public class LogConfiguration  extends YamlConfiguration
{
    @Comment("The maximum of logs that may be logged at once.")
    @Option("logging.batch-size")
    public int loggingBatchSize = 2000;
}
