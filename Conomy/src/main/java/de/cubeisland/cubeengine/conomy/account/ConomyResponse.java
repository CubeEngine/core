package de.cubeisland.cubeengine.conomy.account;

public class ConomyResponse
{
    public final boolean success;
    public final String currencyName;
    public final IAccount account;
    public final Long amount;
    public final Long balance;
    public final String errorMessage;

    public ConomyResponse(boolean success, String currencyName, IAccount account, Long amount, Long balance, String errorMessage)
    {
        this.success = success;
        this.currencyName = currencyName;
        this.account = account;
        this.amount = amount;
        this.balance = balance;
        this.errorMessage = errorMessage;
    }
}
