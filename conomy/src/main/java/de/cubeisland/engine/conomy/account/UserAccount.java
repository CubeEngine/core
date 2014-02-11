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

import de.cubeisland.engine.conomy.Conomy;
import de.cubeisland.engine.conomy.account.storage.AccountModel;

public class UserAccount extends Account
{
    private final Conomy module;
    private final AccountAttachment attachment;

    public UserAccount(AccountAttachment attachment, ConomyManager manager, AccountModel model)
    {
        super(manager, model);
        module = manager.module;
        this.attachment = attachment;
    }

    @Override
    public String getName()
    {
        return attachment.getHolder().getName();
    }

    @Override
    public void log(String action, Object value)
    {
        this.manager.logger.info("{} User:{} {} :: {}", action, this.getName(), value.toString(), this.balance());
    }

    /**
     * Deletes this UserAccount AND also detaches the AccountAttachment!
     */
    public void delete()
    {
        this.manager.deleteUserAccount(this.attachment.getHolder());
    }

    @Override
    public boolean has(double amount)
    {
        if (module.perms().USER_ALLOWUNDERMIN.isAuthorized(this.attachment.getHolder()))
        {
            return true;
        }
        return (this.model.getValue() - amount * this.manager.fractionalDigitsFactor()) >= this.getMinBalance();
    }

    @Override
    public double getDefaultBalance()
    {
        return this.manager.getDefaultBalance();
    }

    @Override
    public double getMinBalance()
    {
        return this.manager.getMinimumBalance();
    }
}
