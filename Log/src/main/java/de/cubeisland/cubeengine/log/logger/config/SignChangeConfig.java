package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.log.SubLogConfig;

public class SignChangeConfig extends SubLogConfig
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