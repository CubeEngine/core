package de.cubeisland.engine.core.contract;

public abstract class Contract
{
    public static void expect(boolean constraint)
    {
        expect(constraint, null);
    }

    public static void expect(boolean constraint, String message)
    {
        if (!constraint)
        {
            if (message == null)
            {
                message = "An API contract was violated!";
            }
            throw new ContractViolationError(message);
        }
    }

    public static void expectNotNull(Object object)
    {
        expectNotNull(object, "A not-null constraint was violated!");
    }

    public static void expectNotNull(Object object, String message)
    {
        expect(object != null, message);
    }
}
