package de.cubeisland.cubeengine.shout.Exceptions;

public class ShoutException extends Exception{

	public ShoutException()
	{
		super();
	}

	public ShoutException(String message)
	{
    	super(message);
	}

	public ShoutException(Throwable cause)
	{
    	super(cause);
	}

	public ShoutException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
