package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.account.bank.BankAccount;
import de.cubeisland.cubeengine.conomy.account.user.UserAccount;
import de.cubeisland.cubeengine.core.user.User;
import java.util.HashMap;

public class AccountManager
{
    HashMap<String, BankAccount> banks = new HashMap<String, BankAccount>();
    HashMap<User, UserAccount> useraccs = new HashMap<User, UserAccount>();

    public BankAccount createBank(String name)
    {
        BankAccount newacc = new BankAccount(name);
        this.registerBank(newacc);
        return newacc;
    }

    public BankAccount createBank(String name, double start)
    {
        BankAccount newacc = new BankAccount(name, start);
        this.registerBank(newacc);
        return newacc;
    }

    public void registerBank(BankAccount bank)
    {
        this.banks.put(bank.getName(), bank);
    }

    public UserAccount createUserAcc(User user)
    {
        UserAccount newacc = new UserAccount(user);
        this.registerUserAcc(newacc);
        return newacc;
    }

    public UserAccount createUserAcc(User user, double start)
    {
        UserAccount newacc = new UserAccount(user, start);
        this.registerUserAcc(newacc);
        return newacc;
    }

    public void registerUserAcc(UserAccount acc)
    {
        this.useraccs.put(acc.getUser(), acc);
    }

    public UserAccount getUserAcc(User user)
    {
        return this.useraccs.get(user);
    }

    public BankAccount getBank(String name)
    {
        return this.banks.get(name);
    }
}
