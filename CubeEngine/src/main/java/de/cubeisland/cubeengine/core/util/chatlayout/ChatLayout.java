package de.cubeisland.cubeengine.core.util.chatlayout;

public interface ChatLayout<T>
{
    public void setData(T data);
    public T getData();
    public String[] compile();
}
