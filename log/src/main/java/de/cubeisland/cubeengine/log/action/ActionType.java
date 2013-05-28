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
package de.cubeisland.cubeengine.log.action;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAttachment;
import de.cubeisland.cubeengine.log.storage.LogEntry;
import de.cubeisland.cubeengine.log.storage.LogManager;
import de.cubeisland.cubeengine.log.storage.QueryParameter;
import de.cubeisland.cubeengine.log.storage.QueuedLog;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ActionType
{
    private long actionTypeID;

    protected Log logModule;
    protected UserManager um;
    protected ObjectMapper om;
    protected ActionTypeManager manager;
    protected LogManager lm;

    /**
     * Queues in a log
     *
     * @param location
     * @param causer
     * @param block
     * @param data
     * @param newBlock
     * @param newData
     * @param additionalData
     */
    public void queueLog(Location location, Entity causer, String block, Long data, String newBlock, Byte newData, String additionalData)
    {
        long worldID = this.logModule.getCore().getWorldManager().getWorldId(location.getWorld());
        Long causerID;
        if (causer == null)
        {
            causerID = 0L;
        }
        else if (causer instanceof Player)
        {
            causerID = this.um.getExactUser(((Player)causer).getName()).key;
        }
        else
        {
            causerID = -1L * causer.getType().getTypeId();
        }
        this.queueLog(worldID,location.getBlockX(),location.getBlockY(),location.getBlockZ(),causerID,block,data,newBlock,newData,additionalData);
    }

    public void queueLog(long worldID, int x, int y, int z, Long causer, String block, Long data, String newBlock, Byte newData, String additionalData)
    {
        QueuedLog log = new QueuedLog(new Timestamp(System.currentTimeMillis()),worldID,x,y,z,this.actionTypeID,causer,block,data,newBlock,newData,additionalData);
        this.lm.queueLog(log);
    }

    /**
     * Register your events here
     */
    public final void initialize(Log module)
    {
        this.logModule = module;
        this.um = module.getCore().getUserManager();
        this.om = module.getObjectMapper();
        this.manager = module.getActionTypeManager();
        this.lm = module.getLogManager();
        if (this.getID() != -1)
        {
            for (Category type : this.getCategories())
            {
                type.registerActionType(this);
            }
        }
        this.enable();
    }

    public abstract String getName();
    public abstract boolean canRollback();
    protected abstract EnumSet<Category> getCategories();
    public abstract void enable();

    /**
     * Returns whether the action is active in given world or not
     *
     * @param world
     * @return
     */
    public abstract boolean isActive(World world);

    /**
     * Shows the user the logentry from given queryparams
     *
     * @param user
     * @param params
     * @param logEntry
     */
    public abstract void showLogEntry(User user, QueryParameter params, LogEntry logEntry);

    /**
     * Returns true if the given log-entries can be put together to minimize output
     *
     * @param logEntry
     * @param other
     * @return
     */
    public abstract boolean isSimilar(LogEntry logEntry, LogEntry other);

    public void setID(long id)
    {
        this.actionTypeID = id;
    }

    public long getID()
    {
        return this.actionTypeID;
    }

    public boolean rollback(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        if (this.canRollback())
        {
            throw new UnsupportedOperationException("Not yet implemented! " + logEntry.actionType.getName());
        }
        return false;
    }

    public static enum Category
    {
        ALL("all"),
        /**
         * All actions with a possible player involved
         */
        PLAYER("player"),
        /**
         * All actions with a block involved
         */
        BLOCK("block"),
        /**
         * All actions with ItemStacks involved
         */
        ITEM("item"),
        /**
         * All actions with inventories involved
         */
        INVENTORY("inventory"),
        /**
         * All actions with entities excluding block changes by an entity
         */
        ENTITY("entity"),
        /**
         * All block actions with a possible living entity as causer
         */
        BLOCK_ENTITY("block-entity"),
        /**
         * possibly environmental actions such as grass growing naturally etc.
         */
        ENVIRONEMENT("environement"),
        /**
         * All actions of the death of an living entity or player
         */
        KILL("kill"),
        /**
         * All block-actions involving an explosion (tnt-prime too)
         */
        EXPLOSION("explosion"),
        /**
         * All actions involving lava or water flows
         */
        FLOW("flow"),
        /**
         * All actions involving fire ignition/spread
         */
        IGNITE("ignite"),
        /**
         * All actions involving fire excluding blocks indirectly broken by block-burns
         */
        FIRE("fire"),
        /**
         * All actions involving buckets
         */
        BUCKET("bucket"),
        /**
         * lava-bucket AND water-bucket
         */
        BUCKET_EMPTY("bucket-empty"),
        /**
         * All actions involving vehicles
         */
        VEHICLE("vehicle"),
        /**
         * All actions involving
         */
        SPAWN("spawn"),
        ;

        private static Map<String, Category> categories = new HashMap<String, Category>();

        static
        {
            for (Category category : Category.values())
            {
                categories.put(category.name, category);
            }
        }

        private HashSet<ActionType> actionTypes = new HashSet<ActionType>();

        public final String name;

        private Category(String name)
        {
            this.name = name;
        }

        public void registerActionType(ActionType actionType)
        {
            this.actionTypes.add(actionType);
        }

        public Set<ActionType> getActionTypes()
        {
            return Collections.unmodifiableSet(actionTypes);
        }

        public static Category match(String actionString)
        {
            return categories.get(actionString);
        }
    }
}
