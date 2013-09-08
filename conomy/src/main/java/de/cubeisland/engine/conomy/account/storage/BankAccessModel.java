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
import de.cubeisland.engine.core.user.UserEntity;
import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.conomy.account.storage.TableBankAccess.TABLE_BANK_ACCESS;

public class BankAccessModel extends UpdatableRecordImpl<BankAccessModel> implements Record4<UInteger, UInteger, UInteger, Byte>
{
    public static final byte OWNER = 1;
    public static final byte MEMBER = 2;
    public static final byte INVITED = 4;

    public BankAccessModel newAccess(long id, UserEntity userEntity, AccountModel accountModel, byte accessLevel, String name)
    {
        this.setId(UInteger.valueOf(id));
        this.setUserid(userEntity.getKey());
        this.setAccountid(accountModel.getKey());
        this.setAccesslevel(accessLevel);
        return this;
    }

    public BankAccessModel newAccess(AccountModel accountModel, User user, byte type)
    {
        this.setUserid(user.getEntity().getKey());
        this.setAccountid(accountModel.getKey());
        this.setAccesslevel(type);
        return this;
    }

    public BankAccessModel()
    {
        super(TABLE_BANK_ACCESS);
    }

    public void setId(UInteger value) {
        setValue(0, value);
    }

    public UInteger getId() {
        return (UInteger) getValue(0);
    }

    public void setUserid(UInteger value) {
        setValue(1, value);
    }

    public UInteger getUserid() {
        return (UInteger) getValue(1);
    }

    public void setAccountid(UInteger value) {
        setValue(2, value);
    }

    public UInteger getAccountid() {
        return (UInteger) getValue(2);
    }

    public void setAccesslevel(Byte value) {
        setValue(3, value);
    }

    public Byte getAccesslevel() {
        return (Byte) getValue(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public org.jooq.Record1<org.jooq.types.UInteger> key() {
        return (org.jooq.Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public org.jooq.Row4<org.jooq.types.UInteger, org.jooq.types.UInteger, org.jooq.types.UInteger, java.lang.Byte> fieldsRow() {
        return (org.jooq.Row4) super.fieldsRow();
    }

    @Override
    public org.jooq.Row4<org.jooq.types.UInteger, org.jooq.types.UInteger, org.jooq.types.UInteger, java.lang.Byte> valuesRow() {
        return (org.jooq.Row4) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_BANK_ACCESS.ID;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_BANK_ACCESS.USERID;
    }

    @Override
    public Field<UInteger> field3() {
        return TABLE_BANK_ACCESS.ACCOUNTID;
    }

    @Override
    public Field<Byte> field4() {
        return TABLE_BANK_ACCESS.ACCESSLEVEL;
    }

    @Override
    public UInteger value1() {
        return getId();
    }

    @Override
    public UInteger value2() {
        return getUserid();
    }

    @Override
    public UInteger value3() {
        return getAccountid();
    }

    @Override
    public Byte value4() {
        return getAccesslevel();
    }
}
