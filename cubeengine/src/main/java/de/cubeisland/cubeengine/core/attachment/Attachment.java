package de.cubeisland.cubeengine.core.attachment;

import de.cubeisland.cubeengine.core.module.Module;

public abstract class Attachment<T extends AttachmentHolder>
{
    private Module module;
    private T holder;

    public final void attachTo(Module module, T holder)
    {
        this.module = module;
        if (this.holder != null)
        {
            throw new IllegalStateException("This attachment has already been attached!");
        }
        this.holder = holder;
        this.onAttach();
    }

    public Module getModule()
    {
        return this.module;
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
