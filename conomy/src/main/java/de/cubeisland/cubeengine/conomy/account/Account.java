package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.conomy.currency.Currency.CurrencyType;

public interface Account
{
    public String getName();

    // TODO perhaps Object that contains detailed informations like Vaults ConomyResponse???
    boolean transaction(Account from, Account to, double amount, boolean force);

    void deposit(double amount);

    void withdraw(double amount);

    void set(double amount);

    void scale(float factor);

    boolean has(double amount);

    CurrencyType getCurrencyType();

    Currency getCurrency();
}
