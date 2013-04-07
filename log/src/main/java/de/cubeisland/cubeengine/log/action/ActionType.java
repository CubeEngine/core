package de.cubeisland.cubeengine.log.action;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.log.Log;
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
            causerID = this.um.getExactUser((Player)causer).key;
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

    public static enum Category
    {
        ALL,
        PLAYER,
        BLOCK,
        ITEM,
        INVENTORY,
        ENTITY,
        ENVIRONEMENT,
        KILL;

        private HashSet<ActionType> actionTypes = new HashSet<ActionType>();

        public void registerActionType(ActionType actionType)
        {
            this.actionTypes.add(actionType);
        }

        public Set<ActionType> getActionTypes()
        {
            return Collections.unmodifiableSet(actionTypes);
        }
    }
}
