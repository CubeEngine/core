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
package de.cubeisland.engine.powersigns.storage;

import org.bukkit.Location;

import de.cubeisland.engine.powersigns.signtype.SignTypeInfo;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record10;
import org.jooq.Row10;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.powersigns.storage.TablePowerSign.TABLE_POWER_SIGN;

public class PowerSignModel extends UpdatableRecordImpl<PowerSignModel> implements Record10<UInteger, UInteger, String, UInteger, Integer, Integer, Integer, Integer, Integer, String>
{
    public PowerSignModel()
    {
        super(TABLE_POWER_SIGN);
    }

    public PowerSignModel newPSign(SignTypeInfo info)
    {
        this.setOwnerId(UInteger.valueOf(info.getCreator()));
        this.setPSID(info.getType().getPSID());
        Location location = info.getLocation();
        this.setWorld(UInteger.valueOf(info.getWorldID()));
        this.setX(location.getBlockX());
        this.setY(location.getBlockY());
        this.setZ(location.getBlockZ());
        this.setChunkX(location.getChunk().getX());
        this.setChunkZ(location.getChunk().getZ());
        this.setData(info.serializeData());
        return this;
    }

    public UInteger getId()
    {
        return (UInteger)this.getValue(0);
    }

    public void setId(UInteger id)
    {
        this.setValue(0, id);
    }

    public UInteger getOwnerId()
    {
        return (UInteger)this.getValue(1);
    }

    public void setOwnerId(UInteger owner_id)
    {
        this.setValue(1, owner_id);
    }

    public String getPSID()
    {
        return (String)this.getValue(2);
    }

    public void setPSID(String PSID)
    {
        this.setValue(2, PSID);
    }

    public UInteger getWorldId()
    {
        return (UInteger)this.getValue(3);
    }

    public void setWorld(UInteger worldId)
    {
        this.setValue(3, worldId);
    }

    public Integer getX()
    {
        return (Integer)this.getValue(4);
    }

    public void setX(Integer x)
    {
        this.setValue(4, x);
    }

    public int getY()
    {
        return (Integer)this.getValue(5);
    }

    public void setY(Integer y)
    {
        this.setValue(5, y);
    }

    public int getZ()
    {
        return (Integer)this.getValue(6);
    }

    public void setZ(Integer z)
    {
        this.setValue(6, z);
    }

    public int getChunkX()
    {
        return (Integer)this.getValue(7);
    }

    public void setChunkX(int chunkX)
    {
        this.setValue(7, chunkX);
    }

    public int getChunkZ()
    {
        return (Integer)this.getValue(8);
    }

    public void setChunkZ(int chunkZ)
    {
        this.setValue(8, chunkZ);
    }

    public String getData()
    {
        return (String)this.getValue(9);
    }

    public void setData(String data)
    {
        this.setValue(9, data);
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
    public Row10<UInteger, UInteger, String, UInteger, Integer, Integer, Integer, Integer, Integer, String> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    @Override
    public Row10<UInteger, UInteger, String, UInteger, Integer, Integer, Integer, Integer, Integer, String> valuesRow() {
        return (Row10) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_POWER_SIGN.ID;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_POWER_SIGN.OWNER_ID;
    }

    @Override
    public Field<String> field3() {
        return TABLE_POWER_SIGN.PSID;
    }

    @Override
    public Field<UInteger> field4() {
        return TABLE_POWER_SIGN.WORLD;
    }

    @Override
    public Field<Integer> field5() {
        return TABLE_POWER_SIGN.X;
    }
    @Override
    public Field<Integer> field6() {
        return TABLE_POWER_SIGN.Y;
    }
    @Override
    public Field<Integer> field7() {
        return TABLE_POWER_SIGN.Z;
    }
    @Override
    public Field<Integer> field8() {
        return TABLE_POWER_SIGN.CHUNKX;
    }
    @Override
    public Field<Integer> field9() {
        return TABLE_POWER_SIGN.CHUNKZ;
    }
    @Override
    public Field<String> field10() {
        return TABLE_POWER_SIGN.DATA;
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
    public String value3() {
        return getPSID();
    }

    @Override
    public UInteger value4() {
        return getWorldId();
    }

    @Override
    public Integer value5() {
        return getX();
    }

    @Override
    public Integer value6() {
        return getY();
    }
    @Override
    public Integer value7() {
        return getZ();
    }
    @Override
    public Integer value8() {
        return getChunkX();
    }
    @Override
    public Integer value9() {
        return getChunkZ();
    }
    @Override
    public String value10() {
        return getData();
    }

}
