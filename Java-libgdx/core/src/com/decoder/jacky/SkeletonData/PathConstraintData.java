package com.decoder.jacky.SkeletonData;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;

import java.io.IOException;

public class PathConstraintData extends BasicData {
    public String name;
    private int order;
    private Array<BoneData> bones = new Array<BoneData>();
    private SlotData target;
    private PositionMode positionMode;
    private SpacingMode spacingMode;
    private RotateMode rotateMode;
    private float offsetRotation;
    private float position, spacing, rotateMix, translateMix;

    private PathConstraintData(String name) {
        super(name);
        this.name = name;
    }

    static public PathConstraintData loadData(
            DataInput input,
            SkeletonData skeletonData) {
        try {
            PathConstraintData data = new PathConstraintData(input.readString());
            data.order = input.readInt(true);
            for (int ii = 0, nn = input.readInt(true); ii < nn; ii++)
                data.bones.add(skeletonData.bones.get(input.readInt(true)));
            data.target = skeletonData.slots.get(input.readInt(true));
            data.positionMode = PositionMode.values[input.readInt(true)];
            data.spacingMode = SpacingMode.values[input.readInt(true)];
            data.rotateMode = RotateMode.values[input.readInt(true)];
            data.offsetRotation = input.readFloat();
            data.position = input.readFloat();
            if (data.positionMode == PositionMode.fixed) data.position *= data.scale;
            data.spacing = input.readFloat();
            if (data.spacingMode == SpacingMode.length || data.spacingMode == SpacingMode.fixed)
                data.spacing *= data.scale;
            data.rotateMix = input.readFloat();
            data.translateMix = input.readFloat();
            skeletonData.pathConstraints.add(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void buildJsonDict(JsonBuilder.Dict path) {
        path.addKeyValue("name", name);
        path.addKeyValue("order", order);
        path.addKeyArray("bones", bones);
        path.addKeyValue("target", target.name);
        path.addKeyValue("positionMode", positionMode.name());
        path.addKeyValue("spacingMode", spacingMode.name());
        path.addKeyValue("rotateMode", rotateMode.name());
        path.addKeyValue("offset", offsetRotation);
        path.addKeyValue("position", position);
        path.addKeyValue("spacing", spacing);
        path.addKeyValue("rotateMix", rotateMix);
        path.addKeyValue("translateMix", translateMix);
    }

    public enum PositionMode {
        fixed, percent;

        static public final PositionMode[] values = PositionMode.values();
    }

    public enum SpacingMode {
        length, fixed, percent;

        static public final SpacingMode[] values = SpacingMode.values();
    }

    public enum RotateMode {
        tangent, chain, chainScale;

        static public final RotateMode[] values = RotateMode.values();
    }
}
