package de.cubeisland.cubeengine.core.attachment;

import de.cubeisland.cubeengine.core.module.Module;

public interface AttachmentHolder<T extends Attachment>
{
    T attach(Class<T> attachment, Module module);
    T attachOrGet(Class<T> attachment, Module module);
    T get(Class<T> attachment);
    boolean has(Class<T> attachment);
    T detach(Class<T> attachment);
    void detach(Module module);
    void detach();
}
