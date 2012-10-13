package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.log.listeners.*;

public enum LogAction
{
    BLOCKPLACE(BlockPlace.class),
    PLAYER_BLOCKBREAK(BlockBreak.class),
    SIGNTEXT(SignChange.class),
    EXPLOSION_TNT(Explosion.class),
    EXPLOSION_CREEPER(Explosion.class),
    EXPLOSION_GHASTFIREBALL(Explosion.class),
    EXPLOSION_ENDERDRAGON(Explosion.class),
    EXPLOSION_MISC(Explosion.class),
    FIRE(BlockBurn.class),
    ENDERMEN(Enderman.class),
    LEAVESDECAY(LeavesDecay.class),
    LAVAFLOW(FluidFlow.class),
    WATERFLOW(FluidFlow.class),
    DISPENSERACCESS(ContainerAccess.class),
    CHESTACCESS(ContainerAccess.class),
    FURNACEACCESS(ContainerAccess.class),
    BREWINGSTANDACCESS(ContainerAccess.class),
    KILL(Kill.class),
    CHAT(Chat.class),
    COMMAND(Chat.class),
    CONSOLE(Chat.class),
    SNOWFORM(BlockForm.class),
    SNOWFADE(BlockFade.class),
    ICEFORM(BlockForm.class),
    ICEFADE(BlockFade.class),
    DOORINTERACT(PlayerInteract.class),
    SWITCHINTERACT(PlayerInteract.class),
    CAKEEAT(PlayerInteract.class),
    NOTEBLOCKINTERACT(PlayerInteract.class),
    DIODEINTERACT(PlayerInteract.class),
    NATURALSTRUCTUREGROW(StructureGrow.class),
    BONEMEALSTRUCTUREGROW(StructureGrow.class),;
    private final Class<? extends LogListener> listenerClass;

    private LogAction(Class<? extends LogListener> listenerClass)
    {
        this.listenerClass = listenerClass;
    }

    public Class<? extends LogListener> getListenerClass()
    {
        return listenerClass;
    }
}