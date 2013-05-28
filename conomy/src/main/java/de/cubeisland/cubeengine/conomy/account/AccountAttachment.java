package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.core.user.UserAttachment;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;

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
            this.manager =  ((Conomy)this.getModule()).getManager();
            AccountModel model = manager.storage.getUserAccount(this.getHolder().key);
            if (model != null)
            {
                this.userAccount = new UserAccount(this, manager, model);
                manager.logger.info("LOAD User:" + userAccount.getName() + " :: " + userAccount.balance());
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
        AccountModel model = new AccountModel(this.getHolder().key, null,
              (long) (this.manager.config.defaultBalance * this.manager.config.fractionalDigitsFactor()), false);
        this.manager.storage.store(model);
        this.userAccount = new UserAccount(this, this.manager, model);
        this.manager.logger.info("NEW User:" + this.getHolder().getName() + " :: " + userAccount.balance());
        return this.userAccount;
    }
}
