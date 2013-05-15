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
package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.conomy.Currency.CurrencyType;
import de.cubeisland.cubeengine.conomy.Currency.CurrencyTypeConverter;
import de.cubeisland.cubeengine.conomy.account.AccountManager;
import de.cubeisland.cubeengine.conomy.commands.EcoCommands;
import de.cubeisland.cubeengine.conomy.commands.MoneyCommand;

public class Conomy extends Module
{
    private ConomyConfiguration config;
    //TODO Roles support (e.g. allow all user of a role to access a bank)
    private AccountManager manager;

    @Override
    public void onEnable()
    {
        Convert.registerConverter(CurrencyType.class, new CurrencyTypeConverter());
        this.config = Configuration.load(ConomyConfiguration.class, this);
        this.manager = new AccountManager(this);
        new ConomyPermissions(this);
        final CommandManager cm = this.getCore().getCommandManager();
        cm.registerCommand(new MoneyCommand(this));
        cm.registerCommand(new EcoCommands(this));
    }

    public ConomyConfiguration getConfig()
    {
        return this.config;
    }

    public AccountManager getManager()
    {
        return manager;
    }
}
