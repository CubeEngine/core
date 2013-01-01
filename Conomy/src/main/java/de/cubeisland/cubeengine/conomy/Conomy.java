package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.conomy.account.AccountsManager;
import de.cubeisland.cubeengine.conomy.account.CurrencyAccountManager;
import de.cubeisland.cubeengine.conomy.config.ConomyConfiguration;
import de.cubeisland.cubeengine.conomy.config.CurrencyConfiguration;
import de.cubeisland.cubeengine.conomy.config.CurrencyConfigurationConverter;
import de.cubeisland.cubeengine.conomy.config.SubCurrencyConfig;
import de.cubeisland.cubeengine.conomy.config.SubCurrencyConverter;
import de.cubeisland.cubeengine.conomy.currency.CurrencyManager;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.io.File;

public class Conomy extends Module
{
    public static boolean debugMode = false;
    protected File dataFolder;
    private static final String PERMISSION_BASE = "cubeengine.conomy";
    private ConomyConfiguration config;
    private AccountsManager accountsManager;
    private CurrencyAccountManager currencyAccountManager;
    private ConomyAPI api;
    private CurrencyManager currencyManager;

    public Conomy()
    {
        Convert.registerConverter(SubCurrencyConfig.class, new SubCurrencyConverter());
        Convert.registerConverter(CurrencyConfiguration.class, new CurrencyConfigurationConverter());
    }

    @Override
    public void onEnable()
    {
        this.currencyManager = new CurrencyManager(this, config);
        currencyManager.load();
        this.accountsManager = new AccountsManager(this);
        this.currencyAccountManager = new CurrencyAccountManager(this);
        this.api = new ConomyAPI(this);
        this.registerListener(new ConomyListener(this));
    }

    public AccountsManager getAccountsManager()
    {
        return accountsManager;
    }

    public CurrencyAccountManager getCurrencyAccountManager()
    {
        return currencyAccountManager;
    }

    public CurrencyManager getCurrencyManager()
    {
        return currencyManager;
    }

    public ConomyAPI getApi()
    {
        return api;
    }
}
