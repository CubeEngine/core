/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.conomy.account;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.avaje.ebean.EbeanServer;
import de.cubeisland.engine.conomy.Conomy;
import de.cubeisland.engine.conomy.ConomyConfiguration;
import de.cubeisland.engine.conomy.account.storage.AccountModel;
import de.cubeisland.engine.conomy.account.storage.BankAccessStorage;
import de.cubeisland.engine.core.service.Economy;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabase;
import de.cubeisland.engine.core.user.User;
import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConomyManager
{
    protected final Conomy module;
    protected BankAccessStorage bankAccessStorage; // TODO remove
    protected final EbeanServer ebean;

    private Map<String,BankAccount> bankaccounts;
    private Map<Long,BankAccount> bankaccountsID;

    protected final Logger logger;
    protected final ConomyConfiguration config;
    private Economy conomyInterface;

    public ConomyManager(Conomy module)
    {
        this.module = module;
        this.ebean = module.getCore().getDB().getEbeanServer();

        //this.bankAccessStorage = new BankAccessStorage(module.getCore().getDB()); // TODO remove

        this.config = module.getConfig();
        this.bankaccounts = new THashMap<String, BankAccount>();
        this.bankaccountsID = new THashMap<Long, BankAccount>();

        this.logger =  LoggerFactory.getLogger("cubeengine.conomy.transactions");
        if (!this.module.getConfig().enableLogging)
        {
            ((ch.qos.logback.classic.Logger)logger).getAppender("conomy.transactions-file").stop();
        }

        this.conomyInterface = new ConomyInterface(this);
    }

    public BankAccount getBankAccount(String name, boolean create)
    {
        BankAccount bankAccount = this.bankaccounts.get(name);
        if (bankAccount == null)
        {
            AccountModel model = this.ebean.find(AccountModel.class).where().eq("name",name).findUnique();
            if (model == null)
            {
                if (!create) return null;
                model = new AccountModel(null,name,(int) (this.config.defaultBankBalance * this.config.fractionalDigitsFactor()),false,this.config.bankNeedInvite);
                this.ebean.save(model);
                bankAccount = new BankAccount(this, model);
                this.logger.info("NEW Bank:{} :: {}", name,  bankAccount.balance());
            }
            else
            {
                bankAccount = new BankAccount(this, model);
                this.logger.info("LOAD Bank:{} :: {}", name, bankAccount.balance());
            }
            this.bankaccounts.put(name,bankAccount);
            this.bankaccountsID.put(bankAccount.model.getId(), bankAccount);
        }
        return bankAccount;
    }

    public UserAccount getUserAccount(User user, boolean create)
    {
        AccountAttachment attachment = user.attachOrGet(AccountAttachment.class, module);
        if (attachment.getAccount() == null)
        {
            if (!create) return null;
            attachment.createAccount();
        }
        return attachment.getAccount();
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
        this.ebean.createUpdate(AccountModel.class,
            "UPDATE :table SET value = :value " +
            "WHERE name IS NULL = :setUser " +
            "OR user_id IS NULL = :setBank")
            .setParameter("table", MySQLDatabase.prepareTableName("user"))
            // TODO cache the table names so we do not have to use strings (String getTableName(Class<?> entityClass) smth like this)
            .setParameter("value", longValue)
            .setParameter("setUser", userAcc)
            .setParameter("setBank", bankAcc).execute();
        this.logger.info("SET-ALL {} {}", (userAcc && bankAcc ? "User/Bank" : userAcc ? "User" : "Bank"), value);
        // update all loaded accounts...
        if (userAcc)
        {
            for (User user : this.module.getCore().getUserManager().getOnlineUsers())
            {
                UserAccount userAccount = ConomyManager.this.getUserAccount(user, false);
                if (userAccount != null)
                {
                    userAccount.model.setValue(longValue);
                }
            }
        }
        if (bankAcc)
        {
            for (BankAccount bankAccount : this.bankaccounts.values())
            {
                bankAccount.model.setValue(longValue);
            }
        }
    }

    public void scaleAll(boolean userAcc, boolean bankAcc, float factor)
    {
        this.ebean.createUpdate(AccountModel.class,
            "UPDATE :table SET value = :factor * value" +
            "WHERE name IS NULL = :setUser " +
            "OR user_id IS NULL = :setBank")
            .setParameter("table", MySQLDatabase.prepareTableName("user"))
            .setParameter("factor", factor)
            .setParameter("setUser", userAcc)
            .setParameter("setBank", bankAcc).execute();
        this.logger.info("SCALE-ALL {} {}", (userAcc && bankAcc ? "User/Bank" : userAcc ? "User" : "Bank"), factor);
        // update all loaded accounts...
        if (userAcc)
        {
            for (User user : this.module.getCore().getUserManager().getOnlineUsers())
            {
                UserAccount userAccount = ConomyManager.this.getUserAccount(user, false);
                if (userAccount != null)
                {
                    userAccount.model.setValue((long)(userAccount.model.getValue() * factor));
                }
            }
        }
        if (bankAcc)
        {
            for (BankAccount bankAccount : this.bankaccounts.values())
            {
                bankAccount.model.setValue((long)(bankAccount.model.getValue() * factor));
            }
        }
    }

    public void transactionAll(boolean userAcc, boolean bankAcc, double value)
    {
        final long longValue = (long)(value * this.config.fractionalDigitsFactor());
        this.ebean.createUpdate(AccountModel.class,
            "UPDATE :table SET value = :value + value" +
            "WHERE name IS NULL = :setUser " +
            "OR user_id IS NULL = :setBank")
            .setParameter("table", MySQLDatabase.prepareTableName("user"))
            .setParameter("value", longValue)
            .setParameter("setUser", userAcc)
            .setParameter("setBank", bankAcc).execute();
        this.logger.info("TRANSACTION-ALL {} {}", (userAcc && bankAcc ? "User/Bank" : userAcc ? "User" : "Bank"), value);
        // update all loaded accounts...
        if (userAcc)
        {
            for (User user : this.module.getCore().getUserManager().getOnlineUsers())
            {
                UserAccount userAccount = ConomyManager.this.getUserAccount(user, false);
                if (userAccount != null)
                {
                    userAccount.model.setValue(userAccount.model.getValue() + longValue);
                }
            }
        }
        if (bankAcc)
        {
            for (BankAccount bankAccount : this.bankaccounts.values())
            {
                bankAccount.model.setValue(bankAccount.model.getValue() + longValue);
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
            this.logger.info("TRANSACTION {}{} -> {}{}", (from instanceof UserAccount ? "User:" : "Bank:"),
                             from.getName(), (to instanceof UserAccount ? "User:" : "Bank:"),  to.getName());
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
        return this.ebean.find(AccountModel.class).where()
            .raw("((mask & 1) = 0 OR ((mask & 1) = 1) = " + showHidden + ")")
            .raw("(name IS NULL) = " + user + " OR " + "(user_id IS NULL) = " + bank)
            .orderBy().desc("value")
            .setFirstRow(fromRank - 1)
            .setMaxRows(toRank + 1 - fromRank).findList();
    }

    public void hideAll(boolean userAcc, boolean bankAcc)
    {
        this.ebean.createUpdate(AccountModel.class,
            "UPDATE :table SET mask = 1 | mask" +
            "WHERE name IS NULL = :setUser " +
            "OR user_id IS NULL = :setBank")
            .setParameter("table", MySQLDatabase.prepareTableName("userAcc"))
            .setParameter("setUser", userAcc)
            .setParameter("setBank", bankAcc).execute();
        if (userAcc)
        {
            for (User user : this.module.getCore().getUserManager().getOnlineUsers())
            {
                UserAccount userAccount = ConomyManager.this.getUserAccount(user, false);
                if (userAccount != null)
                {
                    userAccount.model.setHidden(true);
                }
            }
        }
        if (bankAcc)
        {
            for (BankAccount bankAccount : this.bankaccounts.values())
            {
                bankAccount.model.setHidden(true);
            }
        }
    }

    public void unhideAll(boolean userAcc, boolean bankAcc)
    {
        this.ebean.createUpdate(AccountModel.class,
            "UPDATE :table SET mask = 1 & ~mask" +
            "WHERE name IS NULL = :setUser " +
            "OR user_id IS NULL = :setBank")
            .setParameter("table", MySQLDatabase.prepareTableName("userAcc"))
            .setParameter("setUser", userAcc)
            .setParameter("setBank", bankAcc).execute();
        if (userAcc)
        {
            for (User user : this.module.getCore().getUserManager().getOnlineUsers())
            {
                UserAccount userAccount = ConomyManager.this.getUserAccount(user, false);
                if (userAccount != null)
                {
                    userAccount.model.setHidden(true);
                }
            }
        }
        if (bankAcc)
        {
            for (BankAccount bankAccount : this.bankaccounts.values())
            {
                bankAccount.model.setHidden(true);
            }
        }
    }

    public boolean deleteUserAccount(User user)
    {
        UserAccount account = this.getUserAccount(user, false);
        if (account == null)
        {
            return false;
        }
        this.ebean.delete(account.model);
        user.detach(AccountAttachment.class);
        return true;
    }

    public boolean deleteBankAccount(String name)
    {
        BankAccount bankAccount = this.bankaccounts.get(name);
        if (bankAccount == null)
        {
            return false;
        }
        this.ebean.delete(bankAccount.model);
        this.bankaccounts.remove(name);
        this.bankaccountsID.remove(bankAccount.model.getId());
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

    public Set<BankAccount> getBankAccounts(User user)
    {
        Set<Long> accountIds = this.bankAccessStorage.getBankAccounts(user);
        Set<BankAccount> accounts = new HashSet<BankAccount>();
        for (Long accountId : accountIds)
        {
            BankAccount acc = this.bankaccountsID.get(accountId);
            if (acc == null)
            {
                AccountModel model = this.ebean.find(AccountModel.class, accountId);
                acc = new BankAccount(this, model);
                this.bankaccountsID.put(accountId, acc);
                this.bankaccounts.put(acc.getName(), acc);
            }
            accounts.add(acc);
        }
        return accounts;
    }

    public boolean getAutoCreateUserAccount()
    {
        return this.config.autocreateUserAcc;
    }

    public boolean renameBank(BankAccount bankAccount, String newName)
    {
        BankAccount acc = this.getBankAccount(newName, false);
        if (acc != null) return false; // Account name exists!
        this.bankaccounts.remove(bankAccount.getName());
        bankAccount.model.setName(newName);
        this.update(bankAccount.model);
        this.bankaccounts.put(newName, bankAccount);
        return true;
    }

    public Economy getInterface()
    {
        return this.conomyInterface;
    }

    /**
     * Returns the names of all banks
     *
     * @param hidden if true return hidden banks too
     * @return
     */
    public Set<String> getBankNames(boolean hidden)
    {
        Set<String> banks = new HashSet<>();
        for (AccountModel accountModel : this.ebean.find(AccountModel.class).select("name")
                                                   .where().isNotNull("name")
                                                   .raw("(mask & 1 = 0) OR (mask & 1 = 1) = " + hidden).findList())
        {
            banks.add(accountModel.getName());
        }
        return banks;
    }

    public void update(AccountModel model)
    {
        this.ebean.update(model);
    }

    protected AccountModel loadUserAccount(User holder)
    {
        return this.ebean.find(AccountModel.class).where().eq("user_id",holder.getEntity().getId()).findUnique();
    }
}
