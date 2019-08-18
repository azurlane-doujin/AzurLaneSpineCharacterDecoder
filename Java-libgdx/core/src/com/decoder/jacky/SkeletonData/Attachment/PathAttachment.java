package com.decoder.jacky.SkeletonData.Attachment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;
import com.decoder.jacky.SkeletonData.Skin;
import com.decoder.jacky.SkeletonData.Vertices;

import java.io.IOException;

public class PathAttachment extends VertexAttachment {

    private float[] lengths;
    private boolean closed, constantSpeed;

    // Nonessential.
    private final Color color = new Color(1, 0.5f, 0, 1);

    private int vertexCount;

    private PathAttachment(Skin skin, String name, String path, TextureAtlas atlas) {
        super(skin, name, path, atlas);
    }

    static public PathAttachment loadData(DataInput input, boolean nonessential,
                                          float scale, Skin skin, String name
            , TextureAtlas atlas) {
        try {
            boolean closed = input.readBoolean();
            boolean constantSpeed = input.readBoolean();
            int vertexCount = input.readInt(true);
            Vertices vertices = Vertices.loadData(input, vertexCount);
            float[] lengths = new float[vertexCount / 3];
            for (int i = 0, n = lengths.length; i < n; i++)
                lengths[i] = input.readFloat() * scale;
            int color = nonessential ? input.readInt() : 0;

            PathAttachment path = new PathAttachment(skin, name, "", atlas);
            if (path.name == null) return null;
            path.closed = (closed);
            path.constantSpeed = (constantSpeed);
            path.worldVerticesLength = (vertexCount << 1);
            path.vertexCount = vertexCount;
            if (vertices != null) {
                path.vertices = (vertices.vertices);
                path.bones = (vertices.bones);
            }
            path.lengths = (lengths);
            if (nonessential) Color.rgba8888ToColor(path.color, color);

            return path;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void buildJsonDict(JsonBuilder.Dict attach) {
        attach.addKeyValue("closed", closed);
        attach.addKeyValue("constantSpeed", constantSpeed);
        attach.addKeyValue("vertexCount", vertexCount);
        attach.addKeyArray("vertices", vertices);
        attach.addKeyArray("lengths", lengths);
        attach.addKeyValue("color", color.toString());
    }
}

