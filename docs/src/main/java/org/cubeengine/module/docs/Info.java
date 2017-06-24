package org.cubeengine.module.docs;

import org.cubeengine.reflect.codec.yaml.ReflectedYaml;

import java.util.ArrayList;
import java.util.List;

public class Info extends ReflectedYaml {

    public String version;
    public String sourceVersion;
    public String buildTimeStamp;
    public List<String> features = new ArrayList<>();
}
