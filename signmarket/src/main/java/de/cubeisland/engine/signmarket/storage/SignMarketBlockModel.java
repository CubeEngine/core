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
package de.cubeisland.engine.signmarket.storage;

import javax.persistence.Transient;

import org.bukkit.Location;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.Row11;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static de.cubeisland.engine.signmarket.storage.TableSignBlock.TABLE_SIGN_BLOCK;

public class SignMarketBlockModel extends UpdatableRecordImpl<SignMarketBlockModel> 
    implements Record11<UInteger, UInteger, Integer, Integer, Integer, Byte, UInteger, UInteger, UShort, UInteger, UInteger>
{
    // Helper-methods:
    @Transient
    private Location location;

    public SignMarketBlockModel newBlockModel(Location location)
    {
        this.setWorld(CubeEngine.getCore().getWorldManager().getWorldEntity(location.getWorld()).getKey());
        this.setX(location.getBlockX());
        this.setY(location.getBlockY());
        this.setZ(location.getBlockZ());
        return this;
    }

    /**
     * Copies the values from an other BlockModel into this one
     *
     * @param blockInfo the model to copy the values from
     */
    public void copyValuesFrom(SignMarketBlockModel blockInfo)
    {
        this.setSigntype(blockInfo.getSigntype());
        this.setOwner(blockInfo.getOwner());
        this.setItemkey(blockInfo.getItemkey());
        this.setAmount(blockInfo.getAmount());
        this.setDemand(blockInfo.getDemand());
        this.setPrice(blockInfo.getPrice());
    }

    /**
     * Returns the location of this sign
     * <p>Do NEVER change this location!
     *
     * @return the location of the sign represented by this model
     */
    public final Location getLocation()
    {
        if (this.location == null)
        {
            this.location = new Location(CubeEngine.getCore().getWorldManager().getWorld(this.getWorld().longValue()), this.getX(), this.getY(), this.getZ());
        }
        return this.location;
    }

    /**
     * Returns true if given user is the owner
     *
     * @param user
     * @return
     */
    public boolean isOwner(User user)
    {
        if (this.getOwner() == null) return user == null;
        if (user == null) return false;
        return user.getEntity().getKey().equals(this.getOwner());
    }

    public SignMarketBlockModel()
    {
        super(TABLE_SIGN_BLOCK);
        this.setKey(UInteger.valueOf(0));
    }

    public void setKey(UInteger value) {
        setValue(0, value);
    }

    public UInteger getKey() {
        return (UInteger) getValue(0);
    }

    public void setWorld(UInteger value) {
        setValue(1, value);
    }

    public UInteger getWorld() {
        return (UInteger) getValue(1);
    }

    public void setX(Integer value) {
        setValue(2, value);
    }

    public Integer getX() {
        return (Integer) getValue(2);
    }

    public void setY(Integer value) {
        setValue(3, value);
    }

    public Integer getY() {
        return (Integer) getValue(3);
    }

    public void setZ(Integer value) {
        setValue(4, value);
    }

    public Integer getZ() {
        return (Integer) getValue(4);
    }

    public void setSigntype(Byte value) {
        setValue(5, value);
    }

    public Byte getSigntype() {
        return (Byte) getValue(5);
    }

    public void setOwner(UInteger value) {
        setValue(6, value);
    }

    public UInteger getOwner() {
        return (UInteger) getValue(6);
    }

    public void setItemkey(UInteger value) {
        setValue(7, value);
    }

    public UInteger getItemkey() {
        return (UInteger) getValue(7);
    }

    public void setAmount(UShort value) {
        setValue(8, value);
    }

    public UShort getAmount() {
        return (UShort) getValue(8);
    }

    public void setDemand(UInteger value) {
        setValue(9, value);
    }

    public UInteger getDemand() {
        return (UInteger) getValue(9);
    }

    public void setPrice(UInteger value) {
        setValue(10, value);
    }

    public UInteger getPrice() {
        return (UInteger) getValue(10);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record11 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row11<UInteger, UInteger, Integer, Integer, Integer, Byte, UInteger, UInteger, UShort, UInteger, UInteger> fieldsRow() {
        return (Row11) super.fieldsRow();
    }

    @Override
    public Row11<UInteger, UInteger, Integer, Integer, Integer, Byte, UInteger, UInteger, UShort, UInteger, UInteger> valuesRow() {
        return (Row11) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_SIGN_BLOCK.KEY;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_SIGN_BLOCK.WORLD;
    }

    @Override
    public org.jooq.Field<Integer> field3() {
        return TABLE_SIGN_BLOCK.X;
    }

    @Override
    public org.jooq.Field<Integer> field4() {
        return TABLE_SIGN_BLOCK.Y;
    }

    @Override
    public org.jooq.Field<Integer> field5() {
        return TABLE_SIGN_BLOCK.Z;
    }

    @Override
    public org.jooq.Field<Byte> field6() {
        return TABLE_SIGN_BLOCK.SIGNTYPE;
    }

    @Override
    public Field<UInteger> field7() {
        return TABLE_SIGN_BLOCK.OWNER;
    }

    @Override
    public Field<UInteger> field8() {
        return TABLE_SIGN_BLOCK.ITEMKEY;
    }

    @Override
    public org.jooq.Field<UShort> field9() {
        return TABLE_SIGN_BLOCK.AMOUNT;
    }

    @Override
    public Field<UInteger> field10() {
        return TABLE_SIGN_BLOCK.DEMAND;
    }

    @Override
    public Field<UInteger> field11() {
        return TABLE_SIGN_BLOCK.PRICE;
    }

    @Override
    public UInteger value1() {
        return getKey();
    }

    @Override
    public UInteger value2() {
        return getWorld();
    }

    @Override
    public Integer value3() {
        return getX();
    }

    @Override
    public Integer value4() {
        return getY();
    }

    @Override
    public Integer value5() {
        return getZ();
    }

    @Override
    public Byte value6() {
        return getSigntype();
    }

    @Override
    public UInteger value7() {
        return getOwner();
    }

    @Override
    public UInteger value8() {
        return getItemkey();
    }

    @Override
    public UShort value9() {
        return getAmount();
    }

    @Override
    public UInteger value10() {
        return getDemand();
    }

    @Override
    public UInteger value11() {
        return getPrice();
    }
}
