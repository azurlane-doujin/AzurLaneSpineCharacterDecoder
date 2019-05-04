package com.decoder.jacky;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpineCharacterDecoder {

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

    static public final int CURVE_LINEAR = 0;
    private static final int CURVE_STEPPED = 1;
    private static final int CURVE_BEZIER = 2;

    final static private int Attachment_type_region = 0;
    final static private int Attachment_type_boundingbox = 1;
    final static private int Attachment_type_mesh = 2;
    final static private int Attachment_type_linkedmesh = 3;
    final static private int Attachment_type_path = 4;
    final static private int Attachment_type_point = 5;
    final static private int Attachment_type_clippin = 6;

    private final String[] TransformMode = new String[]{
            "normal",
            "onlyTranslation",
            "noRotationOrReflection",
            "noScale",
            "noScaleOrReflection"};

    private final String[] BlendMode = new String[]{
            "normal",
            "additive",
            "multiply",
            "screen",
    };
    private final String[] PositionMode = new String[]{
            "fixed",
            "percent"};
    private final String[] SpacingMode = new String[]{
            "length",
            "fixed",
            "percent"};
    private final String[] RotateMode = new String[]{
            "tangent",
            "chain",
            "chainScale"
    };

    private final String[] AttachmentType = new String[]{
            "region",
            "boundingbox",
            "mesh",
            "linkedmesh",
            "path",
            "point",
            "clipping"};

    private JsonBuilder builder = new JsonBuilder();
    private JsonBuilder.Dict basicDict = builder.setBasicTypeAsDict();

    private Map<Integer, String> slotsName;
    private Map<Integer, String> bonesName;
    private Map<Integer, String> skinsName;
    private Map<Integer, String> IK_Name;
    private Map<Integer, String> transformsName;
    private Map<Integer, String> pathsName;
    private Map<Integer, String> eventName;

    private Map<String, FloatArray> attachVertices;
    private Map<String, String> slotAttach;

    String version;

    String name;
    boolean failAnimation=false;

    String decoder(FileHandle binaryFile) {
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
        try {
            JsonBuilder.Dict skeleton = basicDict.addKeyDict("skeleton");
            {
                String hash = input.readString();
                version = input.readString();
                float width = input.readFloat();
                float height = input.readFloat();

                if (hash == null || hash.isEmpty())
                    skeleton.addKeyValue("hash", "null");
                skeleton.addKeyValue("hash", hash != null ? hash : "null");
                if (version == null || version.isEmpty())
                    skeleton.addKeyValue("spine", "null");
                skeleton.addKeyValue("spine", version != null ? version : "null");

                skeleton.addKeyValue("width", width);
                skeleton.addKeyValue("height", height);

                nonessential = input.readBoolean();

                if (nonessential) {
                    float fps = input.readFloat();
                    String imagesPath = input.readString();
                    if (imagesPath == null || imagesPath.isEmpty()) imagesPath = "null";
                    skeleton.addKeyValue("fps", fps);
                    skeleton.addKeyValue("images", imagesPath);
                }
            }
            basicDict.insert(skeleton);

            bonesName = new HashMap<Integer, String>();
            slotsName = new HashMap<Integer, String>();
            skinsName = new HashMap<Integer, String>();
            IK_Name = new HashMap<Integer, String>();
            transformsName = new HashMap<Integer, String>();
            pathsName = new HashMap<Integer, String>();
            eventName = new HashMap<Integer, String>();

            attachVertices = new HashMap<String, FloatArray>();
            slotAttach = new HashMap<String, String>();


            JsonBuilder.List Bones = basicDict.addKeyList("bones");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    JsonBuilder.Dict bone = Bones.addDict();
                    {
                        String name = input.readString();

                        String parent = i == 0 ? null : bonesName.get(input.readInt(true));

                        float rotation = input.readFloat();
                        float x = input.readFloat();
                        float y = input.readFloat();
                        float scaleX = input.readFloat();
                        float scaleY = input.readFloat();
                        float shearX = input.readFloat();
                        float shearY = input.readFloat();
                        float length = input.readFloat();
                        int transformMode = input.readInt(true);

                        bone.addKeyValue("name", name != null ? name : "null");
                        if (parent != null)
                            bone.addKeyValue("parent", parent);
                        bone.addKeyValue("x", x);
                        bone.addKeyValue("y", y);
                        bone.addKeyValue("rotation", rotation);
                        bone.addKeyValue("scaleX", scaleX);
                        bone.addKeyValue("scaleY", scaleY);
                        bone.addKeyValue("shearX", shearX);
                        bone.addKeyValue("shearY", shearY);
                        bone.addKeyValue("length", length);
                        bone.addKeyValue("transform", this.TransformMode[transformMode >= TransformMode.length ? TransformMode.length - 1 : transformMode]);

                        if (nonessential) {
                            int colorInt = input.readInt();
                            Color color = new Color();
                            Color.rgba8888ToColor(color, colorInt);
                            bone.addKeyValue("color", color.toString());
                        }
                        bonesName.put(i, name);
                    }
                    Bones.insert(bone);

                }
            }
            basicDict.insert(Bones);


            JsonBuilder.List slots = basicDict.addKeyList("slots");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    JsonBuilder.Dict slot = slots.addDict();
                    {
                        String slotName = input.readString();
                        int val = input.readInt(true);
                        int index = val == 0 ? 1 : val;
                        String boneName;
                        if (bonesName.values().contains(slotName))
                            boneName = slotName;
                        else
                            boneName = bonesName.get(index);

                        int colorInt = input.readInt();
                        Color color = new Color();
                        Color.rgba8888ToColor(color, colorInt);

                        if (version.startsWith("3.6")) {
                            int darkColorInt = input.readInt();
                            Color darkColor = new Color();
                            if (darkColorInt != -1)
                                Color.rgba8888ToColor(darkColor, darkColorInt);
                            slot.addKeyValue("dark", darkColor.toString());

                        }

                        String attachmentName = input.readString();

                        int blendMode = input.readInt(true);
                        slot.addKeyValue("blend", this.BlendMode[blendMode >= this.BlendMode.length ? BlendMode.length - 1 : blendMode]);

                        slot.addKeyValue("name", slotName != null ? slotName : "null");
                        slot.addKeyValue("bone", boneName != null ? boneName : "null");
                        slot.addKeyValue("color", color.toString());

                        if (attachmentName != null)
                            slot.addKeyValue("attachment", attachmentName);
                        slotsName.put(i, slotName);
                        slotAttach.put(slotName, attachmentName);
                    }
                    slots.insert(slot);
                }
            }
            basicDict.insert(slots);

            JsonBuilder.List IKs = basicDict.addKeyList("ik");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    JsonBuilder.Dict ik = IKs.addDict();
                    {
                        String name = input.readString();
                        int order = input.readInt(true);
                        ik.addKeyValue("name", name != null ? name : "null");
                        ik.addKeyValue("order", order);
                        JsonBuilder.List bones = ik.addKeyList("bones");
                        {
                            for (int j = 0, m = input.readInt(true); j < m; j++) {
                                bones.addValue(bonesName.get(input.readInt(true)));
                            }
                        }
                        ik.insert(bones);
                        ik.addKeyValue("target", bonesName.get(input.readInt(true)));
                        ik.addKeyValue("mix", input.readFloat());
                        ik.addKeyValue("bendPositive", input.readByte());
                        IK_Name.put(i, name);
                    }
                    IKs.insert(ik);
                }
            }
            basicDict.insert(IKs);

            JsonBuilder.List transforms = basicDict.addKeyList("transform");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    JsonBuilder.Dict transform = transforms.addDict();
                    {
                        String name = input.readString();
                        transform.addKeyValue("name", name != null ? name : "null");
                        transform.addKeyValue("order", input.readInt(true));
                        JsonBuilder.List bones = transform.addKeyList("bones");
                        {
                            for (int j = 0, m = input.readInt(true); j < m; j++) {
                                bones.addValue(bonesName.get(input.readInt(true)));
                            }
                        }
                        transform.insert(bones);
                        transform.addKeyValue("target", bonesName.get(input.readInt(true)));
                        if (version.startsWith("3.6")) {
                            transform.addKeyValue("local", input.readBoolean());
                            transform.addKeyValue("relative", input.readBoolean());
                        }
                        transform.addKeyValue("offsetRotation", input.readFloat());
                        transform.addKeyValue("offsetX", input.readFloat());
                        transform.addKeyValue("offsetY", input.readFloat());
                        transform.addKeyValue("offsetScaleX", input.readFloat());
                        transform.addKeyValue("offsetScaleY", input.readFloat());
                        transform.addKeyValue("offsetShearY", input.readFloat());
                        transform.addKeyValue("rotateMix", input.readFloat());
                        transform.addKeyValue("translateMix", input.readFloat());
                        transform.addKeyValue("scaleMix", input.readFloat());
                        transform.addKeyValue("shearMix", input.readFloat());
                        transformsName.put(i, name);
                    }
                    transforms.insert(transform);
                }
            }
            basicDict.insert(transforms);

            JsonBuilder.List paths = basicDict.addKeyList("path");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    JsonBuilder.Dict path = paths.addDict();
                    {
                        String name = input.readString();
                        path.addKeyValue("name", name != null ? name : "null");
                        path.addKeyValue("order", input.readInt(true));
                        JsonBuilder.List bones = path.addKeyList("bones");
                        {
                            for (int j = 0, m = input.readInt(true); j < m; j++) {
                                bones.addValue(bonesName.get(input.readInt(true)));
                            }
                        }
                        path.insert(bones);


                        path.addKeyValue("target", slotsName.get(input.readInt(true)));

                        int positionModeIndex = input.readInt(true);
                        int spacingModeIndex = input.readInt(true);
                        int rotateModeIndex = input.readInt(true);
                        String positionMode = PositionMode[positionModeIndex >= PositionMode.length ? 1 : positionModeIndex];
                        String spacingMode = SpacingMode[spacingModeIndex >= SpacingMode.length ? 2 : spacingModeIndex];
                        String rotateMode = RotateMode[rotateModeIndex >= RotateMode.length ? 2 : rotateModeIndex];
                        path.addKeyValue("positionMode", positionMode);
                        path.addKeyValue("spacingMode", spacingMode);
                        path.addKeyValue("rotateMode", rotateMode);


                        path.addKeyValue("offset", input.readFloat());
                        float position = input.readFloat();
                        if (positionMode.equals("fixed"))
                            position *= 1;
                        path.addKeyValue("position", position);
                        float spacing = input.readFloat();
                        if (spacingMode.equals("length") || spacingMode.equals("fixed"))
                            spacing *= 1;
                        path.addKeyValue("spacing", spacing);
                        path.addKeyValue("rotateMix", input.readFloat());
                        path.addKeyValue("translateMix", input.readFloat());
                        pathsName.put(i, name);
                    }
                    paths.insert(path);
                }
            }
            basicDict.insert(paths);


            JsonBuilder.Dict skins = basicDict.addKeyDict("skins");
            {
                JsonBuilder.Dict defaultSkin = skins.addKeyDict("default");
                readSkin(input, defaultSkin, nonessential, 0, name);
                if (defaultSkin != null) {
                    skins.insert(defaultSkin);

                }

                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    String name = input.readString();
                    JsonBuilder.Dict skin = skins.addKeyDict(name);
                    readSkin(input, skin, nonessential, i + 1, name);
                    if (skin != null) {
                        skins.insert(skin);
                    }
                }
            }
            basicDict.insert(skins);


            JsonBuilder.Dict events = basicDict.addKeyDict("events");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++) {
                    String name = input.readString();
                    JsonBuilder.Dict event = events.addKeyDict(name);
                    {
                        event.addKeyValue("int", input.readInt(false));
                        event.addKeyValue("float", input.readFloat());
                        String string = input.readString();
                        event.addKeyValue("string", string != null ? string : "null");

                    }
                    events.insert(event);
                    eventName.put(i, name);
                }
            }
            basicDict.insert(events);

        try {
            JsonBuilder.Dict animations = basicDict.addKeyDict("animations");
            {
                for (int i = 0, n = input.readInt(true); i < n; i++)
                    readAnimation(input, input.readString(), animations);
            }
            basicDict.insert(animations);
        }catch (Throwable event){
            failAnimation=true;
            System.out.printf("error to load %s's animations",name);
        }

            builder.insert(basicDict);
        } catch (IOException info) {
            return String.format("{\"error\":%s}", info.toString());
        }
        String value=builder.toString();
        if (failAnimation)value=value.substring(0,value.length()-14)+"}";
        return value;

    }


    private void readSkin(DataInput input, JsonBuilder.Dict skin, boolean nonessential, int index, String name) throws IOException {
        int slotCount = input.readInt(true);
        if (slotCount == 0) return;

        for (int i = 0; i < slotCount; i++) {
            int slotIndex = input.readInt(true);
            String slotName = slotsName.get(slotIndex);
            if (slotName.startsWith("head")) {
                System.out.println(slotName);
            }
            JsonBuilder.Dict attach = skin.addKeyDict(slotName);
            for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
                String attachName = input.readString();

                readAttachment(input, attach, attachName, nonessential);

                skinsName.put(index, name);
            }
            skin.insert(attach);
        }
    }

    private void readAttachment(DataInput input, JsonBuilder.Dict attachments, String attachmentName, boolean nonessential) throws IOException {

        String name = input.readString();
        if (name == null) name = attachmentName;

        JsonBuilder.Dict attach = attachments.addKeyDict(name);
        {
            int type = input.readByte();
            String typeName = AttachmentType[type >= AttachmentType.length ? AttachmentType.length - 1 : type];

            attach.addKeyValue("type", type);

            switch (type) {
                case Attachment_type_region: {
                    String pathName = input.readString();
                    attach.addKeyValue("path", pathName != null ? pathName : name);
                    attach.addKeyValue("rotation", input.readFloat());
                    attach.addKeyValue("x", input.readFloat());
                    attach.addKeyValue("y", input.readFloat());
                    attach.addKeyValue("scaleX", input.readFloat());
                    attach.addKeyValue("scaleY", input.readFloat());
                    attach.addKeyValue("width", input.readFloat());
                    attach.addKeyValue("height", input.readFloat());
                    //color
                    int colorInt = input.readInt();
                    Color color = new Color();
                    Color.rgba8888ToColor(color, colorInt);
                    attach.addKeyValue("color", color.toString());
                    break;
                }
                case Attachment_type_boundingbox: {
                    int vertexCount = input.readInt(true);

                    JsonBuilder.List vertices = attach.addKeyList("vertices");
                    readVertices(input, vertexCount, vertices, attachmentName);

                    attach.insert(vertices);
                    int colorInt = nonessential ? input.readInt() : 0;
                    if (nonessential) {
                        Color color = new Color();
                        Color.rgba8888ToColor(color, colorInt);
                        attach.addKeyValue("color", color.toString());
                    }
                    break;
                }
                case Attachment_type_mesh: {
                    String path = input.readString();
                    if (path == null) path = name;
                    int colorInt = input.readInt();
                    int vertexCount = input.readInt(true);

                    attach.addKeyValue("path", path);

                    attach.addKeyValue("vertexCount", vertexCount);

                    JsonBuilder.List uvs = attach.addKeyList("uvs");
                    readFloatArray(input, vertexCount << 1, 1, uvs);
                    attach.insert(uvs);

                    JsonBuilder.List triangles = attach.addKeyList("triangles");
                    readShortArray(input, triangles);
                    attach.insert(triangles);

                    JsonBuilder.List vertices = attach.addKeyList("vertices");
                    readVertices(input, vertexCount, vertices, attachmentName);
                    attach.insert(vertices);

                    attach.addKeyValue("hull", input.readInt(true));
                    JsonBuilder.List edges = attach.addKeyList("edges");
                    float width = 0, height = 0;
                    if (nonessential) {
                        readShortArray(input, edges);
                        width = input.readFloat();
                        height = input.readFloat();
                    }
                    attach.insert(edges);
                    attach.addKeyValue("width", width);
                    attach.addKeyValue("height", height);

                    Color color = new Color();
                    Color.rgba8888ToColor(color, colorInt);
                    attach.addKeyValue("color", color.toString());

                    break;
                }
                case Attachment_type_linkedmesh: {
                    String path = input.readString();
                    if (path == null) path = name;
                    int colorInt = input.readInt();
                    String skinName = input.readString();
                    String parent = input.readString();
                    boolean inheritDeform = input.readBoolean();
                    float width = 0, height = 0;
                    if (nonessential) {
                        width = input.readFloat();
                        height = input.readFloat();
                    }

                    attach.addKeyValue("path", path);

                    attach.addKeyValue("skin", skinName);
                    attach.addKeyValue("parent", parent);
                    attach.addKeyValue("deform", inheritDeform);
                    attach.addKeyValue("width", width);
                    attach.addKeyValue("height", height);

                    Color color = new Color();
                    Color.rgba8888ToColor(color, colorInt);
                    attach.addKeyValue("color", color.toString());

                    break;
                }
                case Attachment_type_path: {
                    boolean closed = input.readBoolean();
                    boolean constantSpeed = input.readBoolean();
                    int vertexCount = input.readInt(true);

                    attach.addKeyValue("closed", closed);
                    attach.addKeyValue("constantSpeed", constantSpeed);
                    attach.addKeyValue("vertexCount", vertexCount);


                    JsonBuilder.List vertices = attach.addKeyList("vertices");
                    readVertices(input, vertexCount, vertices, attachmentName);
                    attach.insert(vertices);

                    float[] lengths = new float[vertexCount / 3];
                    JsonBuilder.List length = attach.addKeyList("lengths");
                    for (int i = 0, n = lengths.length; i < n; i++) {
                        lengths[i] = input.readFloat();
                        length.addValue(lengths[i]);
                    }
                    attach.insert(length);
                    //color
                    int colorInt = nonessential ? input.readInt() : 0;
                    if (nonessential) {
                        Color color = new Color();
                        Color.rgba8888ToColor(color, colorInt);
                        attach.addKeyValue("color", color.toString());
                    }
                    break;
                }
                case Attachment_type_point: {
                    float rotation = input.readFloat();
                    float x = input.readFloat();
                    float y = input.readFloat();
                    //color
                    int colorInt = nonessential ? input.readInt() : 0;

                    attach.addKeyValue("x", x);
                    attach.addKeyValue("y", y);
                    attach.addKeyValue("rotation", rotation);
                    if (nonessential) {
                        Color color = new Color();
                        Color.rgba8888ToColor(color, colorInt);
                        attach.addKeyValue("color", color.toString());
                    }

                    break;
                }
                case Attachment_type_clippin: {
                    int endSlotIndex = input.readInt(true);
                    int vertexCount = input.readInt(true);

                    attach.addKeyValue("end", slotsName.get(endSlotIndex));
                    attach.addKeyValue("vertexCount", vertexCount);

                    JsonBuilder.List vertices = attach.addKeyList("vertices");
                    readVertices(input, vertexCount, vertices, attachmentName);
                    attach.insert(vertices);

                    //color
                    int colorInt = input.readInt();
                    if (nonessential) {
                        Color color = new Color();
                        Color.rgba8888ToColor(color, colorInt);
                        attach.addKeyValue("color", color.toString());
                    }
                    break;
                }
            }
        }
        attachments.insert(attach);
    }

    private void readVertices(DataInput input, int vertexCount, JsonBuilder.List vertices, String attachName) throws IOException {
        int verticesLength = vertexCount << 1;
        FloatArray array = new FloatArray();
        if (!input.readBoolean()) {
            array = readFloatArray(input, verticesLength, 1f, vertices);
            attachVertices.put(attachName, array);
            return;
        }
        float temp;
        int tempInt;
        for (int i = 0; i < vertexCount; i++) {

            int boneCount = input.readInt(true);
            vertices.addValue(boneCount);
            array.add(boneCount);
            for (int ii = 0; ii < boneCount; ii++) {
                tempInt = input.readInt(true);
                vertices.addValue(tempInt);
                array.add(tempInt);
                temp = input.readFloat();
                vertices.addValue(temp);
                array.add(temp);
                temp = input.readFloat();
                vertices.addValue(temp);
                array.add(temp);
                temp = input.readFloat();
                vertices.addValue(temp);
                array.add(temp);
            }
        }
        attachVertices.put(attachName, array);
    }

    private FloatArray readFloatArray(DataInput input, int n, float scale, JsonBuilder.List floatArray) throws IOException {
        float[] array = new float[n];
        if (scale == 1) {
            for (int i = 0; i < n; i++)
                array[i] = input.readFloat();
        } else {
            for (int i = 0; i < n; i++)
                array[i] = input.readFloat() * scale;
        }
        for (float v : array) {
            floatArray.addValue(v);
        }
        return new FloatArray(array);
    }

    private void readShortArray(DataInput input, JsonBuilder.List shortArray) throws IOException {
        int n = input.readInt(true);
        short[] array = new short[n];
        for (int i = 0; i < n; i++)
            array[i] = input.readShort();

        for (short v :
                array) {
            shortArray.addValue(v);
        }
    }

    private void readAnimation(DataInput input, String name, JsonBuilder.Dict Animations) {
        JsonBuilder.Dict animation = Animations.addKeyDict(name);
        try {
            // Slot timelines.
            int val = input.readInt(true);
            if (val != 0) {
                JsonBuilder.Dict slots = animation.addKeyDict("slots");

                for (int i = 0; i < val; i++) {
                    int slotIndex = input.readInt(true);
                    String slotName = slotsName.get(slotIndex);
                    JsonBuilder.Dict slot = slots.addKeyDict(slotName);
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
                    String boneName = bonesName.get(boneIndex);
                    JsonBuilder.Dict bone = bones.addKeyDict(boneName);

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
                    String ikName = IK_Name.get(index);

                    JsonBuilder.List ik = iks.addKeyList(ikName);
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
                    String transformName = transformsName.get(index);

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
                        String pathName = pathsName.get(index);

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
                    String deformName = skinsName.get(input.readInt(true));
                    JsonBuilder.Dict perDeform = deforms.addKeyDict(deformName);
                    {
                        for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {

                            int slotIndex = input.readInt(true);
                            JsonBuilder.Dict slot = perDeform.addKeyDict(slotsName.get(slotIndex));
                            {
                                for (int iii = 0, nnn = input.readInt(true); iii < nnn; iii++) {
                                    String timeLineName = input.readString();
                                    JsonBuilder.List timeline = slot.addKeyList(timeLineName);
                                    {
                                        boolean weighted = slotAttach.get(slotsName.get(slotIndex)) != null && slotAttach.get(slotsName.get(slotIndex)).equals(timeLineName);
                                        float[] vertices = attachVertices.get(timeLineName)==null?new float[0]:attachVertices.get(timeLineName).toArray();
                                        int deformLength = weighted ? vertices.length / 3 * 2 : vertices.length;

                                        int frameCount = input.readInt(true);
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
                                                    if (end>deformLength||end<0)end=deformLength;
                                                    if (start>deformLength||start<0)start=deformLength;
                                                    for (int v = start; v < end; v++)
                                                        deform[v] = input.readFloat();
                                                    if (!weighted) {
                                                        for (int v = 0, vn = deform.length; v < vn; v++)
                                                            deform[v] += vertices[v];
                                                    }
                                                    perInfo.addKeyValue("offset", start);

                                                    if (frameIndex < frameCount - 1)
                                                        readCurve(input, perInfo);
                                                }
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
                                        offset.addKeyValue("slot", slotsName.get(slotIndex));
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
                            event.addKeyValue("name", eventName.get(input.readInt(true)));

                            event.addKeyValue("int", input.readInt(false));
                            event.addKeyValue("float", input.readFloat());
                            event.addKeyValue("string", input.readString());
                        }
                        events.insert(event);
                    }
                }
                animation.insert(events);
            }

        } catch (Throwable ex) {
            throw new SerializationException("Error reading skeleton file.", ex);
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
