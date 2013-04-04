package de.cubeisland.cubeengine.log.action;

import java.sql.Timestamp;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.QueuedLog;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ActionType
{
    public final int actionTypeID;
    public final Log logModule;
    protected final WorldManager wm;
    protected final UserManager um;
    protected final ObjectMapper om;
    protected final ActionTypeManager manager;
    public final String name;

    protected ActionType(Log module, int actionTypeID, String name)
    {
        this.logModule = module;
        this.wm = module.getCore().getWorldManager();
        this.um = module.getCore().getUserManager();
        //TODO register ActionType
        //show error if value is already used
        this.actionTypeID = actionTypeID;
        this.om = this.logModule.getObjectMapper();
        this.manager = null; //TODO get from Log Module
        this.name = name;
    }

    public void queueLog(Location location, Entity causer, String block, Long data, String newBlock, Byte newData, String additionalData)
    {
        long worldID = this.wm.getWorldId(location.getWorld());
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
        //TODO add to queryManagerQueue
    }

    public abstract void initialize();

    public boolean isActive(World world)
    {
        return true;
    }
}
