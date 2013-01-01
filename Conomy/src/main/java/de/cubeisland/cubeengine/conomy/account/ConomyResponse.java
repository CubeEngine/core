package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.currency.Currency;

public class ConomyResponse
{
    public final boolean success;
    public final Currency currency;
    public final IAccount account;
    public final Long amount;
    public final Long balance;
    public final String errorMessage;

    public ConomyResponse(boolean success, Currency currency, IAccount account, Long amount, Long balance, String errorMessage)
    {
        this.success = success;
        this.currency = currency;
        this.account = account;
        this.amount = amount;
        this.balance = balance;
        this.errorMessage = errorMessage;
    }
}
