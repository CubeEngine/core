package de.cubeisland.cubeengine.vaultcompat;

import java.util.List;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.Account;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class VaultConomyService implements Economy
{
    private final Vaultcompat compat;
    private final Conomy conomy;

    public VaultConomyService(Vaultcompat compat, Conomy conomy)
    {
        this.compat = compat;
        this.conomy = conomy;
    }

    @Override
    public boolean isEnabled()
    {
        return this.conomy.isEnabled();
    }

    @Override
    public String getName()
    {
        return "CubeEngine:Conomy";
    }

    @Override
    public boolean hasBankSupport()
    {
        return true;
    }

    @Override
    public int fractionalDigits()
    {
        return -1;
    }

    @Override
    public String format(double v)
    {
        return this.conomy.getCurrencyManager().getMainCurrency().formatLong(Long.parseLong(Double.toString(v).replace(".", "")));
    }

    @Override
    public String currencyNamePlural()
    {
        return this.conomy.getCurrencyManager().getMainCurrency().getName(); // TODO is this correct
    }

    @Override
    public String currencyNameSingular()
    {
        return this.conomy.getCurrencyManager().getMainCurrency().getName(); // TODO is this correct
    }

    @Override
    public boolean hasAccount(String s)
    {
        return this.conomy.getAccountsManager().bankAccountExists(s);
    }

    @Override
    public boolean hasAccount(String s, String s2)
    {
        return this.hasAccount(s);
    }

    @Override
    public double getBalance(String s)
    {
        Account acc = this.conomy.getAccountsManager().getAccount(s);
        if (acc != null)
        {
            return acc.getBalance() * 1.0;
        }
        return 0;
    }

    @Override
    public double getBalance(String s, String s2)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean has(String s, double v)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean has(String s, String s2, double v)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s2, double v)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s2, double v)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse createBank(String s, String s2)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse deleteBank(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse bankBalance(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse bankHas(String s, double v)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s2)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EconomyResponse isBankMember(String s, String s2)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getBanks()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean createPlayerAccount(String s)
    {
        return this.conomy.getAccountsManager().createNewAccount(s) != null;
    }

    @Override
    public boolean createPlayerAccount(String s, String s2)
    {
        return this.createPlayerAccount(s);
    }
}
