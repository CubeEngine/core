package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.Logger;

import java.util.HashMap;
import java.util.Map;

public class LoggerManager {

    private final Log module;

    public Map<Class<? extends Logger>,Logger> loggerClassMap = new HashMap<Class<? extends Logger>, Logger>();

    public LoggerManager(Log module) {
        this.module = module;

        // registering loggers:
        this.createLogger(BlockBreakLogger.class);
        this.createLogger(BlockBurnLogger.class);
        this.createLogger(BlockDecayLogger.class);
        this.createLogger(BlockExplosionLogger.class);
        this.createLogger(BlockFadeLogger.class);
        this.createLogger(BlockFluidFlowLogger.class);
        this.createLogger(BlockFormLogger.class);
        this.createLogger(BlockGrowLogger.class);
        this.createLogger(BlockPlaceLogger.class);
        this.createLogger(ChatLogger.class);
        this.createLogger(ContainerLogger.class);
        this.createLogger(EndermanLogger.class);
        this.createLogger(InteractionLogger.class);
        this.createLogger(KillLogger.class);
        this.createLogger(SignChangeLogger.class);
    }

    public <T extends Logger> void createLogger(Class<T> loggerClass)
    {
        try {
            Logger  logger = loggerClass.getConstructor(Log.class).newInstance(this.module);
            this.loggerClassMap.put(loggerClass,logger);
        } catch (Exception ex) {
            throw  new IllegalStateException("Could not create logger-instance!",ex);
        }
    }

    public <T extends Logger> T getLogger(Class<T> loggerClass)
    {
        return (T)this.loggerClassMap.get(loggerClass);
    }
}

