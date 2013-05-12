package de.cubeisland.cubeengine.conomy.account;

import java.util.Map;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.exp.ExpBankAccount;
import de.cubeisland.cubeengine.conomy.account.exp.ExpUserAccount;
import de.cubeisland.cubeengine.conomy.account.item.ItemBankAccount;
import de.cubeisland.cubeengine.conomy.account.item.ItemUserAccount;
import de.cubeisland.cubeengine.conomy.account.normal.NormalBankAccount;
import de.cubeisland.cubeengine.conomy.account.normal.NormalUserAccount;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.conomy.currency.CurrencyManager;

public class AccountManager
{
    private final Conomy module;
    private final AccountStorage accountStorage;
    private final CurrencyManager currencyManager;

    private Map<String,BankAccount> bankaccounts;

    /**
     * The currency defined in the configuration
     */
    private Currency currency;

    public AccountManager(Conomy module)
    {
        this.module = module;
        this.accountStorage = new AccountStorage(module.getCore().getDB());
        this.currencyManager = new CurrencyManager(module,module.getConfig());
    }

    public Account getBankAccount(String name, boolean create)
    {
        BankAccount bankAccount = this.bankaccounts.get(name);
        if (bankAccount == null)
        {
            AccountModel model = this.accountStorage.getBankAccount(name);
            if (model == null)
            {
                if (!create) return null;
                new AccountModel(null,name,this.currency.getType().name(),this.currency.getDefaultBankBalance(),false);
                // TODO create new BankAccount / Model
            }
            switch (currency.getType())
            {
            case NORMAL:
                bankAccount = new NormalBankAccount(name,currency,model,accountStorage);
                break;
            case EXP:
                bankAccount = new ExpBankAccount(name,currency,model,accountStorage);
                break;
            case ITEM:
                bankAccount = new ItemBankAccount(name,currency,model,accountStorage);
            }
            this.bankaccounts.put(name,bankAccount);
        }
        return bankAccount;
    }

    public Account getUserAccount(String playerName, boolean create)
    {
        User user = this.module.getCore().getUserManager().getExactUser(playerName);
        UserAccount userAccount = null;
        switch (currency.getType())
        {
            case NORMAL:
                userAccount = user.attachOrGet(NormalUserAccount.class, module);
                break;
            case EXP:
                userAccount = user.attachOrGet(ExpUserAccount.class, module);
                break;
            case ITEM:
                userAccount = user.attachOrGet(ItemUserAccount.class, module);
        }
        if (!userAccount.isInitialized())
        {
            AccountModel model = this.accountStorage.getUserAccount(user.key);
            if (model == null)
            {
                if (!create) return null;
                // TODO create new AccountModul + store
            }
            userAccount.init(currency,model);
        }
        return userAccount;
    }

    public boolean bankAccountExists(String name)
    {
        if (this.bankaccounts.containsKey(name))
        {
            return true;
        }
        Account acc = this.getBankAccount(name,false);
        return acc != null;
    }

    public boolean userAccountExists(String name)
    {
        Account acc = this.getUserAccount(name,false);
        return acc != null;
    }
}
