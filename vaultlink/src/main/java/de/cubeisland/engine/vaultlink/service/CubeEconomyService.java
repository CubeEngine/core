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
package de.cubeisland.engine.vaultlink.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.vaultlink.Vaultlink;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import static net.milkbowl.vault.economy.EconomyResponse.ResponseType.FAILURE;
import static net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS;

public class CubeEconomyService implements Economy
{
    private final Vaultlink module;
    private final AtomicReference<de.cubeisland.engine.core.module.service.Economy> backingService;

    public CubeEconomyService(Vaultlink module, AtomicReference<de.cubeisland.engine.core.module.service.Economy> backingService)
    {
        this.module = module;
        this.backingService = backingService;
    }

    @Override
    public boolean isEnabled()
    {
        return module.isEnabled();
    }

    @Override
    public String getName()
    {
        return CubeEngine.class.getSimpleName() + ":" + module.getName();
    }

    @Override
    public boolean hasBankSupport()
    {
        return backingService.get().hasBankSupport();
    }

    @Override
    public int fractionalDigits()
    {
        return backingService.get().fractionalDigits();
    }

    @Override
    public String format(double amount)
    {
        return backingService.get().format(amount);
    }

    @Override
    public String currencyNamePlural()
    {
        return backingService.get().currencyNamePlural();
    }

    @Override
    public String currencyNameSingular()
    {
        return backingService.get().currencyName();
    }

    @Override
    public boolean hasAccount(String name)
    {
        return backingService.get().hasAccount(name);
    }

    @Override
    public boolean hasAccount(String name, String world)
    {
        return hasAccount(name);
    }

    @Override
    public double getBalance(String name)
    {
        return backingService.get().getBalance(name);
    }

    @Override
    public double getBalance(String name, String world)
    {
        return getBalance(name);
    }

    @Override
    public boolean has(String name, double amount)
    {
        return backingService.get().has(name, amount);
    }

    @Override
    public boolean has(String name, String world, double amount)
    {
        return has(name, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String player, double amount)
    {
        boolean result = backingService.get().withdraw(player, amount);
        String message = (result ? "Money successfully withdrawn!" : "You don't have enough money.");
        User user = module.getCore().getUserManager().findExactUser(player);
        if (user != null)
        {
            message = user.getTranslation(MessageType.NONE, message); // TODO
        }
        return new EconomyResponse(amount, getBalance(player), result ? SUCCESS : FAILURE, message);
    }

    @Override
    public EconomyResponse withdrawPlayer(String player, String world, double amount)
    {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String name, double amount)
    {
        boolean result = backingService.get().deposit(name, amount);
        return new EconomyResponse(amount, getBalance(name), result ? SUCCESS : FAILURE, result ? "Money successfully deposited!" : "Your account is full.");
    }

    @Override
    public EconomyResponse depositPlayer(String name, String world, double amount)
    {
        return depositPlayer(name, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String owner)
    {
        if (backingService.get().createBank(name, owner))
        {
            return new EconomyResponse(0, bankBalance(name).balance, SUCCESS, "");
        }

        return new EconomyResponse(0, 0, FAILURE, "Failed to create the bank!");
    }

    @Override
    public EconomyResponse deleteBank(String name)
    {
        if (!getBanks().contains(name))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        return new EconomyResponse(0, 0, backingService.get().deleteBank(name) ? SUCCESS : FAILURE, "");
    }

    @Override
    public EconomyResponse bankBalance(String name)
    {
        if (!getBanks().contains(name))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        return new EconomyResponse(0, backingService.get().getBankBalance(name), SUCCESS, "");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount)
    {
        if (!getBanks().contains(name))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        if (backingService.get().bankHas(name, amount))
        {
            return new EconomyResponse(0, getBalance(name), ResponseType.FAILURE, "The bank does not have enough money!");
        }
        else
        {
            return new EconomyResponse(0, getBalance(name), ResponseType.SUCCESS, "");
        }
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount)
    {
        EconomyResponse response = bankHas(name, amount);
        if (!response.transactionSuccess())
        {
            return response;
        }
        backingService.get().bankWithdraw(name, amount);
        return new EconomyResponse(amount, getBalance(name), ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount)
    {
        if (!getBanks().contains(name))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        backingService.get().bankDeposit(name, amount);
        return new EconomyResponse(amount, getBalance(name), ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String player)
    {
        if (!getBanks().contains(name))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        else if (backingService.get().isBankOwner(name, player))
        {
            return new EconomyResponse(0, bankBalance(name).balance, ResponseType.SUCCESS, "");
        }
        else
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That player is not a bank owner!");
        }
    }

    @Override
    public EconomyResponse isBankMember(String bank, String player)
    {
        if (!getBanks().contains(bank))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        else if (backingService.get().isBankMember(bank, player))
        {
            return new EconomyResponse(0, bankBalance(bank).balance, ResponseType.SUCCESS, "");
        }
        else
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That player is not a bank member!");
        }
    }

    @Override
    public List<String> getBanks()
    {
        return backingService.get().getBanks();
    }

    @Override
    public boolean createPlayerAccount(String name)
    {
        return backingService.get().createPlayerAccount(name);
    }

    @Override
    public boolean createPlayerAccount(String name, String world)
    {
        return createPlayerAccount(name);
    }
}
