package de.cubeisland.cubeengine.core.attachment;

import de.cubeisland.cubeengine.core.module.Module;

import java.util.Set;

public interface AttachmentHolder<T extends Attachment>
{
    <A extends T> A attach(Class<A> attachment, Module module);
    <A extends T> A attachOrGet(Class<A> attachment, Module module);
    <A extends T> A get(Class<A> attachment);
    <A extends T> boolean has(Class<A> attachment);
    <A extends T> A detach(Class<A> attachment);
    Set<T> getAll();
    void detachAll(Module module);
    void detachAll();
}
