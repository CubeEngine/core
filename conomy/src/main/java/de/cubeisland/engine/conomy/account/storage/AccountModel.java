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
package de.cubeisland.engine.conomy.account.storage;

import de.cubeisland.engine.core.user.User;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.conomy.account.storage.TableAccount.TABLE_ACCOUNT;

public class AccountModel extends UpdatableRecordImpl<AccountModel> implements Record5<UInteger, UInteger, String, Long, Byte>
{
    public AccountModel()
    {
        super(TABLE_ACCOUNT);
    }

    public AccountModel newAccount(User user, String name, long balance, boolean hidden, boolean needsInvite)
    {
        this.setUserId(user == null ? null : user.getEntity().getKey());
        this.setName(name);
        this.setValue(balance);
        this.setMask((byte)((hidden ? 1 : 0) + (needsInvite ? 2 : 0)));
        return this;
    }

    public AccountModel newAccount(User user, String name, long balance, boolean hidden)
    {
        return this.newAccount(user, name, balance, hidden, false);
    }

    public boolean needsInvite()
    {
        return (this.getMask() & 2) == 2;
    }

    public boolean isHidden()
    {
        return (this.getMask() & 1) == 1;
    }

    public void setNeedsInvite(boolean set)
    {
        Byte mask = this.getMask();
        if (set)
        {
            mask |= 2;
        }
        else
        {
            mask &= ~2;
        }
        this.setMask(mask);
    }

    public void setHidden(boolean set)
    {
        Byte mask = this.getMask();
        if (set)
        {
            mask |= 1;
        }
        else
        {
            mask &= ~1;
        }
        this.setMask(mask);
    }

    public void setKey(UInteger value) {
        setValue(0, value);
    }

    public UInteger getKey() {
        return (UInteger) getValue(0);
    }

    public void setUserId(UInteger value) {
        setValue(1, value);
    }

    public UInteger getUserId() {
        return (UInteger) getValue(1);
    }

    public void setName(String value) {
        setValue(2, value);
    }

    public String getName() {
        return (String) getValue(2);
    }

    public void setValue(Long value) {
        setValue(3, value);
    }

    public Long getValue() {
        return (Long) getValue(3);
    }

    public void setMask(Byte value) {
        setValue(4, value);
    }

    public Byte getMask() {
        return (Byte) getValue(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<UInteger, UInteger, String, Long, Byte> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<UInteger, UInteger, String, Long, Byte> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_ACCOUNT.KEY;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_ACCOUNT.USER_ID;
    }

    @Override
    public Field<String> field3() {
        return TABLE_ACCOUNT.NAME;
    }

    @Override
    public Field<Long> field4() {
        return TABLE_ACCOUNT.VALUE;
    }

    @Override
    public Field<Byte> field5() {
        return TABLE_ACCOUNT.MASK;
    }

    @Override
    public UInteger value1() {
        return getKey();
    }

    @Override
    public UInteger value2() {
        return getUserId();
    }

    @Override
    public String value3() {
        return getName();
    }

    @Override
    public Long value4() {
        return getValue();
    }

    @Override
    public Byte value5() {
        return getMask();
    }
}
