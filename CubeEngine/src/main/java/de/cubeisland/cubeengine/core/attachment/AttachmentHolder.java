package de.cubeisland.cubeengine.core.attachment;

public interface AttachmentHolder<T extends Attachment>
{
    <A extends T> A attach(Class<A> attachment);
    <A extends T> A attachOrGet(Class<A> attachment);
    <A extends T> A get(Class<A> attachment);
    <A extends T> boolean has(Class<A> attachment);
    <A extends T> A detach(Class<A> attachment);
    void detachAll();
}
