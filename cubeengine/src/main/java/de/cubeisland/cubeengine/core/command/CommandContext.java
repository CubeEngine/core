package de.cubeisland.cubeengine.core.command;

import java.util.LinkedList;
import java.util.Stack;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.user.User;

public interface CommandContext
{
    /**
     * Returns the core
     *
     * @return the core
     */
    Core getCore();

    /**
     * Returns the cube command that was executed
     *
     * @return the command
     */
    CubeCommand getCommand();

    /**
     * Checks whether the command sender is compatible with the given class
     *
     * @param type the class to check against
     * @return true is the command sender is assignment compatible with the given class
     */
    boolean isSender(Class<? extends CommandSender> type);

    /**
     * Returns the CommandSender
     *
     * @return the command sender
     */
    CommandSender getSender(); // type inference is our friend!

    /**
     * Returns the label that was used to run this command
     *
     * @return the label
     */
    String getLabel();

    /**
     * Returns all labels that have been used to execute this command
     *
     * @return a stack of labels
     */
    Stack<String> getLabels();

    /**
     * This method is a proxy to CommandSender.sendMessage(String)
     *
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * This method is a proxy to CommandSender.sendTranslated(String, Object...)
     *
     * @param message the mesage to send
     * @param args the args
     */
    void sendTranslated(String message, Object... args);

    /**
     * Returns the number og arguments given to the command
     *
     * @return the number of arguments. always positive
     */
    int getArgCount();

    /**
     * Checks whether any args where given to this command.
     * This equals (getArgCount() > 0)
     *
     * @return true if there are arguments given
     */
    boolean hasArgs();

    /**
     * Returns a linked list of the arguments
     *
     * @return the arguments
     */
    LinkedList<String> getArgs();

    /**
     * Checks whether the given index is available in the args list
     *
     * @param i the index to check
     * @return true if the index is available (getArgCount() > i)
     */
    boolean hasArg(int i);

    /**
     * The method returns a arg as a specific type
     *
     * @param i the index
     * @param type the type class
     * @param <T> the type
     * @return the converted arg value or null
     */
    <T> T getArg(int i, Class<T> type);

    /**
     * The method returns a arg as a specific type
     *
     * @param i the index
     * @param type the type class
     * @param <T> the type
     * @return the converted arg value or the default value
     */
    <T> T getArg(int i, Class<T> type, T def);

    /**
     * This method returns the raw arg string at the given index
     * @param i the index
     * @return the raw string value
     */
    String getString(int i);

    /**
     * This method returns the raw arg string at the given index or the default value
     * @param i the index
     * @param def the default value
     * @return the raw arg string or the default value
     */
    String getString(int i, String def);

    /**
     * This method aggregates all string from the given index
     *
     * @param from the index to start from
     * @return the aggregated string
     */
    String getStrings(int from);

    /**
     * Returns the arg at the given index as a user.
     * This is the same as getArg(i, User.class)
     *
     * @param i the index
     * @return a user or null
     */
    User getUser(int i);
}
