package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.conomy.config.ConomyConfiguration;
import de.cubeisland.cubeengine.conomy.config.CurrencyConfiguration;
import de.cubeisland.cubeengine.conomy.config.CurrencyConfigurationConverter;
import de.cubeisland.cubeengine.conomy.config.SubCurrencyConfig;
import de.cubeisland.cubeengine.conomy.config.SubCurrencyConverter;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.io.File;

public class Conomy extends Module
{
    public static boolean debugMode = false;
    protected File dataFolder;
    private static final String PERMISSION_BASE = "cubeengine.conomy";
    private ConomyConfiguration config;

    public Conomy()
    {
        Convert.registerConverter(SubCurrencyConfig.class, new SubCurrencyConverter());
        Convert.registerConverter(CurrencyConfiguration.class, new CurrencyConfigurationConverter());
    }

    @Override
    public void onEnable()
    {
    }

    @Override
    public void onDisable()
    {
    }
}
