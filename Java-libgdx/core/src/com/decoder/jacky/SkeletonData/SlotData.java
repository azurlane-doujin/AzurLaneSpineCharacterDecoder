package com.decoder.jacky.SkeletonData;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;
import com.decoder.jacky.SkeletonData.EnumGroup.BlendMode;

import java.io.IOException;

public class SlotData extends BasicData {
    private int index;
    public String name;
    private BoneData boneData;
    private Color color = new Color(1, 1, 1, 1);
    private Color darkColor;

    private String attachmentName;
    private BlendMode blendMode;

    private SlotData(int index, String name, BoneData boneData) {
        super(name);
        this.index = index;
        this.name = name;
        this.boneData = boneData;
    }

    static public SlotData loadData(DataInput input, boolean nonessential, SkeletonData skeletonData, int count) {
        try {
            String slotName = input.readString();
            BoneData bone = skeletonData.bones.get(input.readInt(true));
            SlotData slot = new SlotData(count, slotName, bone);
            Color.rgba8888ToColor(slot.color, input.readInt());

            if (skeletonData.version.startsWith("3.6")) {

                int darkColorInt = input.readInt();
                if (darkColorInt != -1)
                    slot.darkColor = new Color(darkColorInt);
            }
            slot.attachmentName = input.readString();
            slot.blendMode = BlendMode.values[input.readInt(true)];
            skeletonData.slots.add(slot);
            return slot;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void buildJsonDict(JsonBuilder.Dict dict) {
        dict.addKeyValue("name", name);
        dict.addKeyValue("bone", boneData.name);
        dict.addKeyValue("color", color.toString());
        dict.addKeyValue("attachment", attachmentName);
        if (darkColor != null)
            dict.addKeyValue("dark", darkColor.toString());
        dict.addKeyValue("blend", blendMode.name());
    }
}
