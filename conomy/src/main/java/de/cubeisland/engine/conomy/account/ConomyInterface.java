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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.cubeisland.engine.core.module.service.Economy;
import de.cubeisland.engine.core.user.User;

// TODO UUID usage
public class ConomyInterface implements Economy
{
    private final ConomyManager manager;

    public ConomyInterface(ConomyManager manager)
    {
        this.manager = manager;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
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
        return manager.config.fractionalDigits;
    }

    @Override
    public String format(double amount)
    {
        return manager.format(amount);
    }

    @Override
    public String format(Locale locale, double amount)
    {
        return manager.format(locale, amount);
    }

    @Override
    public String currencyNamePlural()
    {
        return manager.config.namePlural;
    }

    @Override
    public String currencyName()
    {
        return manager.config.name;
    }

    @Override
    public boolean hasAccount(String player)
    {
        return manager.userAccountExists(player);
    }

    @Override
    public double getBalance(String playerName)
    {
        UserAccount userAccount = manager.getUserAccount(playerName, false);
        if (userAccount == null)
            throw new IllegalArgumentException(playerName+ " has no Account!");
        return userAccount.balance();
    }

    @Override
    public boolean has(String playerName, double amount)
    {
        UserAccount userAccount = manager.getUserAccount(playerName, false);
        if (userAccount == null)
            throw new IllegalArgumentException(playerName+ " has no Account!");
        return userAccount.has(amount);
    }

    @Override
    public boolean withdraw(String playerName, double amount)
    {
        UserAccount userAccount = manager.getUserAccount(playerName, false);
        if (userAccount == null)
            throw new IllegalArgumentException(playerName+ " has no Account!");
        userAccount.withdraw(amount);
        return true;
    }

    @Override
    public boolean deposit(String playerName, double amount)
    {
        UserAccount userAccount = manager.getUserAccount(playerName, false);
        if (userAccount == null)
            throw new IllegalArgumentException(playerName+ " has no Account!");
        userAccount.deposit(amount);
        return true;
    }

    @Override
    public boolean createBank(String name, String ownerName)
    {
        if (manager.bankAccountExists(name))
        {
            return false;
        }
        BankAccount bankAccount = manager.getBankAccount(name, true);
        if (ownerName != null)
        {
            User user = this.manager.module.getCore().getUserManager().findExactUser(ownerName);
            if (user == null)
            {
                throw new IllegalArgumentException("Unknown User: " + ownerName);
            }
            bankAccount.promoteToOwner(user);
        }
        return true;
    }

    @Override
    public boolean deleteBank(String name)
    {
        return manager.deleteBankAccount(name);
    }

    @Override
    public double getBankBalance(String name)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        return bankAccount.balance();
    }

    @Override
    public boolean bankHas(String name, double amount)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        return bankAccount.has(amount);
    }

    @Override
    public boolean bankWithdraw(String name, double amount)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        bankAccount.withdraw(amount);
        return true;
    }

    @Override
    public boolean bankDeposit(String name, double amount)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        bankAccount.deposit(amount);
        return true;
    }

    @Override
    public boolean isBankOwner(String name, String playerName)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        User user = this.manager.module.getCore().getUserManager().findExactUser(playerName);
        if (user == null)
        {
            throw new IllegalArgumentException("Unknown User: " + playerName);
        }
        return bankAccount.isOwner(user);
    }

    @Override
    public boolean isBankMember(String name, String playerName)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        User user = this.manager.module.getCore().getUserManager().findExactUser(playerName);
        if (user == null)
        {
            throw new IllegalArgumentException("Unknown User: " + playerName);
        }
        return bankAccount.isMember(user);
    }

    @Override
    public List<String> getBanks()
    {
        return new ArrayList<>(this.manager.getBankNames(true));
    }

    @Override
    public boolean createPlayerAccount(String playerName)
    {
        if (manager.userAccountExists(playerName))
        {
            return false;
        }
        manager.getUserAccount(playerName, true);
        return true;
    }

    @Override
    public long fractionalDigitsFactor()
    {
        return this.manager.fractionalDigitsFactor();
    }

    @Override
    public Double parse(String price)
    {
        return this.manager.parse(price, Locale.getDefault());
    }

    @Override
    public Double parseFor(String price, Locale locale)
    {
        return this.manager.parse(price, locale);
    }

    @Override
    public double convertLongToDouble(long value)
    {
        return (double)value / this.fractionalDigitsFactor();
    }
}
