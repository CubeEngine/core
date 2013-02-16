package de.cubeisland.cubeengine.core.command.exception;

/**
 * This exception is thrown when a user is not allowed to perform an action.
 * Use denyAccess to throw an exception insinde a command. The exception will be caught.
 */
public class PermissionDeniedException extends CommandException
{}
