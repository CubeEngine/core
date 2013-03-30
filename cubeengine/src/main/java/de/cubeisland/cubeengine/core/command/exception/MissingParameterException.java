package de.cubeisland.cubeengine.core.command.exception;

public class MissingParameterException extends CommandException
{
    public MissingParameterException(String paramName)
    {
        super(paramName);
    }

    public String getParamName()
    {
        return this.getMessage();
    }
}
