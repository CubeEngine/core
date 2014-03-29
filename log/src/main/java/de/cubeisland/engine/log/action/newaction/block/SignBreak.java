package de.cubeisland.engine.log.action.newaction.block;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a sign breaking
 */
public class SignBreak extends BlockBreak
{
    private String[] oldLines;

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return super.translateAction(user);
        }
        String delim = ChatFormat.GREY + " | " + ChatFormat.GOLD;
        return super.translateAction(user) + "\n" +
            user.getTranslation(POSITIVE, "    with {input#signtext} written on it", StringUtils
                .implode(delim, this.oldLines));
    }

    public void setLines(String[] lines)
    {
        this.oldLines = lines;
    }
}
