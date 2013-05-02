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
package de.cubeisland.cubeengine.log.storage;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.ActionType;
import de.cubeisland.cubeengine.log.action.logaction.container.ContainerType;

import com.fasterxml.jackson.databind.JsonNode;

public class LogEntry implements Comparable<LogEntry>
{
    private final Log module;
    private final UserManager um;

    public final long entryID;
    public final Timestamp timestamp;
    public final ActionType actionType;
    public final World world;
    public final BlockVector3 location;
    public final long causer;
    public final String block;
    public final Long data;
    public final String newBlock;
    public final Integer newData;
    public final JsonNode additional;

    private TreeSet<LogEntry> attached = new TreeSet<LogEntry>();

    public LogEntry(Log module, long entryID, Timestamp timestamp, int action, long worldId, int x, int y, int z,
                    long causer, String block, Long data, String newBlock, Integer newData, String additionalData)
    {
        this.module = module;
        this.um = module.getCore().getUserManager();

        this.entryID = entryID;
        this.timestamp = timestamp;
        this.actionType = this.module.getActionTypeManager().getActionType(action);
        this.world = module.getCore().getWorldManager().getWorld(worldId);
        this.location = new BlockVector3(x,y,z);
        this.causer = causer;
        this.block = block;
        this.data = data;
        this.newBlock = newBlock;
        this.newData = newData;
        this.additional = additionalData == null ? null : this.readJson(additionalData);
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

    @Override
    public int compareTo(LogEntry o)
    {
        return (int)(this.entryID - o.entryID);
    }

    public void attach(LogEntry next)
    {
        this.attached.add(next);
    }

    public boolean isSimilar(LogEntry other)
    {
        if (this.actionType != other.actionType)
        {
            return false;
        }
        return this.actionType.isSimilar(this,other);
    }

    public boolean hasAttached()
    {
        return !this.attached.isEmpty();
    }

    public User getCauserUser()
    {
        if (this.hasCauserUser())
        {
            return this.um.getUser(this.causer);
        }
        return null;
    }

    public BlockData getOldBlock()
    {
        return new BlockData(Material.getMaterial(this.block),this.data.byteValue());
    }

    public BlockData getNewBlock()
    {
        return new BlockData(Material.getMaterial(this.newBlock),this.newData.byteValue());
    }

    public BlockData getMaterialFromNewBlock()
    {
        return new BlockData(Material.getMaterial(this.newBlock),(byte)0);
    }

    public EntityData getCauserEntity()
    {
        if (this.hasCauserEntity())
        {
            return new EntityData(EntityType.fromId((int)-causer),this.additional);
        }
        else
        {
            return null;
        }
    }

    public boolean hasCauserEntity()
    {
        return causer < 0;
    }

    /**
     * Gets the Entity represented by the negative value in data.
     * @return
     */
    public EntityData getEntityFromData()
    {
        return new EntityData(EntityType.fromId(-data.intValue()),this.additional);
    }

    /**
     *
     * Gets the itemdata from the json additional
     * @return
     */
    public ItemData getItemData()
    {
        return ItemData.deserialize(additional);
    }

    public User getUserFromData()
    {
        if (data > 0)
        {
            return this.um.getUser(data);
        }
        throw new IllegalStateException("No User-Data in the data field: "+data);
    }

    public JsonNode getAdditional()
    {
        return additional;
    }

    public Timestamp getTimestamp()
    {
        return this.timestamp;
    }

    public TreeSet<LogEntry> getAttached()
    {
        return attached;
    }

    public boolean hasCauserUser()
    {
        return causer > 0;
    }

    public boolean hasReplacedBlock()
    {
        if (block == null || block.equals("AIR"))
        {
            return false;
        }
        return true;

    }

    public ContainerType getContainerTypeFromBlock()
    {
        return ContainerType.ofName(this.block);
    }

    public Long getData()
    {
        return data;
    }

    public Integer getNewData()
    {
        return newData;
    }
}
