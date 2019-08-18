package com.decoder.jacky.SkeletonData;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;

public class BoneData extends BasicData {
    private float x;
    private float y;
    private float scaleX;
    private float scaleY;
    private float shearX;
    private float shearY;
    private float length;
    private float rotation;

    private int index;
    public String name;
    private BoneData parent;

    private TransformMode transformMode = TransformMode.normal;

    private Color color = new Color(0.61f, 0.61f, 0.61f, 1);

    private BoneData(int index, String name, BoneData parent) {
        super(name);
        this.index = index;
        this.parent = parent;
        this.name = name;

    }

    static public BoneData loadData(DataInput input, boolean nonessential, SkeletonData skeletonData, int count) {
        try {

            String name = input.readString();
            BoneData parent = count == 0 ? null : skeletonData.bones.get(input.readInt(true));

            BoneData bone = new BoneData(count, name, parent);

            bone.rotation = input.readFloat();
            bone.x = input.readFloat();
            bone.y = input.readFloat();
            bone.scaleX = input.readFloat();
            bone.scaleY = input.readFloat();
            bone.shearX = input.readFloat();
            bone.shearY = input.readFloat();
            bone.length = input.readFloat();
            bone.transformMode = TransformMode.values[input.readInt(true)];
            if (nonessential)
                Color.rgba8888ToColor(bone.color, input.readInt());

            skeletonData.bones.add(bone);
            return bone;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void buildJsonDict(JsonBuilder.Dict dict) {
        dict.addKeyValue("name", name != null ? name : "null");
        dict.addKeyValue("parent", parent == null ? "null" : parent.name);
        dict.addKeyValue("x", x);
        dict.addKeyValue("y", y);
        dict.addKeyValue("rotation", rotation);
        dict.addKeyValue("scaleX", scaleX);
        dict.addKeyValue("scaleY", scaleY);
        dict.addKeyValue("shearX", shearX);
        dict.addKeyValue("shearY", shearY);
        dict.addKeyValue("length", length);
        dict.addKeyValue("transform", transformMode.name());
        dict.addKeyValue("color", color.toString());
    }

    @Override
    public String toString() {
        return name;
    }

    public enum TransformMode {
        normal, onlyTranslation, noRotationOrReflection, noScale, noScaleOrReflection;

        static public final TransformMode[] values = TransformMode.values();
    }
}
