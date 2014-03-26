package de.cubeisland.engine.log.action.newaction;

import java.util.UUID;

public abstract class UserBlockActionType<ListenerType>
{
    public UUID playerUUID;
    public String playerName; // TODO offlineplayer?
}
