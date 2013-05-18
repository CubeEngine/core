package de.cubeisland.cubeengine.conomy.account;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.ConomyConfiguration;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.AccountStorage;

import gnu.trove.map.hash.THashMap;

public class ConomyManager
{
    private final Conomy module;
    protected final AccountStorage storage;
    private Map<String,BankAccount> bankaccounts;

    protected final CubeLogger logger;
    private final ConomyConfiguration config;

    public ConomyManager(Conomy module)
    {
        this.module = module;
        this.storage = new AccountStorage(module.getCore().getDB());
        this.config = module.getConfig();
        this.bankaccounts = new THashMap<String, BankAccount>();

        this.logger = new CubeLogger("conomy_transactions");
        if (this.module.getConfig().enableLogging)
        {
            try
            {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                CubeFileHandler handler = new CubeFileHandler(LogLevel.ALL,
                                                              new File(this.module.getCore().getFileManager().getLogDir(), "conomy_transactions").toString());
                this.logger.addHandler(handler);
                handler.setFormatter(new Formatter() {
                    @Override
                    public String format(LogRecord record)
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append(dateFormat.format(new Date(record.getMillis())))
                          .append(" ").append(record.getMessage()).append("\n");
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

    public BankAccount getBankAccount(String name, boolean create)
    {
        BankAccount bankAccount = this.bankaccounts.get(name);
        if (bankAccount == null)
        {
            AccountModel model = this.storage.getBankAccount(name);
            if (model == null)
            {
                if (!create) return null;
                model = new AccountModel(null,name,(int) (this.config.defaultBankBalance * this.config.fractionalDigitsFactor()),false);
                this.storage.store(model);
                bankAccount = new BankAccount(this, model);
                this.bankaccounts.put(name,bankAccount);
                this.logger.info("NEW Bank:" + name + " :: " + bankAccount.balance());
            }
            else
            {
                bankAccount = new BankAccount(this, model);
                this.bankaccounts.put(name,bankAccount);
                this.logger.info("LOAD Bank:" + name + " :: " + bankAccount.balance());
            }
        }
        return bankAccount;
    }

    public UserAccount getUserAccount(User user, boolean create)
    {
        UserAccount userAccount = user.attachOrGet(UserAccount.class, module);
        if (!userAccount.isInitialized())
        {
            AccountModel model = this.storage.getUserAccount(user.key);
            if (model == null)
            {
                if (!create) return null;
                model = new AccountModel(user.key,null,(int) (this.config.defaultBalance * this.config.fractionalDigitsFactor()),false);
                this.storage.store(model);
                userAccount.init(this, model);
                this.logger.info("NEW User:" + user.getName() + " :: " + userAccount.balance());
            }
            else
            {
                userAccount.init(this, model);
                this.logger.info("LOAD User:" + user.getName() + " :: " + userAccount.balance());
            }
        }
        return userAccount;
    }

    public UserAccount getUserAccount(String playerName, boolean create)
    {
        User user = this.module.getCore().getUserManager().getExactUser(playerName);
        return this.getUserAccount(user, create);
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

    private Thread thread = null;

    /**
     * Returns false if an action is currently running
     *
     * @param r the runnable tu run
     * @return true if the thread got started
     */
    private boolean startThread(Runnable r)
    {
        if (this.thread == null || !this.thread.isAlive())
        {
            thread = this.module.getCore().getTaskManager().getThreadFactory().newThread(r);
            thread.start();
            return true;
        }
        return false;
    }

    public boolean setAllOnline(final double value)
    {
        final Set<User> onlineUsers = this.module.getCore().getUserManager().getOnlineUsers();
        return this.startThread(new Runnable()
        {
            @Override
            public void run()
            {
                for (User user : onlineUsers)
                {
                    UserAccount userAccount = ConomyManager.this.getUserAccount(user, true);
                    userAccount.set(value);
                }
            }
        });
    }

    public boolean transactionAllOnline(final double value)
    {
        final Set<User> onlineUsers = this.module.getCore().getUserManager().getOnlineUsers();
        return this.startThread(new Runnable()
        {
            @Override
            public void run()
            {
                for (User user : onlineUsers)
                {
                    UserAccount userAccount = ConomyManager.this.getUserAccount(user, true);
                    userAccount.deposit(value);
                }
            }
        });
    }

    public boolean scaleAllOnline(final float factor)
    {
        final Set<User> onlineUsers = this.module.getCore().getUserManager().getOnlineUsers();
        return this.startThread(new Runnable()
        {
            @Override
            public void run()
            {
                for (User user : onlineUsers)
                {
                    UserAccount userAccount = ConomyManager.this.getUserAccount(user, true);
                    userAccount.scale(factor);
                }
            }
        });
    }

    public void setAll(boolean userAcc, boolean bankAcc, double value)
    {
        final long longValue = (long)(value * this.config.fractionalDigitsFactor());
        this.storage.setAll(userAcc, bankAcc, longValue);
        // update all loaded accounts...
        if (userAcc)
        {
            for (User user : this.module.getCore().getUserManager().getOnlineUsers())
            {
                UserAccount userAccount = ConomyManager.this.getUserAccount(user, false);
                if (userAccount != null)
                {
                    userAccount.model.value = longValue;
                }
            }
        }
        if (bankAcc)
        {
            for (BankAccount bankAccount : this.bankaccounts.values())
            {
                bankAccount.model.value = longValue;
            }
        }
    }

    public void scaleAll(boolean userAcc, boolean bankAcc, float factor)
    {
        this.storage.scaleAll(userAcc, bankAcc, factor);
        // update all loaded accounts...
        if (userAcc)
        {
            for (User user : this.module.getCore().getUserManager().getOnlineUsers())
            {
                UserAccount userAccount = ConomyManager.this.getUserAccount(user, false);
                if (userAccount != null)
                {
                    userAccount.model.value *= factor;
                }
            }
        }
        if (bankAcc)
        {
            for (BankAccount bankAccount : this.bankaccounts.values())
            {
                bankAccount.model.value *= factor;
            }
        }
    }

    public void transactionAll(boolean userAcc, boolean bankAcc, double value)
    {
        final long longValue = (long)(value * this.config.fractionalDigitsFactor());
        this.storage.transactAll(userAcc, bankAcc, longValue);
        // update all loaded accounts...
        if (userAcc)
        {
            for (User user : this.module.getCore().getUserManager().getOnlineUsers())
            {
                UserAccount userAccount = ConomyManager.this.getUserAccount(user, false);
                if (userAccount != null)
                {
                    userAccount.model.value += longValue;
                }
            }
        }
        if (bankAcc)
        {
            for (BankAccount bankAccount : this.bankaccounts.values())
            {
                bankAccount.model.value += longValue;
            }
        }
    }

    public boolean transaction(Account from, Account to, double amount, boolean force)
    {
        if (to == null && from == null)
        {
            throw new IllegalStateException("Both accounts are null!");
        }
        if (from != null && !force)
        {
            if (!from.has(amount))
            {
                return false;
            }
        }
        if (to != null && from != null)
        {
            this.logger.info("TRANSACTION " + (from instanceof UserAccount ? "User:" : "Bank:") + from.getName()
                                 + " -> " + (to instanceof UserAccount ? "User:" : "Bank:") + to.getName());
        }
        if (from != null)
        {
            from.withdraw(amount);
        }
        if (to != null)
        {
            to.deposit(amount);
        }
        return true;
    }

    public Collection<AccountModel> getTopAccounts(boolean user, boolean bank, int fromRank, int toRank, boolean showHidden)
    {
        return this.storage.getTopAccounts(user, bank, fromRank, toRank, showHidden);
    }

    public void hideAll(boolean user, boolean bank)
    {
        this.storage.setAllHidden(user, bank, true);
    }

    public void unhideAll(boolean user, boolean bank)
    {
        this.storage.setAllHidden(user, bank, false);
    }

    public boolean deleteUserAccount(User user)
    {
        UserAccount account = this.getUserAccount(user, false);
        if (account == null)
        {
            return false;
        }
        this.storage.delete(account.model);
        user.detach(account.getClass());
        return true;
    }

    public String format(double balance)
    {
        return this.format(Locale.ENGLISH, balance);
    }

    public String format(Locale locale, double balance)
    {
        return String.format(locale, "%." + this.config.fractionalDigits
            + "f "+ this.config.symbol, balance);
    }

    public Double parse(String amountString)
    {
        //private Pattern pattern2 = Pattern.compile("[^a-zA-Z]+");
        //private Pattern pattern1;
        //this.pattern1 = Pattern.compile("^-*[\\d,]+$");
        try
        {
            return Double.parseDouble(amountString);
        }
        catch (NumberFormatException ex)
        {}
        // TODO filter currency Names / Symbols
        return null;
    }

    public long fractionalDigitsFactor()
    {
        return this.config.fractionalDigitsFactor();
    }

    public double getDefaultBalance()
    {
        return this.config.defaultBalance;
    }

    public double getDefaultBankBalance()
    {
        return this.config.defaultBankBalance;
    }

    public double getMinimumBankBalance()
    {
        return this.config.minimumBankBalance;
    }

    public double getMinimumBalance()
    {
        return this.config.minimumBalance;
    }
}
