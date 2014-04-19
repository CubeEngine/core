package de.cubeisland.engine.log.converter;

import org.bukkit.Note;

import de.cubeisland.engine.reflect.codec.ConverterManager;
import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.ByteNode;
import de.cubeisland.engine.reflect.node.Node;

public class NoteConverter implements Converter<Note>
{
    @Override
    public Node toNode(Note object, ConverterManager manager) throws ConversionException
    {
        return new ByteNode(object.getId());
    }

    @Override
    public Note fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof ByteNode)
        {
            return new Note(((ByteNode)node).getValue().intValue());
        }
        throw ConversionException.of(this, node, "Note is not a ByteNode!");
    }
}
