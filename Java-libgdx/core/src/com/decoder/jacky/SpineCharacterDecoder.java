package com.decoder.jacky;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.*;
import com.decoder.jacky.SkeletonData.*;
import com.decoder.jacky.SkeletonData.Attachment.VertexAttachment;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.ZipException;

class SpineCharacterDecoder {

    private static final int BONE_ROTATE = 0;
    private static final int BONE_TRANSLATE = 1;
    private static final int BONE_SCALE = 2;
    private static final int BONE_SHEAR = 3;

    private static final int SLOT_ATTACHMENT = 0;
    private static final int SLOT_COLOR = 1;
    private static final int SLOT_TWO_COLOR = 2;

    private static final int PATH_POSITION = 0;
    private static final int PATH_SPACING = 1;
    private static final int PATH_MIX = 2;

    //static public final int CURVE_LINEAR = 0;
    private static final int CURVE_STEPPED = 1;
    private static final int CURVE_BEZIER = 2;

    private JsonBuilder builder = new JsonBuilder();
    private JsonBuilder.Dict basicDict = builder.setBasicTypeAsDict();

    String name;
    private boolean failAnimation=false;

    SpineCharacterDecoder() {
    }

    String decoder(FileHandle binaryFile, TextureAtlas atlas, float scale) {
        SkeletonData skeletonData=new SkeletonData();
        name = binaryFile.nameWithoutExtension().replace(".skel", "");
        DataInput input = new DataInput(binaryFile.read(512)) {
            private char[] chars = new char[32];

            @Override
            public String readString() throws IOException {
                int byteCount = readInt(true);
                switch (byteCount) {
                    case 0:
                        return null;
                    case 1:
                        return "";
                }
                byteCount--;
                if (chars.length < byteCount) chars = new char[byteCount];
                char[] chars = this.chars;
                int charCount = 0;
                for (int i = 0; i < byteCount; ) {
                    int b = read();
                    switch (b >> 4) {
                        case -1:
                            throw new EOFException();
                        case 12:
                        case 13:
                            chars[charCount++] = (char) ((b & 0x1F) << 6 | read() & 0x3F);
                            i += 2;
                            break;
                        case 14:
                            chars[charCount++] = (char) ((b & 0x0F) << 12 | (read() & 0x3F) << 6 | read() & 0x3F);
                            i += 3;
                            break;
                        default:
                            chars[charCount++] = (char) b;
                            i++;
                    }
                }
                return new String(chars, 0, charCount);
            }
        };
        boolean nonessential;
        if (atlas!=null)
            System.out.printf("get atlas,%s \n",atlas.toString());
        try {
            System.out.println("\tnow load basic information");
            nonessential=skeletonData.loadData(input,basicDict);

            //bone
            System.out.println("\tnow load bones information");
            JsonBuilder.List Bones = basicDict.addKeyList("bones");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    BoneData data= BoneData.loadData(input,nonessential,skeletonData,i);
                    if (data!=null)
                        data.buildJson(Bones);
                }
            }
            basicDict.insert(Bones);

