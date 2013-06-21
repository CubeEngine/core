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
package de.cubeisland.cubeengine.conomy.account;

import java.util.HashMap;
import java.util.Map;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.account.storage.BankAccessModel;

import static de.cubeisland.cubeengine.conomy.account.storage.BankAccessModel.*;

public class BankAccount extends Account
{
    private Map<Long, BankAccessModel> owner;
    private Map<Long, BankAccessModel> member;

    private Map<Long, BankAccessModel> invites;

    protected BankAccount(ConomyManager manager, AccountModel model)
    {
        super(manager, model);
        this.owner = new HashMap<Long, BankAccessModel>();
        this.member = new HashMap<Long, BankAccessModel>();
        this.invites = new HashMap<Long, BankAccessModel>();
        for (BankAccessModel access : this.manager.bankAccessStorage.getBankAccess(this.model))
        {
            switch (access.accessLevel)
            {
               case OWNER:
                   this.owner.put(access.userId, access);
                   break;
               case MEMBER:
                   this.member.put(access.userId, access);
                   break;
               case INVITED:
                   this.invites.put(access.userId, access);
            }
        }
    }

    @Override
    public String getName()
    {
        return this.model.name;
    }

    @Override
    public void log(String action, Object value)
    {
        this.manager.logger.info("{} Bank:{} {} :: {}", action, this.getName(), value.toString(), ""+this.balance());
    }

    /**
     * Deletes this BankAccount
     */
    public void delete()
    {
        this.manager.deleteBankAccount(this.getName());
    }

    @Override
    public boolean has(double amount)
    {
        return (this.model.value - amount * this.manager.fractionalDigitsFactor()) >= this.manager.getMinimumBankBalance();
    }

    @Override
    public double getDefaultBalance()
    {
        return this.manager.getDefaultBankBalance();
    }

    @Override
    public double getMinBalance()
    {
        return this.manager.getMinimumBankBalance();
    }

    /**
     * Sets the access-level of given user to owner.
     * The old owner will be set to member status.
     *
     * @param user the user to set as owner
     * @return false if given user is already owner
     */
    public boolean promoteToOwner(User user)
    {
        if (this.isOwner(user)) return false;
        // Search if new owner is moderator OR member
        BankAccessModel access = this.member.remove(user.key);
        if (access != null) // promote new owner
        {
            access.accessLevel = OWNER;
            this.manager.bankAccessStorage.update(access);
        }
        else // create new owner
        {
            access = new BankAccessModel(this.model, user, OWNER);
            this.manager.bankAccessStorage.store(access);
        }
        this.owner.put(user.key, access);
        return true;
    }

    /**
     * Demotes a users access onto this bank to member
     *
     * @param user the user to demote
     * @return false if already member rank OR not member
     */
    public boolean demote(User user)
    {
        if (this.isOwner(user))
        {
            BankAccessModel access = this.owner.remove(user.key);
            access.accessLevel = MEMBER;
            this.manager.bankAccessStorage.update(access);
            this.member.put(user.key, access);
            return true;
        }
        return false;
    }

    /**
     * Promotes a non-member to access this bank
     *
     * @param user the user to promote to member
     * @return false if already member OR owner
     */
    public boolean promoteToMember(User user)
    {
        if (this.hasAccess(user)) return false;
        BankAccessModel access = this.invites.remove(user.key);
        if (access == null)
        {
            access = new BankAccessModel(this.model, user, MEMBER);
            this.manager.bankAccessStorage.store(access);
        }
        else
        {
            access.accessLevel = MEMBER;
            this.manager.bankAccessStorage.update(access);
        }
        this.member.put(user.key, access);
        return true;
    }

    /**
     * Removes access from given user onto this Bank.
     *
     * @param user the user to kick
     * @return false if the user had no access
     */
    public boolean kickUser(User user)
    {
        BankAccessModel oldAccess = this.owner.remove(user.key);
        if (oldAccess == null)
        {
            oldAccess = this.member.remove(user.key);
        }
        if (oldAccess != null)
        {
            this.manager.bankAccessStorage.delete(oldAccess);
            return true;
        }
        return false; // is not member OR moderator
    }

    /**
     * Returns whether the given user is owner of this bank
     *
     * @param user the user to check
     * @return true if given user is owner
     */
    public boolean isOwner(User user)
    {
        return this.owner.get(user.key) != null;
    }

    public boolean isMember(User user)
    {
        return this.member.get(user.key) != null;
    }

    public boolean isInvited(User user)
    {
        return this.invites.get(user.key) != null;
    }

    public boolean hasAccess(User user)
    {
        return this.isOwner(user) || this.isMember(user);
    }

    public boolean invite(User user)
    {
        if (!needsInvite() || this.hasAccess(user) || this.invites.get(user.key) != null) return false;
        BankAccessModel invite = new BankAccessModel(this.model, user, INVITED);
        this.manager.bankAccessStorage.store(invite);
        this.invites.put(user.key, invite);
        return true;
    }

    public boolean uninvite(User user)
    {
        if (!needsInvite() || this.hasAccess(user) || this.invites.get(user.key) == null) return false;
        BankAccessModel invite = this.invites.remove(user.key);
        this.manager.bankAccessStorage.delete(invite);
        return true;
    }

    public boolean needsInvite()
    {
        return this.model.needsInvite();
    }

    public void setNeedsInvite(boolean set)
    {
        this.model.setNeedsInvite(set);
    }

    public boolean rename(String newName)
    {
        return this.manager.renameBank(this, newName);
    }
}
