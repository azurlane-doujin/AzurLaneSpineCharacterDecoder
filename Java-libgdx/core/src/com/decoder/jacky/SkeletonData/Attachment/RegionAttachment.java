package com.decoder.jacky.SkeletonData.Attachment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.DataInput;
import com.decoder.jacky.JsonBuilder;
import com.decoder.jacky.SkeletonData.Skin;

import java.io.IOException;

public class RegionAttachment extends Attachment {
    private static final int BLX = 0;
    private static final int BLY = 1;
    private static final int ULX = 2;
    private static final int ULY = 3;
    private static final int URX = 4;
    private static final int URY = 5;
    private static final int BRX = 6;
    private static final int BRY = 7;

    private TextureRegion region;
    private String path;
    private float x, y, scaleX = 1, scaleY = 1, rotation, width, height;
    private final float[] vertices = new float[20];
    private final float[] offset = new float[8];
    private final Color color = new Color(1, 1, 1, 1);

    private boolean is_ok=true;
    private RegionAttachment(Skin skin, String name, String path, TextureAtlas atlas) {
        super(skin, name, path, atlas);
        this.region = atlas.findRegion(path);
        if (this.region==null)is_ok=false;
    }

    static public RegionAttachment loadData(DataInput input,String name,float scale,TextureAtlas atlas,Skin skin,float scaleNum) {
        try {

            String path = input.readString();
            float rotation = input.readFloat();
            float x = input.readFloat();
            float y = input.readFloat();
            float scaleX = input.readFloat();
            float scaleY = input.readFloat();
            float width = input.readFloat();
            float height = input.readFloat();
            int color = input.readInt();

            if (path == null) path = name;
            RegionAttachment attachment=new RegionAttachment(skin,name,path,atlas);

            if (!attachment.is_ok) return null;
            attachment.path=path;
            attachment.x=x*scale;
            attachment.y=y*scale;
            attachment.scaleX=scaleX*scaleNum;
            attachment.scaleY=scaleY*scaleNum;
            attachment.rotation=rotation;
            attachment.width=width*scale;
            attachment.height=height*scale;
            Color.rgba8888ToColor(attachment.color, color);
            attachment.updateOffset();
            return attachment;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void updateOffset() {
        float width = getWidth();
        float height = getHeight();
        float localX2 = width / 2;
        float localY2 = height / 2;
        float localX = -localX2;
        float localY = -localY2;
        if (region instanceof TextureAtlas.AtlasRegion) {
            TextureAtlas.AtlasRegion region = (TextureAtlas.AtlasRegion) this.region;
            if (region.rotate) {
                localX += region.offsetX / region.originalWidth * width;
                localY += region.offsetY / region.originalHeight * height;
                localX2 -= (region.originalWidth - region.offsetX - region.packedHeight) / region.originalWidth * width;
                localY2 -= (region.originalHeight - region.offsetY - region.packedWidth) / region.originalHeight * height;
            } else {
                localX += region.offsetX / region.originalWidth * width;
                localY += region.offsetY / region.originalHeight * height;
                localX2 -= (region.originalWidth - region.offsetX - region.packedWidth) / region.originalWidth * width;
                localY2 -= (region.originalHeight - region.offsetY - region.packedHeight) / region.originalHeight * height;
            }
        }
        float scaleX = getScaleX();
        float scaleY = getScaleY();
        localX *= scaleX;
        localY *= scaleY;
        localX2 *= scaleX;
        localY2 *= scaleY;
        float rotation = getRotation();
        float cos = MathUtils.cosDeg(rotation);
        float sin = MathUtils.sinDeg(rotation);
        float x = getX();
        float y = getY();
        float localXCos = localX * cos + x;
        float localXSin = localX * sin;
        float localYCos = localY * cos + y;
        float localYSin = localY * sin;
        float localX2Cos = localX2 * cos + x;
        float localX2Sin = localX2 * sin;
        float localY2Cos = localY2 * cos + y;
        float localY2Sin = localY2 * sin;
        float[] offset = this.offset;
        offset[BLX] = localXCos - localYSin;
        offset[BLY] = localYCos + localXSin;
        offset[ULX] = localXCos - localY2Sin;
        offset[ULY] = localY2Cos + localXSin;
        offset[URX] = localX2Cos - localY2Sin;
        offset[URY] = localY2Cos + localX2Sin;
        offset[BRX] = localX2Cos - localYSin;
        offset[BRY] = localYCos + localX2Sin;
    }

    @Override
    public void buildJsonDict(JsonBuilder.Dict attach) {
        String typeName = "region";
        attach.addKeyValue("type", typeName);
        attach.addKeyValue("path", path);
        attach.addKeyValue("rotation", rotation);
        attach.addKeyValue("x", x);
        attach.addKeyValue("y", y);
        attach.addKeyValue("scaleX", scaleX);
        attach.addKeyValue("scaleY", scaleY);
        attach.addKeyValue("width", width);
        attach.addKeyValue("height", height);
        attach.addKeyValue("color", color.toString());

    }

    void setPath(String path) {
        this.path = path;
    }

    private float getX() {
        return x;
    }

    private float getY() {
        return y;
    }

    private float getScaleX() {
        return scaleX;
    }

    private float getScaleY() {
        return scaleY;
    }

    private float getRotation() {
        return rotation;
    }

    private float getWidth() {
        return width;
    }

    private float getHeight() {
        return height;
    }


}
