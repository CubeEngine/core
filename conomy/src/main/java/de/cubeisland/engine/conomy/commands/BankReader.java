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
package de.cubeisland.engine.conomy.commands;

import java.util.Locale;

import de.cubeisland.engine.conomy.account.BankAccount;
import de.cubeisland.engine.conomy.account.ConomyManager;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.exception.InvalidArgumentException;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;

public class BankReader extends ArgumentReader
{
    private final ConomyManager manager;

    public BankReader(ConomyManager manager)
    {
        this.manager = manager;
    }

    @Override
    public Object read(String arg, Locale locale) throws InvalidArgumentException
    {
        BankAccount target = this.manager.getBankAccount(arg, false);
        if (target == null)
        {
            throw new InvalidArgumentException(CubeEngine.getI18n().translate(locale,NEGATIVE, "There is no bank account named {input#name}!", arg));
        }
        return target;
    }
}
