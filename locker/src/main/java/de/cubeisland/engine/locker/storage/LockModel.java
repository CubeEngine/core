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
package de.cubeisland.engine.locker.storage;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.UUID;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record10;
import org.jooq.Row10;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.locker.storage.TableLocks.TABLE_LOCK;

public class LockModel extends UpdatableRecordImpl<LockModel> implements Record10<UInteger, UInteger, Short, Byte, Byte, byte[], Long, Long, Timestamp, Timestamp>
{
    public LockModel()
    {
        super(TABLE_LOCK);
    }

    public LockModel newLock(User user, LockType lockType, ProtectedType type)
    {
        return this.newLock(user, lockType, type, null);
    }

    public LockModel newLock(User user, LockType lockType, ProtectedType type, UUID entityUUID)
    {
        this.setOwnerId(user.getEntity().getKey());
        this.setLockType(lockType.id);
        this.setFlags((short)0); // none
        this.setType(type.id);
        if (entityUUID != null)
        {
            this.setEntityUidLeast(entityUUID.getLeastSignificantBits());
            this.setEntityUidMost(entityUUID.getMostSignificantBits());
        }
        this.setLastAccess(new Timestamp(System.currentTimeMillis()));
        this.setCreated(new Timestamp(System.currentTimeMillis()));
        return this;
    }

    private UUID uuid = null;

    public String getColorPass()
    {
        StringBuilder builder = new StringBuilder();
        String stringPass;
        if (this.getPassword().length != 4)
        {
            for (byte b : this.getPassword())
            {
                builder.append(String.format("%02X", b));
            }
            stringPass = builder.toString();
            builder = new StringBuilder();
        }
        else
        {
            stringPass = new String(this.getPassword());
        }
        for (char c : stringPass.toCharArray())
        {
            builder.append("&").append(c);
        }
        return ChatFormat.parseFormats(builder.toString());
    }

    public UUID getUUID()
    {
        if (this.uuid == null)
        {
            if (this.getEntityUidLeast() == null) return null;
            this.uuid = new UUID(this.getEntityUidMost(), this.getEntityUidLeast());
        }
        return this.uuid;
    }

    /**
     * Sets a new password for given lock-model
     *
     * @param manager
     * @param pass
     *
     * @return fluent interface
     */
    protected LockModel createPassword(LockManager manager, String pass)
    {
        if (pass != null)
        {
            synchronized (manager.messageDigest)
            {
                manager.messageDigest.reset();
                this.setPassword(manager.messageDigest.digest(pass.getBytes()));
            }
        }
        else
        {
            this.setPassword(StringUtils.randomString(new SecureRandom(), 4, "0123456789abcdefklmnor").getBytes());
        }
        return this;
    }

    public void setId(UInteger value) {
        setValue(0, value);
    }

    public UInteger getId() {
        return (UInteger) getValue(0);
    }

    public void setOwnerId(UInteger value) {
        setValue(1, value);
    }

    public UInteger getOwnerId() {
        return (UInteger) getValue(1);
    }

    public void setFlags(Short value) {
        setValue(2, value);
    }

    public Short getFlags() {
        return (Short) getValue(2);
    }

    public void setType(Byte value) {
        setValue(3, value);
    }

    public Byte getType() {
        return (Byte) getValue(3);
    }

    public void setLockType(Byte value) {
        setValue(4, value);
    }

    public Byte getLockType() {
        return (Byte) getValue(4);
    }

    public void setPassword(byte[] value) {
        setValue(5, value);
    }

    public byte[] getPassword() {
        return (byte[]) getValue(5);
    }

    public void setEntityUidLeast(Long value) {
        setValue(6, value);
    }

    public Long getEntityUidLeast() {
        return (Long) getValue(6);
    }

    public void setEntityUidMost(Long value) {
        setValue(7, value);
    }

    public Long getEntityUidMost() {
        return (Long) getValue(7);
    }

    public void setLastAccess(Timestamp value) {
        setValue(8, value);
    }

    public Timestamp getLastAccess() {
        return (Timestamp) getValue(8);
    }

    public void setCreated(Timestamp value) {
        setValue(9, value);
    }

    public Timestamp getCreated() {
        return (Timestamp) getValue(9);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row10<UInteger, UInteger, Short, Byte, Byte, byte[], Long, Long, Timestamp, Timestamp> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    @Override
    public Row10<UInteger, UInteger, Short, Byte, Byte, byte[], Long, Long, Timestamp, Timestamp> valuesRow() {
        return (Row10) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_LOCK.ID;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_LOCK.OWNER_ID;
    }

    @Override
    public Field<Short> field3() {
        return TABLE_LOCK.FLAGS;
    }

    @Override
    public Field<Byte> field4() {
        return TABLE_LOCK.PROTECTED_TYPE;
    }

    @Override
    public Field<Byte> field5() {
        return TABLE_LOCK.LOCK_TYPE;
    }

    @Override
    public Field<byte[]> field6() {
        return TABLE_LOCK.PASSWORD;
    }

    @Override
    public Field<Long> field7() {
        return TABLE_LOCK.ENTITY_UID_LEAST;
    }

    @Override
    public Field<Long> field8() {
        return TABLE_LOCK.ENTITY_UID_MOST;
    }

    @Override
    public Field<Timestamp> field9()
    {
        return TABLE_LOCK.LAST_ACCESS;
    }

    @Override
    public Field<Timestamp> field10()
    {
        return TABLE_LOCK.CREATED;
    }

    @Override
    public UInteger value1() {
        return getId();
    }

    @Override
    public UInteger value2() {
        return getOwnerId();
    }

    @Override
    public Short value3() {
        return getFlags();
    }

    @Override
    public Byte value4() {
        return getType();
    }

    @Override
    public Byte value5() {
        return getLockType();
    }

    @Override
    public byte[] value6() {
        return getPassword();
    }

    @Override
    public Long value7() {
        return getEntityUidLeast();
    }

    @Override
    public Long value8() {
        return getEntityUidMost();
    }

    @Override
    public Timestamp value9()
    {
        return getLastAccess();
    }

    @Override
    public Timestamp value10()
    {
        return getCreated();
    }
}
