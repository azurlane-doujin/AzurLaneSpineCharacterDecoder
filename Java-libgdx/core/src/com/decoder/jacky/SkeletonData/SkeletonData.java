package com.decoder.jacky.SkeletonData;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;

import java.io.IOException;

public class SkeletonData {
    String version;
    private String hash;
    private float width;
    private float height;
    private boolean nonessential;

    //nonessential
    private float fps = 30;
    private String imagesPath = "./";

    public Array<BoneData> bones = new Array<BoneData>();
    public Array<SlotData> slots = new Array<SlotData>();
    public Array<IkConstraintData> ikConstraints = new Array<IkConstraintData>();
    public Array<TransformConstraintData> transformConstraints = new Array<TransformConstraintData>();
    public Array<PathConstraintData> pathConstraints = new Array<PathConstraintData>();
    public Array<Skin> skin = new Array<Skin>();
    public Array<EventData> events = new Array<EventData>();

    static public float[] readFloatArray(DataInput input, int n, float scale) throws IOException {
        float[] array = new float[n];
        if (scale == 1) {
            for (int i = 0; i < n; i++)
                array[i] = input.readFloat();
        } else {
            for (int i = 0; i < n; i++)
                array[i] = input.readFloat() * scale;
        }
        return array;
    }

    static public short[] readShortArray(DataInput input) throws IOException {
        int n = input.readInt(true);
        short[] array = new short[n];
        for (int i = 0; i < n; i++)
            array[i] = input.readShort();
        return array;
    }

    public boolean loadData(DataInput input, JsonBuilder.Dict basicDict) {
        JsonBuilder.Dict skeleton = basicDict.addKeyDict("skeleton");
        try {
            hash = input.readString();
            version = input.readString();
            width = input.readFloat();
            height = input.readFloat();

            if (hash == null || hash.isEmpty())
                skeleton.addKeyValue("hash", "null");
            skeleton.addKeyValue("hash", hash != null ? hash : "null");
            if (version == null || version.isEmpty())
                skeleton.addKeyValue("spine", "null");
            skeleton.addKeyValue("spine", version != null ? version : "null");

            skeleton.addKeyValue("width", width);
            skeleton.addKeyValue("height", height);

            nonessential = input.readBoolean();

            if (nonessential) {
                fps = input.readFloat();
                imagesPath = input.readString();
                if (imagesPath == null || imagesPath.isEmpty()) imagesPath = "null";
                skeleton.addKeyValue("fps", fps);
                skeleton.addKeyValue("images", imagesPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        basicDict.insert(skeleton);

        return nonessential;
    }
}
