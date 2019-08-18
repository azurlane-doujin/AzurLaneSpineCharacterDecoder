package com.decoder.jacky.SkeletonData;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;

import java.io.IOException;

public class TransformConstraintData extends BasicData {
    public String name;
    private int order;
    private Array<BoneData> bones = new Array<BoneData>();
    private BoneData target;
    private float rotateMix, translateMix, scaleMix, shearMix;
    private float offsetRotation, offsetX, offsetY, offsetScaleX, offsetScaleY, offsetShearY;

    private boolean local, relative, is_3_6 = false;

    private TransformConstraintData(String name) {
        super(name);
        this.name = name;
    }

    static public TransformConstraintData loadData(
            DataInput input,
            boolean nonessential,
            SkeletonData skeletonData,
            int count) {
        try {
            TransformConstraintData data = new TransformConstraintData(input.readString());
            data.order = input.readInt(true);
            for (int ii = 0, nn = input.readInt(true); ii < nn; ii++)
                data.bones.add(skeletonData.bones.get(input.readInt(true)));
            data.target = skeletonData.bones.get(input.readInt(true));

            if (skeletonData.version.startsWith("3.6")) {
                data.is_3_6 = true;
                data.local = input.readBoolean();
                data.relative = input.readBoolean();
            }

            data.offsetRotation = input.readFloat();
            data.offsetX = input.readFloat();
            data.offsetY = input.readFloat();
            data.offsetScaleX = input.readFloat();
            data.offsetScaleY = input.readFloat();
            data.offsetShearY = input.readFloat();
            data.rotateMix = input.readFloat();
            data.translateMix = input.readFloat();
            data.scaleMix = input.readFloat();
            data.shearMix = input.readFloat();

            skeletonData.transformConstraints.add(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void buildJsonDict(JsonBuilder.Dict transform) {

        transform.addKeyValue("name", name);
        transform.addKeyValue("order", order);
        transform.addKeyArray("bones", bones);

        transform.addKeyValue("target", target.name);
        if (is_3_6) {
            transform.addKeyValue("local", local);
            transform.addKeyValue("relative", relative);
        }
        transform.addKeyValue("offsetRotation", offsetRotation);
        transform.addKeyValue("offsetX", offsetX);
        transform.addKeyValue("offsetY", offsetY);
        transform.addKeyValue("offsetScaleX", offsetScaleX);
        transform.addKeyValue("offsetScaleY", offsetScaleY);
        transform.addKeyValue("offsetShearY", offsetShearY);
        transform.addKeyValue("rotateMix", rotateMix);
        transform.addKeyValue("translateMix", translateMix);
        transform.addKeyValue("scaleMix", scaleMix);
        transform.addKeyValue("shearMix", shearMix);
    }
}
