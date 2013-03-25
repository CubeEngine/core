package de.cubeisland.cubeengine.conomy.account;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.conomy.currency.CurrencyManager;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public class AccountManager
{
    private final AccountStorage accountStorage;
    private final CurrencyManager currencyManager;
    private final Conomy module;
    private THashMap<String, THashMap<Currency, Account>> bankaccounts = new THashMap<String, THashMap<Currency, Account>>();
    private TLongObjectHashMap<THashMap<Currency, Account>> useraccounts = new TLongObjectHashMap<THashMap<Currency, Account>>();
    private Logger transactionLogger;

    public AccountManager(Conomy module)
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.module = module;
        this.currencyManager = module.getCurrencyManager();
        this.accountStorage = module.getAccountsStorage();
        this.transactionLogger = new CubeLogger("conomy_transactions");
        if (this.module.getConfig().enableLogging)
            try
            {
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

    public AccountStorage getStorage()
    {
        return this.accountStorage;
    }

    /**
     * Returns the main currency.
     *
     * @return the main currency
     */
    public Currency getMainCurrency()
    {
        return this.module.getCurrencyManager().getMainCurrency();
    }

    /**
     * Returns the account of given user for the main currency
     *
     * @param user the user
     * @return the user's account
     */
    public Account getAccount(User user)
    {
        return this.getAccount(user, this.module.getCurrencyManager().getMainCurrency());
    }

    /**
     * Returns the account of given user for given currency
     *
     * @param user the user
     * @param currency the currency
     * @return the user's account
     */
    public Account getAccount(User user, Currency currency)
    {
        if (user == null)
            return null;
        this.userAccountExists(user, currency); //loads accounts if not yet loaded
        return this.useraccounts.get(user.key).get(currency);
    }

    /**
     * Returns the bank-account with given name for the main currency
     *
     * @param name the banks name
     * @return the bank's account
     */
    public Account getAccount(String name)
    {
        return this.getAccount(name, this.getMainCurrency());
    }

    /**
     * Returns the bank-account with given name for given currency
     *
     * @param name the banks name
     * @param currency the currency
     * @return the bank's account
     */
    public Account getAccount(String name, Currency currency)
    {
        this.bankAccountExists(name, currency); // TODO this is confusing
        return this.bankaccounts.get(name).get(currency);

    }

    /**
     * Returns if given user has an account in the main currency.
     *
     * @param user the user
     * @return true if the user has an account
     */
    public boolean userAccountExists(User user)
    {
        return this.userAccountExists(user, this.getMainCurrency());
    }

    /**
     * Returns if given user has an account in given currency.
     *
     * @param user the user
     * @param currency the currency
     * @return true if the user has an account
     */
    public boolean userAccountExists(User user, Currency currency)
    {
        if (this.useraccounts.containsKey(user.key))
        {
            return this.useraccounts.get(user.key).containsKey(currency);
        }
        else
        {
            this.loadUserAccounts(user);
            return this.useraccounts.get(user.key).containsKey(currency);
        }
    }

    /**
     * Returns if a bank account with given name exists for the main currency.
     *
     * @param name the bank's name
     * @return true if the bank-account exists
     */
    public boolean bankAccountExists(String name)
    {
        return this.bankAccountExists(name, this.getMainCurrency());
    }

    /**
     * Returns if a bank account with given name exists for the given currency.
     *
     * @param name the bank's name
     * @param currency the currency
     * @return true if the bank-account exists
     */
    public boolean bankAccountExists(String name, Currency currency)
    {
        if (this.bankaccounts.containsKey(name))
        {
            return this.bankaccounts.get(name).containsKey(currency);
        }
        else
        {
            this.loadBankAccounts(name);
            return this.bankaccounts.get(name).containsKey(currency);
        }
    }

    /**
     * Creates a main-currency account for given user.
     *
     * @param user the user
     * @return the new account
     */
    public Account createNewAccount(User user)
    {
        //TODO log creation
        return this.createNewAccount(user, this.getMainCurrency());
    }

    /**
     * Creates an account in given currency for given user.
     *
     * @param user the user
     * @param currency the currency
     * @return the new account
     */
    public Account createNewAccount(User user, Currency currency)
    {
        if (this.userAccountExists(user, currency))
        {
            return null;
        }
        return this.createNewAccountNoCheck(user, currency);
    }

    /**
     * Creates a new account for the user in given currency without checking for
     * eventually existing account.
     *
     * @param user
     * @param currency
     * @return
     */
    private Account createNewAccountNoCheck(User user, Currency currency)
    {
        AccountModel model = new AccountModel(user.key, null, currency.getName(), currency.getDefaultBalance(), false);
        this.accountStorage.store(model);
        Account account = new Account(this, currency, model);
        this.useraccounts.get(user.key).put(currency, account);
        if (this.module.getConfig().enableLogging)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("ACCOUNT CREATED: ").append(account);
            this.transactionLogger.info(sb.toString());
        }
        return account;
    }

    /**
     * Creates a main-currency bank-account with given name.
     *
     * @param name the bank-name
     * @return the new account
     */
    public Account createNewAccount(String name)
    {
        return this.createNewAccount(name, this.getMainCurrency());
    }

    /**
     * Creates a bank-account in given currency with given name.
     *
     * @param name the bank-name
     * @param currency the currency
     * @return the new account
     */
    public Account createNewAccount(String name, Currency currency)
    {
        if (this.bankAccountExists(name, currency))
        {
            return null;
        }
        return this.createNewAccountNoCheck(name, currency);
    }

    private Account createNewAccountNoCheck(String name, Currency currency)
    {
        AccountModel model = new AccountModel(null, name, currency.getName(), currency.getDefaultBalance(), false);
        this.accountStorage.store(model);
        Account account = new Account(this, currency, model);
        this.bankaccounts.get(name).put(currency, account);
        if (this.module.getConfig().enableLogging)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("ACCOUNT CREATED: ").append(account);
            this.transactionLogger.info(sb.toString());
        }
        return account;
    }

    private void loadUserAccounts(User user)
    {
        Collection<AccountModel> models = this.accountStorage.loadAccounts(user.key);
        this.useraccounts.put(user.key, new THashMap<Currency, Account>());
        for (AccountModel model : models)
        {
            Currency currency = this.currencyManager.getCurrencyByName(model.currencyName);
            Account account = new Account(this, currency, model);
            this.useraccounts.get(user.key).put(currency, account);
        }
        if (user.hasPlayedBefore() || user.isOnline()) // only if user has played on the server create missing accounts
        {
            for (Currency currency : this.currencyManager.getAllCurrencies())
            {
                if (!this.useraccounts.get(user.key).containsKey(currency))
                {
                    this.createNewAccountNoCheck(user, currency); // No check i just did check in db
                }
            }
        }
    }

    private void loadBankAccounts(String name)
    {
        Collection<AccountModel> models = this.accountStorage.loadAccounts(name);
        this.bankaccounts.put(name, new THashMap<Currency, Account>());
        for (AccountModel model : models)
        {
            Currency currency = this.currencyManager.getCurrencyByName(model.currencyName);
            Account account = new Account(this, currency, model);
            this.bankaccounts.get(name).put(currency, account);
        }
    }

    /**
     * Returns the accounts of given user
     *
     * @param user the user
     * @return the account or null if
     */
    public Collection<Account> getAccounts(User user)
    {
        this.loadUserAccounts(user);
        return this.useraccounts.get(user.key).values();
    }

    /**
     * Transfers money from the source-account to the target-amount.
     *
     * @param source the source
     * @param target the target
     * @param amount the amount in target-currency
     * @return true if the transaction was successful
     * @throws IllegalArgumentException when currencies are not convertible
     */
    public boolean transaction(Account source, Account target, Long amount) throws IllegalArgumentException
    {
        if (target == null)
        {
            return this.transaction(target, source, -amount);
        }
        if (!(source == null || target == null))
            if (!source.getCurrency().canConvert(target.getCurrency()))
            {
                return false; // currencies are not convertible
            }
        target.transaction(source, amount);
        if (this.module.getConfig().enableLogging)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("TRANSACTION: ").append(source).append("->").append(target).append(" AMOUNT:").append(amount);
            this.transactionLogger.info(sb.toString());
        }
        return true;
    }

    /**
     * Gives money to all user-accounts
     *
     * @param currency the currency
     * @param amount the amount
     * @param online set to true if only online users
     */
    public void transactAll(Currency currency, long amount, boolean online)
    {
        if (online)
        {
            for (User user : this.module.getCore().getUserManager().getOnlineUsers())
            {
                this.userAccountExists(user);
                Account acc = this.useraccounts.get(user.key).get(currency);
                if (acc != null)
                {
                    acc.transaction(null, amount);
                }
            }
        }
        else
        {
            this.accountStorage.transactAll(currency, amount);
            this.useraccounts = new TLongObjectHashMap<THashMap<Currency, Account>>();
        }
    }

    /**
     * Sets all user-account
     *
     * @param currency the currency
     * @param amount the amount
     * @param online set to true if only online users
     */
    public void setAll(Currency currency, long amount, boolean online)
    {
        if (online)
        {
            for (User user : this.module.getCore().getUserManager().getOnlineUsers())
            {
                this.userAccountExists(user);
                Account acc = this.useraccounts.get(user.key).get(currency);
                if (acc != null)
                {
                    acc.set(amount);
                }
            }
        }
        else
        {
            this.accountStorage.setAll(currency, amount);
            this.useraccounts = new TLongObjectHashMap<THashMap<Currency, Account>>();
        }
    }
}
