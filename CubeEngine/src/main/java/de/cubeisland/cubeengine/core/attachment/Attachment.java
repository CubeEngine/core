package de.cubeisland.cubeengine.core.attachment;

import de.cubeisland.cubeengine.core.module.Module;

public interface Attachment<T extends AttachmentHolder>
{
    Module getModule();
    void onAttach(T holder);
    void onDetach();
}
