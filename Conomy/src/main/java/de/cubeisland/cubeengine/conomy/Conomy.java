package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.conomy.account.AccountManager;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;
import de.cubeisland.cubeengine.conomy.commands.EcoCommands;
import de.cubeisland.cubeengine.conomy.commands.MoneyCommand;
import de.cubeisland.cubeengine.conomy.config.ConomyConfiguration;
import de.cubeisland.cubeengine.conomy.config.CurrencyConfiguration;
import de.cubeisland.cubeengine.conomy.config.CurrencyConfigurationConverter;
import de.cubeisland.cubeengine.conomy.config.SubCurrencyConfig;
import de.cubeisland.cubeengine.conomy.config.SubCurrencyConverter;
import de.cubeisland.cubeengine.conomy.currency.CurrencyManager;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.io.File;

public class Conomy extends Module
{
    public static boolean debugMode = false;
    protected File dataFolder;
    private ConomyConfiguration config;
    private AccountManager accountsManager;
    private AccountStorage accountsStorage;
    private CurrencyManager currencyManager;

    //TODO Roles support (e.g. allow all user of a role to access a bank)
    public Conomy()
    {
        Convert.registerConverter(SubCurrencyConfig.class, new SubCurrencyConverter());
        Convert.registerConverter(CurrencyConfiguration.class, new CurrencyConfigurationConverter());
    }

    @Override
    public void onEnable()
    {
        this.registerPermissions(ConomyPermissions.values());
        this.currencyManager = new CurrencyManager(this, config);
        this.currencyManager.load();
        this.accountsStorage = new AccountStorage(this.getDatabase());
        this.accountsManager = new AccountManager(this); // Needs cManager / aStorage
        this.registerCommand(new MoneyCommand(this));
        this.registerCommand(new EcoCommands(this));
    }

    public AccountManager getAccountsManager()
    {
        return accountsManager;
    }

    public CurrencyManager getCurrencyManager()
    {
        return currencyManager;
    }

    public AccountStorage getAccountsStorage()
    {
        return accountsStorage;
    }

}
