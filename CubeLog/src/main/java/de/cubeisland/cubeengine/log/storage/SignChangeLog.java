package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import java.util.List;
import org.bukkit.block.BlockState;

@Entity(name = "signchangelog")
public class SignChangeLog extends AbstractPositionLog
{
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String oldLine1;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String oldLine2;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String oldLine3;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String oldLine4;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String newLine1;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String newLine2;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String newLine3;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String newLine4;

    @DatabaseConstructor
    public SignChangeLog(List<Object> args) throws ConversionException
    {
        super(args.subList(0, 7));
        this.oldLine1 = args.get(8).toString();
        this.oldLine2 = args.get(9).toString();
        this.oldLine3 = args.get(10).toString();
        this.oldLine4 = args.get(11).toString();

        this.newLine1 = args.get(12).toString();
        this.newLine2 = args.get(13).toString();
        this.newLine3 = args.get(14).toString();
        this.newLine4 = args.get(15).toString();
    }

    public SignChangeLog(int userID, BlockState state, String[] oldLines, String[] newLines)
    {
        super(userID, state.getLocation());

        this.oldLine1 = oldLines[0];
        this.oldLine2 = oldLines[1];
        this.oldLine3 = oldLines[2];
        this.oldLine4 = oldLines[3];

        this.newLine1 = newLines[0];
        this.newLine2 = newLines[1];
        this.newLine3 = newLines[2];
        this.newLine4 = newLines[3];
    }
}