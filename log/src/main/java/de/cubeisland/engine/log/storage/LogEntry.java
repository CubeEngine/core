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
package de.cubeisland.engine.log.storage;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.TreeSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import com.fasterxml.jackson.databind.JsonNode;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.ActionType;
import de.cubeisland.engine.log.action.logaction.container.ContainerType;
import org.jooq.Field;
import org.jooq.Record13;
import org.jooq.Row13;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.log.storage.TableLogEntry.TABLE_LOG_ENTRY;

public class LogEntry extends UpdatableRecordImpl<LogEntry>
    implements Record13<UInteger, Timestamp, UInteger, Integer, Integer, Integer, UInteger, Long, String, Long, String, Byte, String>
{

    private Log module;
    private UserManager um;
    private TreeSet<LogEntry> attached = new TreeSet<>();
    private ActionType actionType;
    private World world;
    private BlockVector3 location;
    private JsonNode additional;

    public LogEntry()
    {
        super(TableLogEntry.TABLE_LOG_ENTRY);
    }

    public LogEntry init(Log module)
    {
        this.module = module;
        this.um = module.getCore().getUserManager();

        this.location = new BlockVector3(this.getX(),this.getY(),this.getZ());
        this.actionType = this.module.getActionTypeManager().getActionType(this.getAction().intValue());
        this.world = module.getCore().getWorldManager().getWorld(this.getWorldID().longValue());
        this.additional = this.getAdditionaldata() == null ? null : this.readJson(this.getAdditionaldata());
        return this;
    }

    private JsonNode readJson(String string)
    {
        if (string == null)
        {
            return null;
        }
        try
        {
            return this.module.getObjectMapper().readTree(string);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Could not read additional data: "+ string, e);
        }
    }

    public void attach(LogEntry next)
    {
        this.attached.add(next);
    }

    public boolean isSimilar(LogEntry other)
    {
        return this.actionType.isSimilar(this,other);
    }

    public boolean hasAttached()
    {
        return !this.attached.isEmpty();
    }

    public User getCauserUser()
    {
        return this.um.getUser(this.getCauser());
    }

    public World getWorld()
    {
        return this.world;
    }

    public ImmutableBlockData getOldBlock()
    {
        return new ImmutableBlockData(Material.getMaterial(this.getBlock()),this.getData().byteValue());
    }

    public ImmutableBlockData getNewBlock()
    {
        return new ImmutableBlockData(Material.getMaterial(this.getNewblock()),this.getNewdata());
    }

    public ImmutableBlockData getMaterialFromNewBlock()
    {
        return new ImmutableBlockData(Material.getMaterial(this.getNewblock()),(byte)0);
    }

    public EntityData getCauserEntity()
    {
        if (this.hasCauserEntity())
        {
            return new EntityData(EntityType.fromId((int)-getCauser()),this.getAdditional());
        }
        else
        {
            return null;
        }
    }

    public boolean hasCauserEntity()
    {
        return getCauser() < 0;
    }

    /**
     * Gets the Entity represented by the negative value in data.
     * @return
     */
    public EntityData getEntityFromData()
    {
        return new EntityData(EntityType.fromId(-getData().intValue()),this.getAdditional());
    }

    /**
     *
     * Gets the itemdata from the json additional
     * @return
     */
    public ItemData getItemData()
    {
        return ItemData.deserialize(getAdditional());
    }

    public User getUserFromData()
    {
        if (getData() > 0)
        {
            return this.um.getUser(getData());
        }
        throw new IllegalStateException("No User-Data in the data field: "+getData());
    }

    public JsonNode getAdditional()
    {
        return additional;
    }

    public ActionType getActionType()
    {
        return actionType;
    }

    public BlockVector3 getVector()
    {
        return location;
    }

    public TreeSet<LogEntry> getAttached()
    {
        return attached;
    }

    public boolean hasCauserUser()
    {
        if (getCauser() > 0)
        {
            if (this.getCauserUser() == null)
            {
                this.module.getLog().warn("LogEntry with invalid user!\n{}", this);
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean hasReplacedBlock()
    {
        if (getBlock() == null || getBlock().equals("AIR"))
        {
            return false;
        }
        return true;
    }

    public ContainerType getContainerTypeFromBlock()
    {
        return ContainerType.ofName(this.getBlock());
    }

    public boolean rollback(LogAttachment attachment, boolean force, boolean preview)
    {
        return this.actionType.canRollback() && this.actionType.rollback(attachment, this, force, preview);
    }

    public boolean redo(LogAttachment attachment, boolean force, boolean preview)
    {
        return this.actionType.canRedo() && this.actionType.redo(attachment, this, force, preview);
    }

    private Location bukkitLoc = null;

    public Location getLocation()
    {
        if (bukkitLoc == null)
        {
            bukkitLoc = new Location(this.world, this.location.x, this.location.y, this.location.z);
        }
        return bukkitLoc;
    }

    public void clearAttached()
    {
        this.attached.clear();
    }

    public static final Comparator<LogEntry> COMPARATOR = new Comparator<LogEntry>()
    {
        @Override
        public int compare(LogEntry o1, LogEntry o2)
        {
            return (int)(o1.getId().longValue() - o2.getId().longValue());
        }
    };

    // Direct Getter & Setter

    public void setId(UInteger value) {
        setValue(0, value);
    }

    public UInteger getId() {
        return (UInteger) getValue(0);
    }

    public void setTimestamp(Timestamp value) {
        setValue(1, value);
    }

    public Timestamp getTimestamp() {
        return (Timestamp) getValue(1);
    }

    public void setWorld(UInteger value) {
        setValue(2, value);
    }

    public UInteger getWorldID() {
        return (UInteger) getValue(2);
    }

    public void setX(Integer value) {
        setValue(3, value);
    }

    public Integer getX() {
        return (Integer) getValue(3);
    }

    public void setY(Integer value) {
        setValue(4, value);
    }

    public Integer getY() {
        return (Integer) getValue(4);
    }

    public void setZ(Integer value) {
        setValue(5, value);
    }

    public Integer getZ() {
        return (Integer) getValue(5);
    }

    public void setAction(UInteger value) {
        setValue(6, value);
    }

    public UInteger getAction() {
        return (UInteger) getValue(6);
    }

    public void setCauser(Long value) {
        setValue(7, value);
    }

    public Long getCauser() {
        return (Long) getValue(7);
    }

    public void setBlock(String value) {
        setValue(8, value);
    }

    public String getBlock() {
        return (String) getValue(8);
    }

    public void setData(Long value) {
        setValue(9, value);
    }

    public Long getData() {
        return (Long) getValue(9);
    }

    public void setNewblock(String value) {
        setValue(10, value);
    }

    public String getNewblock() {
        return (String) getValue(10);
    }

    public void setNewdata(Byte value) {
        setValue(11, value);
    }

    public Byte getNewdata() {
        return (Byte) getValue(11);
    }

    public void setAdditionaldata(String value) {
        setValue(12, value);
    }

    public String getAdditionaldata() {
        return (String) getValue(12);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public org.jooq.Record1<UInteger> key() {
        return (org.jooq.Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record13 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row13<UInteger, Timestamp, UInteger, Integer, Integer, Integer, UInteger, Long, String, Long, String, Byte, String> fieldsRow() {
        return (Row13) super.fieldsRow();
    }

    @Override
    public Row13<UInteger, Timestamp, UInteger, Integer, Integer, Integer, UInteger, Long, String, Long, String, Byte, String> valuesRow() {
        return (Row13) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_LOG_ENTRY.ID;
    }

    @Override
    public Field<Timestamp> field2() {
        return TABLE_LOG_ENTRY.DATE;
    }

    @Override
    public Field<UInteger> field3() {
        return TABLE_LOG_ENTRY.WORLD;
    }

    @Override
    public Field<Integer> field4() {
        return TABLE_LOG_ENTRY.X;
    }

    @Override
    public Field<Integer> field5() {
        return TABLE_LOG_ENTRY.Y;
    }

    @Override
    public Field<Integer> field6() {
        return TABLE_LOG_ENTRY.Z;
    }

    @Override
    public Field<UInteger> field7() {
        return TABLE_LOG_ENTRY.ACTION;
    }

    @Override
    public Field<Long> field8() {
        return TABLE_LOG_ENTRY.CAUSER;
    }

    @Override
    public Field<String> field9() {
        return TABLE_LOG_ENTRY.BLOCK;
    }

    @Override
    public Field<Long> field10() {
        return TABLE_LOG_ENTRY.DATA;
    }

    @Override
    public Field<String> field11() {
        return TABLE_LOG_ENTRY.NEWBLOCK;
    }

    @Override
    public Field<Byte> field12() {
        return TABLE_LOG_ENTRY.NEWDATA;
    }

    @Override
    public Field<String> field13() {
        return TABLE_LOG_ENTRY.ADDITIONALDATA;
    }

    @Override
    public UInteger value1() {
        return getId();
    }

    @Override
    public Timestamp value2() {
        return getTimestamp();
    }

    @Override
    public UInteger value3() {
        return getWorldID();
    }

    @Override
    public Integer value4() {
        return getX();
    }

    @Override
    public Integer value5() {
        return getY();
    }

    @Override
    public Integer value6() {
        return getZ();
    }

    @Override
    public UInteger value7() {
        return getAction();
    }

    @Override
    public Long value8() {
        return getCauser();
    }

    @Override
    public String value9() {
        return getBlock();
    }

    @Override
    public Long value10() {
        return getData();
    }

    @Override
    public String value11() {
        return getNewblock();
    }

    @Override
    public Byte value12() {
        return getNewdata();
    }

    @Override
    public String value13() {
        return getAdditionaldata();
    }
}
