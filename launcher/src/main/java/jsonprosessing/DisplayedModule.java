package jsonprosessing;

import javafx.scene.control.CheckBox;

/**
 * Created by Tim on 07.10.2015.
 */
public class DisplayedModule
{
    CheckBox cb;
    String name;
    String id;
    String from;

    public DisplayedModule(String name, String id, String from)
    {
        this.name = name;
        this.id = id;
        this.from = from;
        cb = new CheckBox(name);
    }



    public String getDownloadLocation()
    {
        return from;
    }

    public CheckBox getCheckBox()
    {
        return cb;
    }
}
