package com.decoder.jacky.SkeletonData.Attachment;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.SkeletonData.Skin;
import com.decoder.jacky.SkeletonData.Vertices;

import java.io.IOException;

public class VertexAttachment extends Attachment {

    VertexAttachment(Skin skin, String name, String path, TextureAtlas atlas) {
        super(skin, name, path, atlas);this.name=name;
    }

    public int[] bones;
    public float[] vertices;
    int worldVerticesLength;
    public String name;

    static public VertexAttachment loadData(DataInput input,Skin skin,String name,TextureAtlas atlas,boolean nonessential){
        try{
            int vertexCount = input.readInt(true);
            Vertices vertices = Vertices.loadData(input, vertexCount);
            VertexAttachment box = new VertexAttachment(skin, name,"",atlas);
            box.worldVerticesLength=vertexCount << 1;
            if (vertices != null) {
                box.vertices = (vertices.vertices);
                box.bones = (vertices.bones);
            }
            return box;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

