package de.cubeisland.cubeengine.core.attachment;

import de.cubeisland.cubeengine.core.module.Module;

public interface AttachmentHolder<T extends Attachment>
{
    T addAttachment(Class<T> attachment);
    T addOrGetAttachment(Class<T> attachment);
    T getAttachment(Class<T> attachment);
    boolean hasAttachment(Class<T> attachment);
    T removeAttachment(Class<T> attachment);
    void clearAttachments(Module module);
    void clearAttachments();
}
