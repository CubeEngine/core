package de.cubeisland.cubeengine.core.attachment;

public abstract class Attachment<T extends AttachmentHolder>
{
    private T holder;

    public final void attachTo(T holder)
    {
        if (this.holder != null)
        {
            throw new IllegalStateException("This attachment has already been attached!");
        }
        this.holder = holder;
        this.onAttach();
    }

    public T getHolder()
    {
        return this.holder;
    }

    public void onAttach()
    {}

    public void onDetach()
    {}
}
