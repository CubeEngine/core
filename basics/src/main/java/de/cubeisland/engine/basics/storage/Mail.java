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
package de.cubeisland.engine.basics.storage;

import javax.persistence.Entity;
import javax.persistence.Table;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.basics.storage.TableMail.TABLE_MAIL;

@Entity
@Table(name = "mail")
public class Mail extends UpdatableRecordImpl<Mail> implements Record4<UInteger, String, UInteger, UInteger>
{
    public Mail newMail(UInteger userId, UInteger senderId, String message)
    {
        this.setMessage(message);
        this.setUserid(userId);
        this.setSenderid(senderId);
        return this;
    }

    public Mail()
    {
        super(TABLE_MAIL);
    }

    public String readMail()
    {
        if (this.getSenderid() == null || this.getSenderid().longValue() == 0)
        {
            return ChatFormat.RED + "CONSOLE" + ChatFormat.WHITE + ": " + this.getMessage();
        }
        User user = CubeEngine.getUserManager().getUser(this.getSenderid());
        return ChatFormat.DARK_GREEN + user.getDisplayName() + ChatFormat.WHITE + ": " + this.getMessage();
    }

    public void setKey(UInteger value) {
        setValue(0, value);
    }

    public UInteger getKey() {
        return (UInteger) getValue(0);
    }

    public void setMessage(String value) {
        setValue(1, value);
    }

    public String getMessage() {
        return (String) getValue(1);
    }

    public void setUserid(UInteger value) {
        setValue(2, value);
    }

    public UInteger getUserid() {
        return (UInteger) getValue(2);
    }

    public void setSenderid(UInteger value) {
        setValue(3, value);
    }

    public UInteger getSenderid() {
        return (UInteger) getValue(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public org.jooq.Record1<UInteger> key() {
        return (org.jooq.Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<UInteger, String, UInteger, UInteger> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<UInteger, String, UInteger, UInteger> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_MAIL.KEY;
    }

    @Override
    public Field<String> field2() {
        return TABLE_MAIL.MESSAGE;
    }

    @Override
    public Field<UInteger> field3() {
        return TABLE_MAIL.USERID;
    }

    @Override
    public Field<UInteger> field4() {
        return TABLE_MAIL.SENDERID;
    }

    @Override
    public UInteger value1() {
        return getKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getMessage();
    }

    @Override
    public UInteger value3() {
        return getUserid();
    }

    @Override
    public UInteger value4() {
        return getSenderid();
    }
}
