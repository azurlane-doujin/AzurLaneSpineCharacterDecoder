package com.decoder.jacky.SkeletonData;

import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;

public class BasicData {
    final float scale = 1.0f;
    public String name = null;
    boolean useList = false;

    public BasicData(String name) {
        this.name = name;
    }

    static public BasicData loadData(DataInput input, boolean nonessential, SkeletonData skeletonData, int count) {
        return null;
    }

    public void buildJson(JsonBuilder.List parent) {
        if (useList) {
            JsonBuilder.List list = parent.addList();
            buildJsonList(list);
            parent.insert(list);
        } else {
            JsonBuilder.Dict dict = parent.addDict();
            buildJsonDict(dict);
            parent.insert(dict);
        }
    }

    public void buildJson(JsonBuilder.Dict parent) {
        if (useList) {
            JsonBuilder.List list = parent.addKeyList(name);
            buildJsonList(list);
            parent.insert(list);
        } else {
            JsonBuilder.Dict dict = parent.addKeyDict(name);
            buildJsonDict(dict);
            parent.insert(dict);
        }
    }

    public void buildJsonDict(JsonBuilder.Dict dict) {
    }

    public void buildJsonList(JsonBuilder.List list) {
    }

    @Override
    public String toString() {
        return name;
    }
}
