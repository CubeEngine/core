package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.LoggerConfig;

public class InteractionConfig extends LoggerConfig
{
    public InteractionConfig()
    {
        super(false);
    }

    @Option("log-pressureplate")
    public boolean logPressurePlate = false;
    @Option("log-door")
    public boolean logDoor = false;
    @Option("log-trapDoor")
    public boolean logTrapDoor = false;
    @Option("log-fenceGate")
    public boolean logfenceGate = false;
    @Option("log-lever")
    public boolean logLever = false;
    @Option("log-button")
    public boolean logButtons = false;
    @Option("log-cake")
    public boolean logCake = false;
    @Option("log-noteblock")
    public boolean logNoteBlock = false;
    @Option("log-diode")
    public boolean logDiode = false;

    @Override
    public String getName()
    {
        return "interact";
    }
}
