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
import de.cubeisland.engine.core.user.UserAttachment;

import static de.cubeisland.engine.conomy.account.storage.TableAccount.TABLE_ACCOUNT;

public class AccountAttachment extends UserAttachment
{
    private UserAccount userAccount;
    private ConomyManager manager;

    /**
     * Gets the UserAccount of this User (may be null)
     *
     * @return
     */
    public UserAccount getAccount()
    {
        return userAccount;
    }

    @Override
    public void onAttach()
    {
        if (this.getModule() instanceof Conomy)
        {
            this.manager = ((Conomy)this.getModule()).getManager();
            AccountModel model = manager.loadUserAccount(this.getHolder());
            if (model != null)
            {
                this.userAccount = new UserAccount(this, manager, model);
                manager.logger.debug("LOAD User: {} :: {}", userAccount.getName(), userAccount.balance());
            }
        }
        else
        {
            throw new IllegalArgumentException("The module was not Conomy!");
        }
    }

    /**
     * Creates a new UserAccount or returns the current account
     *
     * @return
     */
    public UserAccount createAccount()
    {
        if (this.userAccount != null) return this.getAccount();
        AccountModel model = this.getModule().getCore().getDB().getDSL().newRecord(TABLE_ACCOUNT).
            newAccount(this.getHolder(), null,(long) (this.manager.config.defaultBalance * this.manager.config.fractionalDigitsFactor()), false);
        model.insert();
        this.userAccount = new UserAccount(this, this.manager, model);
        this.manager.logger.debug("NEW User: {} :: {}", this.getHolder().getName(), userAccount.balance());
        return this.userAccount;
    }
}
