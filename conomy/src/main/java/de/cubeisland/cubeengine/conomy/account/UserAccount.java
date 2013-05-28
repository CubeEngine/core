package de.cubeisland.cubeengine.conomy.account;

import de.cubeisland.cubeengine.conomy.ConomyPermissions;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;

public class UserAccount extends Account
{
    private AccountAttachment attachment;

    public UserAccount(AccountAttachment attachment, ConomyManager manager, AccountModel model)
    {
        super(manager, model);
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
        this.manager.logger.info(action + " User:" + this.getName() + " " + value + " :: " + this.balance());
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
        if (ConomyPermissions.USER_ALLOWUNDERMIN.isAuthorized(this.attachment.getHolder()))
        {
            return true;
        }
        return (this.model.value - amount * this.manager.fractionalDigitsFactor()) >= this.getMinBalance();
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
