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
package de.cubeisland.engine.log.action;

import java.sql.Timestamp;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.storage.ActionTypeModel;
import de.cubeisland.engine.log.storage.LogEntry;
import de.cubeisland.engine.log.storage.LogManager;
import de.cubeisland.engine.log.storage.QueryParameter;
import de.cubeisland.engine.log.storage.QueuedLog;
import de.cubeisland.engine.log.storage.ShowParameter;

public abstract class ActionType
{
    private ActionTypeModel model;

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
            causerID = this.um.getExactUser(((Player)causer).getName()).getId();
        }
        else
        {
            // TODO This is not ideal as some ids are -1 in bukkitcode
            causerID = -1L * Math.abs(causer.getType().getTypeId());
        }
        this.queueLog(worldID,location.getBlockX(),location.getBlockY(),location.getBlockZ(),causerID,block,data,newBlock,newData,additionalData);
    }

    public void queueLog(long worldID, int x, int y, int z, Long causer, String block, Long data, String newBlock, Byte newData, String additionalData)
    {
        QueuedLog log = new QueuedLog(new Timestamp(System.currentTimeMillis()),worldID,x,y,z,this.model.getId().longValue(),causer,block,data,newBlock,newData,additionalData);
        this.lm.queueLog(log);
    }

    /**
     * Register your events here
     */
    public final void initialize(Log module, ActionTypeManager manager)
    {
        this.logModule = module;
        this.um = module.getCore().getUserManager();
        this.om = module.getObjectMapper();
        this.manager = manager;
        this.lm = module.getLogManager();
        if (this.getModel() != null)
        {
            for (ActionTypeCategory category : this.getCategories())
            {
                category.registerActionType(this);
            }
        }
        this.enable();
    }

    public abstract String getName();
    public abstract boolean canRollback();
    public abstract boolean canRedo();
    protected abstract Set<ActionTypeCategory> getCategories();
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
     * @param show
     */
    public abstract void showLogEntry(User user, QueryParameter params, LogEntry logEntry, ShowParameter show);

    /**
     * Returns true if the given log-entries can be put together to minimize output
     *
     * @param logEntry
     * @param other
     * @return
     */
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.getActionType() == other.getActionType();
    }

    public void setModel(ActionTypeModel model)
    {
        this.model = model;
    }

    public ActionTypeModel getModel()
    {
        return this.model;
    }

    public boolean rollback(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        if (this.canRollback())
        {
            attachment.getHolder().sendTranslated("&4Encountered an unimplemented LogAction-Rollback: &6%s", logEntry.getActionType().getName());
            throw new UnsupportedOperationException("Not yet implemented! " + logEntry.getActionType().getName());
        }
        return false;
    }

    public boolean redo(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        if (this.canRedo())
        {
            attachment.getHolder().sendTranslated("&4Encountered an unimplemented LogAction-Redo: &6%s", logEntry.getActionType().getName());
            throw new UnsupportedOperationException("Not yet implemented! " + logEntry.getActionType().getName());
        }
        return false;
    }

    /**
     * Returns whether this actionType can have more than one changes at a single location.
     * <p>e.g.: Block-Changes like block-break or block-place will return false
     * <p>Container-Transactions, mob-spawns or kills will return true
     * <p>default is true override to change this!</p>
     *
     * @return true if this log-action can stack
     */
    public boolean isStackable()
    {
        return true;
    }

    /**
     * Returns whether this actionType is referring to a specific Block not a location
     * <p>This is mostly true for block-changes and container-transactions
     * <p>default is false override to change this!</p>
     *
     * @return true if this log-action is block-bound
     */
    public boolean isBlockBound()
    {
        return false;
    }

    public abstract boolean needsModel();
}