            //slot
            System.out.println("\tnow load slots information");
            JsonBuilder.List slots = basicDict.addKeyList("slots");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    SlotData data =SlotData.loadData(input,nonessential,skeletonData,i);
                    if (data!=null)
                        data.buildJson(slots);
                }
            }
            basicDict.insert(slots);

            //ik
            System.out.println("\tnow load IK information");
            JsonBuilder.List IKs = basicDict.addKeyList("ik");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    IkConstraintData data =IkConstraintData.loadData(input, skeletonData);
                    if (data!=null)
                        data.buildJson(IKs);
                }
            }
            basicDict.insert(IKs);

            //transform
            System.out.println("\tnow load transform information");
            JsonBuilder.List transforms = basicDict.addKeyList("transform");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    TransformConstraintData data=TransformConstraintData.loadData(input,nonessential,skeletonData,i);
                    if(data!=null)
                        data.buildJson(transforms);
                }
            }
            basicDict.insert(transforms);

            //path
            System.out.println("\tnow load path information");
            JsonBuilder.List paths = basicDict.addKeyList("path");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    PathConstraintData data =PathConstraintData.loadData(input, skeletonData);
                    if (data!=null)
                        data.buildJson(paths);
                }
            }
            basicDict.insert(paths);

            //skin
            System.out.println("\tnow load skins information");
            JsonBuilder.Dict skins = basicDict.addKeyDict("skins");
            {
                Skin defaultSkin=Skin.loadData(input,nonessential,skeletonData,0,"default",atlas,scale);
                if (defaultSkin!=null)
                    defaultSkin.buildJson(skins);
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    String name = input.readString();
                    Skin skin=Skin.loadData(input,nonessential,skeletonData,i+1,name,atlas,scale);
                    if (skin!=null)
                        skin.buildJson(skins);
                }
            }
            basicDict.insert(skins);

            //event
            System.out.println("\tnow load events information");
            JsonBuilder.Dict events = basicDict.addKeyDict("events");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    EventData data=EventData.loadData(input,skeletonData);
                    if (data!=null)
                        data.buildJson(events);
                }
            }
            basicDict.insert(events);

            //animation
        try {
            System.out.println("\tnow load animations information");
            JsonBuilder.Dict animations = basicDict.addKeyDict("animations");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++)
                    readAnimation(input, input.readString(), animations, skeletonData);
            }
            basicDict.insert(animations);
        }catch (Exception event){
            failAnimation=true;
            System.out.printf("fail to load 【%s】's animations\t",name);
        }

            builder.insert(basicDict);
        } catch (IOException info) {
            return String.format("{\"error\":%s}", info.toString());
        }
        String value=builder.toString();
        if (failAnimation)value=value.substring(0,value.length()-14)+"}";
        return value;

    }


    private void readAnimation(DataInput input, String name, JsonBuilder.Dict Animations, SkeletonData data) {
        JsonBuilder.Dict animation = Animations.addKeyDict(name);
        try {
            // Slot timelines.
            int val = input.readInt(true);
            if (val != 0) {
                JsonBuilder.Dict slots = animation.addKeyDict("slots");

                for (int i = 0; i < val; i++) {
                    int slotIndex = input.readInt(true);
                    SlotData slotData=data.slots.get(slotIndex);
                    JsonBuilder.Dict slot = slots.addKeyDict(slotData.name);
                    for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
                        int timelineType = input.readByte();
                        int frameCount = input.readInt(true);

                        switch (timelineType) {
                            case SLOT_ATTACHMENT: {
                                JsonBuilder.List translate = slot.addKeyList("attachment");

                                for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                                    JsonBuilder.Dict perInfo = translate.addDict();
                                    perInfo.addKeyValue("time", input.readFloat());
                                    perInfo.addKeyValue("name", input.readString());
                                    translate.insert(perInfo);
                                }
                                slot.insert(translate);
                                break;
                            }
                            case SLOT_COLOR: {
                                JsonBuilder.List color = slot.addKeyList("color");

                                for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                                    JsonBuilder.Dict perInfo = color.addDict();
                                    {
                                        float time = input.readFloat();
                                        int colorInt = input.readInt();
                                        Color perColor = new Color();
                                        Color.rgba8888ToColor(perColor, colorInt);
                                        perInfo.addKeyValue("color", perColor.toString());
                                        perInfo.addKeyValue("time", time);

                                        if (frameIndex < frameCount - 1) readCurve(input, perInfo);
                                    }
                                    color.insert(perInfo);
                                }
                                slot.insert(color);
                                break;
                            }
                            case SLOT_TWO_COLOR: {
                                JsonBuilder.List twoColor = slot.addKeyList("twoColor");
                                {
                                    for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                                        JsonBuilder.Dict perInfo = twoColor.addDict();
                                        float time = input.readFloat();
                                        int light = input.readInt();
                                        int dark = input.readInt();

                                        Color perColor = new Color();
                                        Color.rgba8888ToColor(perColor, light);
                                        perInfo.addKeyValue("light", perColor.toString());

                                        perColor = new Color();
                                        Color.rgba8888ToColor(perColor, dark);
                                        perInfo.addKeyValue("dark", perColor.toString());

                                        perInfo.addKeyValue("time", time);

                                        if (frameIndex < frameCount - 1) readCurve(input, perInfo);

                                        twoColor.insert(perInfo);
                                    }
                                }
                                slot.insert(twoColor);
                                break;
                            }
                        }
                    }
                    slots.insert(slot);
                }
                animation.insert(slots);
            }

            // Bone timelines.
            val = input.readInt(true);
            if (val != 0) {

                JsonBuilder.Dict bones = animation.addKeyDict("bones");

                for (int i = 0; i < val; i++) {
                    int boneIndex = input.readInt(true);
                    BoneData boneData=data.bones.get(boneIndex);
                    JsonBuilder.Dict bone = bones.addKeyDict(boneData.name);

                    for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
                        int timelineType = input.readByte();
                        int frameCount = input.readInt(true);
                        switch (timelineType) {
                            case BONE_ROTATE: {
                                JsonBuilder.List rotate = bone.addKeyList("rotate");
                                for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                                    JsonBuilder.Dict perInfo = rotate.addDict();
                                    perInfo.addKeyValue("time", input.readFloat());
                                    perInfo.addKeyValue("angle", input.readFloat());
                                    if (frameIndex < frameCount - 1) readCurve(input, perInfo);
                                    rotate.insert(perInfo);
                                }
                                bone.insert(rotate);
                                break;
                            }
                            case BONE_TRANSLATE:
                            case BONE_SCALE:
                            case BONE_SHEAR: {
                                JsonBuilder.List boneInfo;
                                if (timelineType == BONE_SCALE)
                                    boneInfo = bone.addKeyList("scale");
                                else if (timelineType == BONE_SHEAR)
                                    boneInfo = bone.addKeyList("shear");
                                else
                                    boneInfo = bone.addKeyList("translate");
                                for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                                    JsonBuilder.Dict perInfo = boneInfo.addDict();
                                    {
                                        perInfo.addKeyValue("time", input.readFloat());
                                        perInfo.addKeyValue("x", input.readFloat());
                                        perInfo.addKeyValue("y", input.readFloat());
                                        if (frameIndex < frameCount - 1) readCurve(input, perInfo);
                                    }
                                    boneInfo.insert(perInfo);
                                }
                                bone.insert(boneInfo);
                                break;
                            }
                        }
                    }
                    bones.insert(bone);
                }
                animation.insert(bones);
            }

            // IK constraint timelines.
            val = input.readInt(true);
            if (val != 0) {

                JsonBuilder.Dict iks = animation.addKeyDict("ik");

                for (int i = 0; i < val; i++) {
                    int index = input.readInt(true);
                    IkConstraintData ikConstraintData=data.ikConstraints.get(index);
                    JsonBuilder.List ik = iks.addKeyList(ikConstraintData.name);
                    {
                        int frameCount = input.readInt(true);
                        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                            JsonBuilder.Dict perInfo = ik.addDict();
                            {
                                perInfo.addKeyValue("time", input.readFloat());
                                perInfo.addKeyValue("mix", input.readFloat());
                                perInfo.addKeyValue("bendPositive", (int) input.readByte() == 0);

                                if (frameIndex < frameCount - 1) readCurve(input, perInfo);
                            }
                            ik.insert(perInfo);
                        }
                    }
                    iks.insert(ik);
                }
                animation.insert(iks);
            }

            // Transform constraint timelines.
            val = input.readInt(true);
            if (val != 0) {
                JsonBuilder.Dict transforms = animation.addKeyDict("transform");

                for (int i = 0; i < val; i++) {
                    int index = input.readInt(true);
                    String transformName = data.transformConstraints.get(index).name;

                    JsonBuilder.List transform = transforms.addKeyList(transformName);
                    {
                        int frameCount = input.readInt(true);
                        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                            JsonBuilder.Dict perInfo = transform.addDict();
                            {
                                perInfo.addKeyValue("time", input.readFloat());
                                perInfo.addKeyValue("rotateMix", input.readFloat());
                                perInfo.addKeyValue("translateMix", input.readFloat());
                                perInfo.addKeyValue("scaleMix", input.readFloat());
                                perInfo.addKeyValue("shearMix", input.readFloat());

                                if (frameIndex < frameCount - 1) readCurve(input, perInfo);
                            }
                            transform.insert(perInfo);
                        }
                    }
                    transforms.insert(transform);
                }
                animation.insert(transforms);
            }

            // Path constraint timelines.
            val = input.readInt(true);
            if (val != 0) {
                JsonBuilder.Dict paths = animation.addKeyDict("paths");
                {
                    for (int i = 0, n = input.readInt(true); i < n; i++) {
                        int index = input.readInt(true);
                        String pathName = data.pathConstraints.get(index).name;

                        JsonBuilder.Dict path = paths.addKeyDict(pathName);
                        {
                            for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
                                int timelineType = input.readByte();
                                int frameCount = input.readInt(true);

                                switch (timelineType) {
                                    case PATH_POSITION:
                                    case PATH_SPACING: {
                                        JsonBuilder.List pathInfo;
                                        String timeName;
                                        if (timelineType == PATH_SPACING) {
                                            pathInfo = path.addKeyList("spacing");
                                            timeName = "spacing";
                                        } else {
                                            pathInfo = path.addKeyList("position");
                                            timeName = "position";
                                        }
                                        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                                            JsonBuilder.Dict perInfo = pathInfo.addDict();
                                            {
                                                perInfo.addKeyValue("time", input.readFloat());
                                                perInfo.addKeyValue(timeName, input.readFloat());

                                                if (frameIndex < frameCount - 1) readCurve(input, perInfo);
                                            }
                                            pathInfo.insert(perInfo);
                                        }
                                        path.insert(pathInfo);
                                        break;
                                    }
                                    case PATH_MIX: {
                                        JsonBuilder.List mix = path.addKeyList("mix");
                                        {
                                            for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                                                JsonBuilder.Dict perInfo = mix.addDict();
                                                {
                                                    perInfo.addKeyValue("time", input.readFloat());
                                                    perInfo.addKeyValue("rotateMix", input.readFloat());
                                                    perInfo.addKeyValue("translateMix", input.readFloat());

                                                    if (frameIndex < frameCount - 1) readCurve(input, perInfo);
                                                }
                                                mix.insert(perInfo);
                                            }
                                        }
                                        path.insert(mix);
                                        break;
                                    }
                                }
                            }
                        }
                        paths.insert(path);
                    }
                }
                animation.insert(paths);
            }

            // Deform timelines.
            val = input.readInt(true);
            if (val != 0) {
                JsonBuilder.Dict deforms = animation.addKeyDict("deform");

                for (int i = 0; i < val; i++) {
                    Skin skin=data.skin.get(input.readInt(true));
                    JsonBuilder.Dict perDeform = deforms.addKeyDict(skin.name);
                    {
                        for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {

                            int slotIndex = input.readInt(true);
                            SlotData slotData=data.slots.get(slotIndex);
                            JsonBuilder.Dict slot = perDeform.addKeyDict(slotData.name);
                            {
                                for (int iii = 0, nnn = input.readInt(true); iii < nnn; iii++) {
                                    String at_name=input.readString();
                                    VertexAttachment attachment = (VertexAttachment)skin.getAttachment(slotIndex,at_name );
                                    boolean weighted;
                                    weighted = attachment.bones != null;
                                    float[] vertices = attachment.vertices;
                                    int deformLength = weighted ? vertices.length / 3 * 2 : vertices.length;
                                    int frameCount = input.readInt(true);

                                    JsonBuilder.List timeline = slot.addKeyList(attachment.name);
                                    {
                                        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                                            JsonBuilder.Dict perInfo = timeline.addDict();
                                            {
                                                float time = input.readFloat();
                                                perInfo.addKeyValue("time", time);
                                                float[] deform;
                                                int end = input.readInt(true);
                                                if (end == 0)
                                                    deform = weighted ? new float[deformLength] : vertices;
                                                else {
                                                    deform = new float[deformLength];
                                                    int start = input.readInt(true);
                                                    end += start;
                                                    for (int v = start; v < end; v++)
                                                        deform[v] = input.readFloat();
                                                    if (!weighted) {
                                                        for (int v = 0, vn = deform.length; v < vn; v++)
                                                            deform[v] += vertices[v];
                                                    }
                                                    perInfo.addKeyValue("offset", start);
                                                }
                                                if (frameIndex < frameCount - 1)
                                                    readCurve(input, perInfo);
                                                JsonBuilder.List verticesValue = perInfo.addKeyList("vertices");
                                                {
                                                    for (float v : deform)
                                                        verticesValue.addValue(v);
                                                }
                                                perInfo.insert(verticesValue);

                                            }
                                            timeline.insert(perInfo);
                                        }
                                    }
                                    slot.insert(timeline);
                                }
                            }
                            perDeform.insert(slot);
                        }
                    }
                    deforms.insert(perDeform);
                }
                animation.insert(deforms);
            }

            // Draw order timeline.
            int drawOrderCount = input.readInt(true);
            if (drawOrderCount > 0) {
                JsonBuilder.List drawOrders = animation.addKeyList("drawOrder");
                {
                    for (int i = 0; i < drawOrderCount; i++) {
                        JsonBuilder.Dict drawOrder = drawOrders.addDict();
                        {
                            float time = input.readFloat();
                            int offsetCount = input.readInt(true);

                            JsonBuilder.List offsets = drawOrder.addKeyList("offsets");
                            {
                                for (int ii = 0; ii < offsetCount; ii++) {
                                    JsonBuilder.Dict offset = offsets.addDict();
                                    {
                                        int slotIndex = input.readInt(true);
                                        offset.addKeyValue("slot", data.slots.get(slotIndex).name);
                                        offset.addKeyValue("offset", input.readInt(true));
                                    }
                                    offsets.insert(offset);
                                }
                            }
                            drawOrder.insert(offsets);
                            drawOrder.addKeyValue("time", time);
                        }
                        drawOrders.insert(drawOrder);
                    }
                }
                animation.insert(drawOrders);
            }

            // Event timeline.
            int eventCount = input.readInt(true);
            if (eventCount > 0) {
                JsonBuilder.List events = animation.addKeyList("events");
                {
                    for (int i = 0; i < eventCount; i++) {
                        JsonBuilder.Dict event = events.addDict();
                        {
                            event.addKeyValue("time", input.readFloat());
                            event.addKeyValue("name", data.events.get(input.readInt(true)).name);

                            event.addKeyValue("int", input.readInt(false));
                            event.addKeyValue("float", input.readFloat());
                            event.addKeyValue("string", input.readString());
                        }
                        events.insert(event);
                    }
                }
                animation.insert(events);
            }

        } catch (ZipException ex) {
            throw new SerializationException("Error reading skeleton file.", ex);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Animations.insert(animation);
    }

    private void readCurve(DataInput input, JsonBuilder.Dict timeline) throws IOException {
        switch (input.readByte()) {
            case CURVE_STEPPED:
                timeline.addKeyValue("curve", "stepped");
                break;
            case CURVE_BEZIER:
                JsonBuilder.List val = timeline.addKeyList("curve");
                val.addValue(input.readFloat());
                val.addValue(input.readFloat());
                val.addValue(input.readFloat());
                val.addValue(input.readFloat());
                timeline.insert(val);
                break;
        }
    }
}
