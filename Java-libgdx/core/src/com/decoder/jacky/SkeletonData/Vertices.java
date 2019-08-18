package com.decoder.jacky.SkeletonData;

import com.badlogic.gdx.utils.DataInput;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.decoder.jacky.JsonBuilder;

import java.io.IOException;

import static com.decoder.jacky.SkeletonData.SkeletonData.readFloatArray;

public class Vertices extends BasicData {


    public int[] bones;
    public float[] vertices;

    static float scale = 1.0f;

    private Vertices() {
        super("vertices");
        this.useList = true;
    }

    static public Vertices loadData(DataInput input, int vertexCount) {
        try {
            int verticesLength = vertexCount << 1;
            Vertices vertices = new Vertices();
            if (!input.readBoolean()) {
                vertices.vertices = readFloatArray(input, verticesLength, scale);
                return vertices;
            }
            FloatArray weights = new FloatArray(verticesLength * 3 * 3);
            IntArray bonesArray = new IntArray(verticesLength * 3);
            for (int i = 0; i < vertexCount; i++) {
                int boneCount = input.readInt(true);
                bonesArray.add(boneCount);
                for (int ii = 0; ii < boneCount; ii++) {
                    bonesArray.add(input.readInt(true));
                    weights.add(input.readFloat() * scale);
                    weights.add(input.readFloat() * scale);
                    weights.add(input.readFloat());
                }
            }
            vertices.vertices = weights.toArray();
            vertices.bones = bonesArray.toArray();
            return vertices;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void buildJsonList(JsonBuilder.List list) {
        int index = 0;
        if (bones == null) {
            for (float vertex : vertices) {
                list.addValue(vertex);
            }
        } else {
            int boneCount = 0;
            while (boneCount < bones.length) {
                int boneNum = bones[boneCount];
                list.addValue(boneNum);
                for (int i = 0; i < boneNum; i++) {
                    list.addValue(bones[boneCount + 1 + i]);

                    list.addValue(vertices[index]);
                    index++;
                    list.addValue(vertices[index]);
                    index++;
                    list.addValue(vertices[index]);
                    index++;
                }
                boneCount += (boneNum + 1);
            }
        }
    }
}
