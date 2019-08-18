package com.decoder.jacky.SkeletonData;

import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;

import java.io.IOException;

public class EventData extends BasicData {
    public final String name;
    private int intValue;
    private float floatValue;
    private String stringValue;

    private EventData(String name) {
        super(name);
        this.name = name;
    }

    static public EventData loadData(DataInput input, SkeletonData skeletonData) {
        try {
            EventData data = new EventData(input.readString());
            data.intValue = input.readInt(false);
            data.floatValue = input.readFloat();
            data.stringValue = input.readString();
            skeletonData.events.add(data);

            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void buildJsonDict(JsonBuilder.Dict event) {
        event.addKeyValue("int", intValue);
        event.addKeyValue("float", floatValue);
        event.addKeyValue("string", stringValue);
    }
}
