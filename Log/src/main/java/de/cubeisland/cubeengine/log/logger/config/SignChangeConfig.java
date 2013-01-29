package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.log.LoggerConfig;

public class SignChangeConfig extends LoggerConfig
{
    public SignChangeConfig()
    {
     super(false);
    }

    @Override
    public String getName()
    {
        return "sign-changes";
    }
}