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
import java.util.UUID;

import de.cubeisland.engine.core.module.service.Economy;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;

public class ConomyInterface implements Economy
{
    private final ConomyManager manager;
    private final UserManager um;

    public ConomyInterface(ConomyManager manager)
    {
        this.manager = manager;
        this.um = manager.module.getCore().getUserManager();
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
    public boolean hasAccount(UUID player)
    {
        return manager.getUserAccount(um.getExactUser(player), false) != null;
    }

    @Override
    public double getBalance(UUID player)
    {
        return this.getUserAccount(player).balance();
    }

    @Override
    public boolean has(UUID player, double amount)
    {
        return this.getUserAccount(player).has(amount);
    }

    @Override
    public boolean withdraw(UUID player, double amount)
    {
        this.getUserAccount(player).withdraw(amount);
        return true;
    }

    @Override
    public boolean deposit(UUID player, double amount)
    {
        this.getUserAccount(player).deposit(amount);
        return true;
    }

    private UserAccount getUserAccount(UUID player)
    {
        User user = um.getExactUser(player);
        UserAccount userAccount = manager.getUserAccount(user, false);
        if (userAccount == null)
        {
            throw new IllegalArgumentException(user.getDisplayName() + " has no Account!");
        }
        return userAccount;
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
        {
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        }
        return bankAccount.balance();
    }

    @Override
    public boolean bankHas(String name, double amount)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
        {
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        }
        return bankAccount.has(amount);
    }

    @Override
    public boolean bankWithdraw(String name, double amount)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
        {
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        }
        bankAccount.withdraw(amount);
        return true;
    }

    @Override
    public boolean bankDeposit(String name, double amount)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
        {
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        }
        bankAccount.deposit(amount);
        return true;
    }

    @Override
    public boolean isBankOwner(String name, UUID player)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
        {
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        }
        User user = this.manager.module.getCore().getUserManager().getExactUser(player);
        if (user == null)
        {
            throw new IllegalArgumentException("Unknown User: " + player);
        }
        return bankAccount.isOwner(user);
    }

    @Override
    public boolean isBankMember(String name, UUID player)
    {
        BankAccount bankAccount = manager.getBankAccount(name, false);
        if (bankAccount == null)
        {
            throw new IllegalArgumentException("There is no bankaccount named: " + name);
        }
        User user = this.manager.module.getCore().getUserManager().getExactUser(player);
        if (user == null)
        {
            throw new IllegalArgumentException("Unknown User: " + player);
        }
        return bankAccount.isMember(user);
    }

    @Override
    public List<String> getBanks()
    {
        return new ArrayList<>(this.manager.getBankNames(true));
    }

    @Override
    public boolean createAccount(UUID player)
    {
        if (this.hasAccount(player))
        {
            return false;
        }
        manager.getUserAccount(um.getExactUser(player), true);
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

    @Override
    public boolean deleteAccount(UUID player)
    {
        return this.manager.deleteUserAccount(um.getExactUser(player));
    }

    @Override
    public boolean bankExists(String name)
    {
        return this.manager.bankAccountExists(name);
    }
}
