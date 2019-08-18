package com.decoder.jacky.SkeletonData.Attachment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;
import com.decoder.jacky.SkeletonData.Skin;

import java.io.IOException;

public class BoundingBoxAttachment extends VertexAttachment {
    private Color color = new Color(0.38f, 0.94f, 0, 1);

    BoundingBoxAttachment(Skin skin, String name, String path, TextureAtlas atlas) {
        super(skin, name, path, atlas);
    }

    static public BoundingBoxAttachment loadData(DataInput input, Skin skin, String name, TextureAtlas atlas, boolean nonessential) {
        try {
            BoundingBoxAttachment box = (BoundingBoxAttachment) VertexAttachment.loadData(input, skin, name, atlas, nonessential);
            if (box == null) return null;
            int color = nonessential ? input.readInt() : 0;
            if (nonessential) Color.rgba8888ToColor(box.color, color);

            return box;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void buildJsonDict(JsonBuilder.Dict attach) {
        attach.addKeyValue("type","boundingbox");
        attach.addKeyArray("vertices", vertices);
        attach.addKeyValue("color", color.toString());
    }
}