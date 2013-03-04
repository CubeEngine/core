package de.cubeisland.cubeengine.core.attachment;

import de.cubeisland.cubeengine.core.module.Module;

public abstract class Attachment<T extends AttachmentHolder>
{
    private T holder;
    private Module module;

    public final void attachTo(T holder, Module module)
    {
        if (this.holder != null)
        {
            throw new IllegalStateException("This attachment has already been attached!");
        }
        this.holder = holder;
        this.module = module;
        this.onAttach();
    }

    public T getHolder()
    {
        return this.holder;
    }

    public final Module getModule()
    {
        return this.module;
    }

    public void onAttach()
    {}

    public void onDetach()
    {}
}
