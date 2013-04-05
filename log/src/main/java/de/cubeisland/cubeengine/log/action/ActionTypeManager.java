package de.cubeisland.cubeengine.log.action;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.log.Log;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

public class ActionTypeManager
{
    private Map<Class<? extends ActionType>,ActionType> registeredActionTypes = new ConcurrentHashMap<Class<? extends ActionType>, ActionType>();
    private TIntObjectHashMap<ActionType> registeredIds = new TIntObjectHashMap<ActionType>();
    private final Log module;

    public ActionTypeManager(Log module)
    {
        this.module = module;
    }

    public ActionTypeManager registerActionType(ActionType type)
    {
        if (registeredActionTypes.containsKey(type.getClass()))
        {
            this.module.getLog().log(LogLevel.WARNING,"ActionTypeID already in use: " + type.actionTypeID +
                        " ("+registeredActionTypes.get(type.actionTypeID).name+") new:" + type.name);
            return this;
        }
        if (type.actionTypeID == -1)
        {
            type.initialize();
            return this;
        }
        if (registeredIds.containsKey(type.actionTypeID))
        {
            registeredIds.put(type.actionTypeID,type);
            registeredActionTypes.put(type.getClass(),type);
            type.initialize();
            this.module.getLog().log(LogLevel.DEBUG,"ActionType registered: " + type.actionTypeID + " " + type.name);
            return this;
        }
        else
        {
            throw new ActionTypeDuplicateException(type);
        }
    }

    public <AT extends ActionType> AT getActionType(Class<AT> actionTypeClass)
    {
        return (AT)this.registeredActionTypes.get(actionTypeClass);
    }

    public ActionType getActionType(int id)
    {
        return this.registeredIds.get(id);
    }
}
