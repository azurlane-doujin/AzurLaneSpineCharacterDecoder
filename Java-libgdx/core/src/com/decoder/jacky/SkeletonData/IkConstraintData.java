package com.decoder.jacky.SkeletonData;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;

import java.io.IOException;

public class IkConstraintData extends BasicData {
    public String name;
    private int order;
    private Array<BoneData> bones = new Array<BoneData>();
    private BoneData target;
    private int bendDirection = 1;
    private float mix = 1;

    private IkConstraintData(String name) {
        super(name);
        if (name == null) throw new IllegalArgumentException("name cannot be null.");
        this.name = name;
    }

    static public IkConstraintData loadData(
            DataInput input,
            SkeletonData skeletonData) {
        try {
            IkConstraintData data = new IkConstraintData(input.readString());
            data.order = input.readInt(true);
            for (int j = 0, m = input.readInt(true); j < m; j++) {
                data.bones.add(skeletonData.bones.get(input.readInt(true)));
            }
            data.target = skeletonData.bones.get(input.readInt(true));
            data.mix = input.readFloat();
            data.bendDirection = input.readByte();

            skeletonData.ikConstraints.add(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void buildJsonDict(JsonBuilder.Dict ik) {
        ik.addKeyValue("name", name);
        ik.addKeyValue("order", order);
        ik.addKeyArray("bones", bones);
        ik.addKeyValue("target", target.name);
        ik.addKeyValue("mix", mix);
        ik.addKeyValue("bendPositive", bendDirection);
    }
}
