package de.cubeisland.engine.log.action.newaction.block.player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player changing the text on a sign
 */
public class PlayerSignChange extends PlayerBlockActionType<PlayerBlockListener>
{
    // return "sign-change";
    // return this.lm.getConfig(world).block.SIGN_CHANGE_enable;

    public String[] oldLines;
    public String[] newLines;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return false;
    }

    @Override
    public String translateAction(User user)
    {
        String delim = ChatFormat.GREY + " | " + ChatFormat.GOLD;
        if (oldLines == null || oldLines.length == 0)
        {
            return user.getTranslation(POSITIVE, "{user} wrote {input#signtext} on a sign",
                                       this.player.name, StringUtils.implode(delim, newLines));
        }
        return user.getTranslation(POSITIVE, "{user} wrote {input#signtext} on a sign",
                                   this.player.name, StringUtils.implode(delim, newLines)) + "\n" +
               user.getTranslation(POSITIVE, "    The old signtext was {input#signtext}",
                                   StringUtils.implode(delim, oldLines));
    }

    public void setNewLines(String[] newLines)
    {
        this.newLines = newLines;
    }

    public void setOldLines(String[] oldLines)
    {
        this.oldLines = oldLines;
    }
}
