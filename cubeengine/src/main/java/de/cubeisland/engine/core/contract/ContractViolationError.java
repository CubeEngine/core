package de.cubeisland.engine.core.contract;

public class ContractViolationError extends Error
{
    public ContractViolationError(String message)
    {
        super(message);
    }
}
