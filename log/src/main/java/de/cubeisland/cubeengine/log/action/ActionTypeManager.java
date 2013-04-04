package de.cubeisland.cubeengine.log.action;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gnu.trove.set.hash.TIntHashSet;

public class ActionTypeManager
{
    private Map<Class<? extends ActionType>,ActionType> registeredActionTypes = new ConcurrentHashMap<Class<? extends ActionType>, ActionType>();
    private TIntHashSet registeredIds = new TIntHashSet();

    public void registerActionType(ActionType type)
    {
        if (registeredActionTypes.containsKey(type.getClass()))
        {
            //TODO warning
            return;
        }
        if (registeredIds.add(type.actionTypeID))
        {
            registeredActionTypes.put(type.getClass(),type);
            type.initialize();
            //TODO debug output registered
        }
        else
        {
            throw new ActionTypeDuplicateException();
        }
    }

    public <AT extends ActionType> AT getActionType(Class<AT> actionTypeClass)
    {
        return (AT)this.registeredActionTypes.get(actionTypeClass);
    }
}
