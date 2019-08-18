package com.decoder.jacky.SkeletonData.Attachment;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.SkeletonData.BasicData;
import com.decoder.jacky.SkeletonData.EnumGroup.AttachmentType;
import com.decoder.jacky.SkeletonData.Skin;

import java.io.IOException;

public class Attachment  extends BasicData {

    private static float scale = 1.0f;
    public String name;

    Attachment(Skin skin, String name, String path, TextureAtlas atlas) {
        super(name);
    }


    static public Attachment loadData(DataInput input, Skin skin, String attachmentName, boolean nonessential,
                                      TextureAtlas atlas,float scaleNum) {
        try {
            float scale = Attachment.scale;

            String name = input.readString();
            if (name == null) name = attachmentName;

            AttachmentType type = AttachmentType.values[input.readByte()];
            switch (type) {
                case region: {
                    return RegionAttachment.loadData(input, name, scale, atlas, skin,scaleNum);
                }
                case boundingbox: {
                    return BoundingBoxAttachment.loadData(input, skin, name, atlas, nonessential);
                }
                case mesh: {
                    return MeshAttachment.loadData(input, nonessential, name, skin, atlas, scale, AttachmentType.mesh);
                }
                case linkedmesh: {
                    return MeshAttachment.loadData(input, nonessential, name, skin, atlas, scale, AttachmentType.linkedmesh);
                }
                case path: {
                    return PathAttachment.loadData(input, nonessential, scale, skin, name, atlas);
                }
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}