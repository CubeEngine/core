package de.cubeisland.cubeengine.chat;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
@DefaultConfig
public class ChatConfig extends Configuration
{
    @Option("format")
    @Comment("There at least the following variables available:\n- {NAME} -> player name\n- {DISPLAY_NAME} -> display name\n- {WORLD} -> the world the player is in\n- {MESSAGE} -> the message\n\nUsual color/format codes are also supported: &1, ... &f, ... &r")
    public String format = "{NAME}: {MESSAGE}";

    @Option("allow-colors")
    @Comment("This also counts for the format string!")
    public boolean parseColors = true;
}
