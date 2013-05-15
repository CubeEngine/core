package de.cubeisland.cubeengine.conomy.account;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.logger.LogLevel;
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
import de.cubeisland.cubeengine.conomy.Currency;

import gnu.trove.map.hash.THashMap;

public class AccountManager
{
    private final Conomy module;
    protected final AccountStorage storage;
    private Map<String,BankAccount> bankaccounts;

    /**
     * The currency defined in the configuration
     */
    private Currency currency;

    private final CubeLogger transactionLogger;

    public AccountManager(Conomy module)
    {
        this.module = module;
        this.storage = new AccountStorage(module.getCore().getDB());
        this.currency = new Currency(this, module.getConfig());
        this.bankaccounts = new THashMap<String, BankAccount>();

        this.transactionLogger = new CubeLogger("conomy_transactions");
        if (this.module.getConfig().enableLogging)
        {
            try
            {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                CubeFileHandler handler = new CubeFileHandler(LogLevel.ALL,
                                                              new File(this.module.getCore().getFileManager().getLogDir(), "conomy_transactions").toString());
                this.transactionLogger.addHandler(handler);
                handler.setFormatter(new Formatter() {
                    @Override
                    public String format(LogRecord record)
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append(dateFormat.format(new Date(record.getMillis())))
                          .append(" ").append(record.getMessage());
                        return sb.toString();
                    }
                });
            }
            catch (Exception ex)
            {
                throw new IllegalStateException("Could not create handler for transaction-logger", ex);
            }
        }
    }

    public Account getBankAccount(String name, boolean create)
    {
        BankAccount bankAccount = this.bankaccounts.get(name);
        if (bankAccount == null)
        {
            AccountModel model = this.storage.getBankAccount(name);
            if (model == null)
            {
                if (!create) return null;
                model = new AccountModel(null,name,(int) (this.currency.getDefaultBankBalance() * this.currency.fractionalDigitsFactor()),false);
                this.storage.store(model);
                // TODO log Acc-creation
            }
            switch (currency.getType())
            {
                case NORMAL:
                    bankAccount = new NormalBankAccount(this, currency,model);
                    break;
                case EXP:
                    bankAccount = new ExpBankAccount(this, currency,model);
                    break;
                case ITEM:
                    bankAccount = new ItemBankAccount(this, currency,model);
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
            // TODO EXP and ITEM do not have database backend!!!
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
            AccountModel model = this.storage.getUserAccount(user.key);
            if (model == null)
            {
                if (!create) return null;
                model = new AccountModel(user.key,null,(int) (this.currency.getDefaultBalance() * this.currency.fractionalDigitsFactor()),false);
                this.storage.store(model);
                // TODO log Acc-creation
            }
            userAccount.init(this, currency, model);
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

    public void setAllOnline(double value)
    {
        // TODO
    }

    public void transactionAllOnline(double value)
    {
        // TODO
    }

    public void scaleAllOnline(float factor)
    {
        // TODO
    }

    public void setAll(boolean userAcc, boolean bankAcc, double value)
    {
        // TODO
    }

    public void scaleAll(boolean userAcc, boolean bankAcc, float factor)
    {
        // TODO
    }

    public void transactionAll(boolean userAcc, boolean bankAcc, double value)
    {
        // TODO
    }

    public boolean transaction(Account from, Account to, double amount, boolean force)
    {
        if (to == null && from == null)
        {
            throw new IllegalStateException("Both accounts are null!");
        }
        if (from == null || to == null || from.getCurrencyType().equals(to.getCurrencyType()))
        {
            if (from != null && !force)
            {
                if (!from.has(amount))
                {
                    return false;
                }
            }
            if (from != null)
            {
                from.withdraw(amount);
            }
            if (to != null)
            {
                to.deposit(amount);
            }
        }
        return true;
    }

    public Currency getCurrency()
    {
        return currency;
    }

    public Collection<AccountModel> getTopUserAccounts(int fromRank, int toRank, boolean showHidden)
    {
        return this.storage.getTopAccounts(fromRank, toRank, showHidden);
    }
}
