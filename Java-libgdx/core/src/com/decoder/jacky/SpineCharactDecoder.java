package com.decoder.jacky;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpineCharactDecoder {

    private static final int BONE_ROTATE = 0;
    private static final int BONE_TRANSLATE = 1;
    private static final int BONE_SCALE = 2;
    private static final int BONE_SHEAR = 3;

    private static final int SLOT_ATTACHMENT = 0;
    private static final int SLOT_COLOR = 1;
    private static final int SLOT_TWO_COLOR = 2;

    static public final int PATH_POSITION = 0;
    static public final int PATH_SPACING = 1;
    static public final int PATH_MIX = 2;

    static public final int CURVE_LINEAR = 0;
    private static final int CURVE_STEPPED = 1;
    private static final int CURVE_BEZIER = 2;

    final String TransformMode[] = new String[]{
            "normal",
            "onlyTranslation",
            "noRotationOrReflection",
            "noScale",
            "noScaleOrReflection"};

    final String BlendMode[] = new String[]{
            "normal",
            "additive",
            "multiply",
            "screen",
    };
    final String PositionMode[] = new String[]{"fixed",
            "percent"};
    final String SpacingMode[] = new String[]{
            "length",
            "fixed",
            "percent"};
    final String RotateMode[] = new String[]
            {
                    "tangent",
                    "chain",
                    "chainScale"
            };

    final String AttachmentType[] = new String[]{
            "region",
            "boundingbox",
            "mesh",
            "linkedmesh",
            "path",
            "point",
            "clipping"};

    private JsonBuilder builder = new JsonBuilder();
    private JsonBuilder.Dict basicDict = builder.setBasicTypeAsDict();

    public String name;

    public String decoder(FileHandle binaryFile) {
        name=binaryFile.nameWithoutExtension().replace(".skel","");
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
                String version = input.readString();
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

            Map<Integer, String> bonesName = new HashMap<Integer, String>();
            Map<Integer, String> slotsName = new HashMap<Integer, String>();


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
                        bone.addKeyValue("transform", this.TransformMode[transformMode]);

                        if (nonessential) {
                            int color = input.readInt();
                            String color_s = Integer.toHexString(color);
                            bone.addKeyValue("color", color_s.substring(2));
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

                        int color = input.readInt();
                        String color_s = Integer.toHexString(color).substring(2);

                        int darkColor = input.readInt();
                        String darkColor_s = null;
                        if (darkColor != -1)
                            darkColor_s = Integer.toHexString(color);

                        String attachmentName = input.readString();
                        int blendMode = input.readInt(true);

                        slot.addKeyValue("name", slotName != null ? slotName : "null");
                        slot.addKeyValue("bone", boneName != null ? boneName : "null");
                        slot.addKeyValue("color", color_s);
                        if (darkColor_s != null)
                            slot.addKeyValue("dark", darkColor_s);
                        if (attachmentName != null)
                            slot.addKeyValue("attachment", attachmentName);

                        slot.addKeyValue("blend", this.BlendMode[blendMode]);

                        slotsName.put(i, slotName);
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
                        transform.addKeyValue("local", input.readBoolean());
                        transform.addKeyValue("relative", input.readBoolean());
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

                        String positionMode = PositionMode[input.readInt(true)];
                        String spacingMode = SpacingMode[input.readInt(true)];
                        String rotateMode = RotateMode[input.readInt(true)];
                        path.addKeyValue("positionMode", positionMode);
                        path.addKeyValue("spacingMode", spacingMode);
                        path.addKeyValue("rotateMode", rotateMode);

                        path.addKeyValue("offsetRotation", input.readFloat());
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

                    }
                    paths.insert(path);
                }
            }
            basicDict.insert(paths);


            builder.insert(basicDict);
        } catch (IOException info) {
            return String.format("{\"error\":%s}", info.toString());
        }
        return builder.toString();

    }

    public void clear() {
        builder = new JsonBuilder();
    }

    private JsonBuilder.Dict readSkin(DataInput input, JsonBuilder.Dict basicDict, String skinName, boolean nonessential) throws IOException {
        int slotCount = input.readInt(true);
        if (slotCount == 0) return null;
        String skin_Name = skinName;
        JsonBuilder.Dict skin = basicDict.addKeyDict("nn");
        for (int i = 0; i < slotCount; i++) {
            int slotIndex = input.readInt(true);
            for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
                String name = input.readString();
                JsonBuilder.Dict attachment = readAttachment(input, skin, slotIndex, name, nonessential);
                if (attachment != null) {
                    skin.addKeyDict("attachment");
                    skin.insert(attachment);
                }
            }
        }
        return skin;
    }

    private JsonBuilder.Dict readAttachment(DataInput input, JsonBuilder.Dict skin, int slotIndex, String attachmentName,
                                            boolean nonessential) throws IOException {
        // float scale = this.scale;

        String name = input.readString();
        if (name == null) name = attachmentName;

        int type = input.readByte();
        switch (type) {
            case 0: {
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
                //    RegionAttachment region = attachmentLoader.newRegionAttachment(skin, name, path);
                //    if (region == null) return null;
                //    region.setPath(path);
                //    region.setX(x * scale);
                //    region.setY(y * scale);
                //    region.setScaleX(scaleX);
                //    region.setScaleY(scaleY);
                //    region.setRotation(rotation);
                //    region.setWidth(width * scale);
                //    region.setHeight(height * scale);
                //    Color.rgba8888ToColor(region.getColor(), color);
                //    region.updateOffset();
                //  return region;
            }
            case 1: {
                int vertexCount = input.readInt(true);
                //SkeletonBinary.Vertices vertices = readVertices(input, vertexCount);
                int color = nonessential ? input.readInt() : 0;

                //  BoundingBoxAttachment box = attachmentLoader.newBoundingBoxAttachment(skin, name);
                //  if (box == null) return null;
                //  box.setWorldVerticesLength(vertexCount << 1);
                //  box.setVertices(vertices.vertices);
                //  box.setBones(vertices.bones);
                //  if (nonessential) Color.rgba8888ToColor(box.getColor(), color);
                //  return box;
            }
            case 2: {
                String path = input.readString();
                int color = input.readInt();
                int vertexCount = input.readInt(true);
                float[] uvs = readFloatArray(input, vertexCount << 1, 1);
                short[] triangles = readShortArray(input);
                //  SkeletonBinary.Vertices vertices = readVertices(input, vertexCount);
                int hullLength = input.readInt(true);
                short[] edges = null;
                float width = 0, height = 0;
                if (nonessential) {
                    edges = readShortArray(input);
                    width = input.readFloat();
                    height = input.readFloat();
                }

                if (path == null) path = name;
                // MeshAttachment mesh = attachmentLoader.newMeshAttachment(skin, name, path);
                // if (mesh == null) return null;
                // mesh.setPath(path);
                // Color.rgba8888ToColor(mesh.getColor(), color);
                // mesh.setBones(vertices.bones);
                // mesh.setVertices(vertices.vertices);
                // mesh.setWorldVerticesLength(vertexCount << 1);
                // mesh.setTriangles(triangles);
                // mesh.setRegionUVs(uvs);
                // mesh.updateUVs();
                // mesh.setHullLength(hullLength << 1);
                // if (nonessential) {
                //     mesh.setEdges(edges);
                //     mesh.setWidth(width * scale);
                //     mesh.setHeight(height * scale);
                // }
                // return mesh;
            }
            case 3: {
                String path = input.readString();
                int color = input.readInt();
                String skinName = input.readString();
                String parent = input.readString();
                boolean inheritDeform = input.readBoolean();
                float width = 0, height = 0;
                if (nonessential) {
                    width = input.readFloat();
                    height = input.readFloat();
                }

                if (path == null) path = name;
                //  MeshAttachment mesh = attachmentLoader.newMeshAttachment(skin, name, path);
                //  if (mesh == null) return null;
                //  mesh.setPath(path);
                //  Color.rgba8888ToColor(mesh.getColor(), color);
                //  mesh.setInheritDeform(inheritDeform);
                //  if (nonessential) {
                //      mesh.setWidth(width * scale);
                //      mesh.setHeight(height * scale);
                //  }
                //  linkedMeshes.add(new SkeletonJson.LinkedMesh(mesh, skinName, slotIndex, parent));
                //  return mesh;
            }
            case 4: {
                boolean closed = input.readBoolean();
                boolean constantSpeed = input.readBoolean();
                int vertexCount = input.readInt(true);
                //   SkeletonBinary.Vertices vertices = readVertices(input, vertexCount);
                float[] lengths = new float[vertexCount / 3];
                for (int i = 0, n = lengths.length; i < n; i++)
                    lengths[i] = input.readFloat();
                int color = nonessential ? input.readInt() : 0;

                //  PathAttachment path = attachmentLoader.newPathAttachment(skin, name);
                //  if (path == null) return null;
                //  path.setClosed(closed);
                //  path.setConstantSpeed(constantSpeed);
                //  path.setWorldVerticesLength(vertexCount << 1);
                //  path.setVertices(vertices.vertices);
                //  path.setBones(vertices.bones);
                //  path.setLengths(lengths);
                //  if (nonessential) Color.rgba8888ToColor(path.getColor(), color);
                //  return path;
            }
            case 5: {
                float rotation = input.readFloat();
                float x = input.readFloat();
                float y = input.readFloat();
                int color = nonessential ? input.readInt() : 0;

                //  PointAttachment point = attachmentLoader.newPointAttachment(skin, name);
                //  if (point == null) return null;
                //  point.setX(x * scale);
                //  point.setY(y * scale);
                //  point.setRotation(rotation);
                //  if (nonessential) Color.rgba8888ToColor(point.getColor(), color);
                //  return point;
            }
            case 6: {
                int endSlotIndex = input.readInt(true);
                int vertexCount = input.readInt(true);
                //      SkeletonBinary.Vertices vertices = readVertices(input, vertexCount);
                int color = nonessential ? input.readInt() : 0;

                //  ClippingAttachment clip = attachmentLoader.newClippingAttachment(skin, name);
                //  if (clip == null) return null;
                //  clip.setEndSlot(skeletonData.slots.get(endSlotIndex));
                //  clip.setWorldVerticesLength(vertexCount << 1);
                //  clip.setVertices(vertices.vertices);
                //  clip.setBones(vertices.bones);
                //  if (nonessential) Color.rgba8888ToColor(clip.getColor(), color);
                //  return clip;
            }
        }
        return null;
    }

    private JsonBuilder.List readVertices(DataInput input, int vertexCount, JsonBuilder.Dict basicDict) throws IOException {
        int verticesLength = vertexCount << 1;
        float scale = 1f;
        JsonBuilder.List vertices = basicDict.addKeyList("ver");
        //   SkeletonBinary.Vertices vertices = new SkeletonBinary.Vertices();
        if (!input.readBoolean()) {
            //  vertices.vertices = readFloatArray(input, verticesLength, scale);

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
        vertices.addValue(weights.toArray());
        vertices.addValue(bonesArray.toArray());
        return vertices;
    }

    private float[] readFloatArray(DataInput input, int n, float scale) throws IOException {
        float[] array = new float[n];
        if (scale == 1) {
            for (int i = 0; i < n; i++)
                array[i] = input.readFloat();
        } else {
            for (int i = 0; i < n; i++)
                array[i] = input.readFloat() * scale;
        }
        return array;
    }

    private short[] readShortArray(DataInput input) throws IOException {
        int n = input.readInt(true);
        short[] array = new short[n];
        for (int i = 0; i < n; i++)
            array[i] = input.readShort();
        return array;
    }

    private void readAnimation(DataInput input, String name, JsonBuilder.Dict basicDict) {
        JsonBuilder.List timelines = basicDict.addKeyList("timmeline");
        float scale = 1f;
        float duration = 0;

        try {
            // Slot timelines.
            for (int i = 0, n = input.readInt(true); i < n; i++) {
                int slotIndex = input.readInt(true);
                for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
                    int timelineType = input.readByte();
                    int frameCount = input.readInt(true);
                    switch (timelineType) {
                        case SLOT_ATTACHMENT: {
                            //Animation.AttachmentTimeline timeline = new Animation.AttachmentTimeline(frameCount);
                            //timeline.slotIndex = slotIndex;
                            //for (int frameIndex = 0; frameIndex < frameCount; frameIndex++)
                            //    timeline.setFrame(frameIndex, input.readFloat(), input.readString());
                            //timelines.add(timeline);
                            //duration = Math.max(duration, timeline.getFrames()[frameCount - 1]);
                            break;
                        }
                        case SLOT_COLOR: {
                            //   Animation.ColorTimeline timeline = new Animation.ColorTimeline(frameCount);
                            //   timeline.slotIndex = slotIndex;
                            //   for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                            //       float time = input.readFloat();
                            //      // Color.rgba8888ToColor(tempColor1, input.readInt());
                            //      // timeline.setFrame(frameIndex, time, tempColor1.r, tempColor1.g, tempColor1.b, tempColor1.a);
                            //       if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
                            //   }
                            //   timelines.add(timeline);
                            //   duration = Math.max(duration, timeline.getFrames()[(frameCount - 1) * Animation.ColorTimeline.ENTRIES]);
                            break;
                        }
                        case SLOT_TWO_COLOR: {
                            //  Animation.TwoColorTimeline timeline = new Animation.TwoColorTimeline(frameCount);
                            //  timeline.slotIndex = slotIndex;
                            //  for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                            //      float time = input.readFloat();
                            //    //  Color.rgba8888ToColor(tempColor1, input.readInt());
                            //    //  Color.rgb888ToColor(tempColor2, input.readInt());
                            //    //  timeline.setFrame(frameIndex, time, tempColor1.r, tempColor1.g, tempColor1.b, tempColor1.a, tempColor2.r,
                            //    //          tempColor2.g, tempColor2.b);
                            //      if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
                            //  }
                            //  timelines.add(timeline);
                            //  duration = Math.max(duration, timeline.getFrames()[(frameCount - 1) * Animation.TwoColorTimeline.ENTRIES]);
                            break;
                        }
                    }
                }
            }

            // Bone timelines.
            for (int i = 0, n = input.readInt(true); i < n; i++) {
                int boneIndex = input.readInt(true);
                for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
                    int timelineType = input.readByte();
                    int frameCount = input.readInt(true);
                    switch (timelineType) {
                        case BONE_ROTATE: {
                            //    Animation.RotateTimeline timeline = new Animation.RotateTimeline(frameCount);
                            //    timeline.boneIndex = boneIndex;
                            //    for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                            //        timeline.setFrame(frameIndex, input.readFloat(), input.readFloat());
                            //        if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
                            //    }
                            //    timelines.add(timeline);
                            //    duration = Math.max(duration, timeline.getFrames()[(frameCount - 1) * Animation.RotateTimeline.ENTRIES]);
                            break;
                        }
                        case BONE_TRANSLATE:
                        case BONE_SCALE:
                        case BONE_SHEAR: {
                            //      Animation.TranslateTimeline timeline;
                            //      float timelineScale = 1;
                            //      if (timelineType == BONE_SCALE)
                            //          timeline = new Animation.ScaleTimeline(frameCount);
                            //      else if (timelineType == BONE_SHEAR)
                            //          timeline = new Animation.ShearTimeline(frameCount);
                            //      else {
                            //          timeline = new Animation.TranslateTimeline(frameCount);
                            //          timelineScale = scale;
                            //      }
                            //      timeline.boneIndex = boneIndex;
                            //      for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                            //          timeline.setFrame(frameIndex, input.readFloat(), input.readFloat() * timelineScale,
                            //                  input.readFloat() * timelineScale);
                            //          if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
                            //      }
                            //      timelines.add(timeline);
                            //      duration = Math.max(duration, timeline.getFrames()[(frameCount - 1) * Animation.TranslateTimeline.ENTRIES]);
                            break;
                        }
                    }
                }
            }

            // IK constraint timelines.
            for (int i = 0, n = input.readInt(true); i < n; i++) {
                // int index = input.readInt(true);
                // int frameCount = input.readInt(true);
                // Animation.IkConstraintTimeline timeline = new Animation.IkConstraintTimeline(frameCount);
                // timeline.ikConstraintIndex = index;
                // for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                //     timeline.setFrame(frameIndex, input.readFloat(), input.readFloat(), input.readByte());
                //     if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
                // }
                // timelines.add(timeline);
                // duration = Math.max(duration, timeline.getFrames()[(frameCount - 1) * Animation.IkConstraintTimeline.ENTRIES]);
            }

            // Transform constraint timelines.
            for (int i = 0, n = input.readInt(true); i < n; i++) {
                //  int index = input.readInt(true);
                //  int frameCount = input.readInt(true);
                //  Animation.TransformConstraintTimeline timeline = new Animation.TransformConstraintTimeline(frameCount);
                //  timeline.transformConstraintIndex = index;
                //  for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                //      timeline.setFrame(frameIndex, input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat(),
                //              input.readFloat());
                //      if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
                //  }
                //  timelines.add(timeline);
                //  duration = Math.max(duration, timeline.getFrames()[(frameCount - 1) * Animation.TransformConstraintTimeline.ENTRIES]);
            }

            // Path constraint timelines.
            for (int i = 0, n = input.readInt(true); i < n; i++) {
                //  int index = input.readInt(true);
                //  PathConstraintData data = skeletonData.pathConstraints.get(index);
                //  for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
                //      int timelineType = input.readByte();
                //      int frameCount = input.readInt(true);
                //      switch (timelineType) {
                //          case PATH_POSITION:
                //          case PATH_SPACING: {
                //              Animation.PathConstraintPositionTimeline timeline;
                //              float timelineScale = 1;
                //              if (timelineType == PATH_SPACING) {
                //                  timeline = new Animation.PathConstraintSpacingTimeline(frameCount);
                //                  if (data.spacingMode == PathConstraintData.SpacingMode.length || data.spacingMode == PathConstraintData.SpacingMode.fixed) timelineScale = scale;
                //              } else {
                //                  timeline = new Animation.PathConstraintPositionTimeline(frameCount);
                //                  if (data.positionMode == PathConstraintData.PositionMode.fixed) timelineScale = scale;
                //              }
                //              timeline.pathConstraintIndex = index;
                //              for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                //                  timeline.setFrame(frameIndex, input.readFloat(), input.readFloat() * timelineScale);
                //                  if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
                //              }
                //              timelines.add(timeline);
                //              duration = Math.max(duration, timeline.getFrames()[(frameCount - 1) * Animation.PathConstraintPositionTimeline.ENTRIES]);
                //              break;
                //          }
                //          case PATH_MIX: {
                //              Animation.PathConstraintMixTimeline timeline = new Animation.PathConstraintMixTimeline(frameCount);
                //              timeline.pathConstraintIndex = index;
                //              for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                //                  timeline.setFrame(frameIndex, input.readFloat(), input.readFloat(), input.readFloat());
                //                  if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
                //              }
                //              timelines.add(timeline);
                //              duration = Math.max(duration, timeline.getFrames()[(frameCount - 1) * Animation.PathConstraintMixTimeline.ENTRIES]);
                //             break;
            }


            // Deform timelines.
            for (int i = 0, n = input.readInt(true); i < n; i++) {
                //           Skin skin = skeletonData.skins.get(input.readInt(true));
                //           for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
                //               int slotIndex = input.readInt(true);
                //               for (int iii = 0, nnn = input.readInt(true); iii < nnn; iii++) {
                //                   VertexAttachment attachment = (VertexAttachment)skin.getAttachment(slotIndex, input.readString());
                //                   boolean weighted = attachment.getBones() != null;
                //                   float[] vertices = attachment.getVertices();
                //                   int deformLength = weighted ? vertices.length / 3 * 2 : vertices.length;

                //                   int frameCount = input.readInt(true);
                //                   Animation.DeformTimeline timeline = new Animation.DeformTimeline(frameCount);
                //                   timeline.slotIndex = slotIndex;
                //                   timeline.attachment = attachment;

                //                   for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                //                       float time = input.readFloat();
                //                       float[] deform;
                //                       int end = input.readInt(true);
                //                       if (end == 0)
                //                           deform = weighted ? new float[deformLength] : vertices;
                //                       else {
                //                           deform = new float[deformLength];
                //                           int start = input.readInt(true);
                //                           end += start;
                //                           if (scale == 1) {
                //                               for (int v = start; v < end; v++)
                //                                   deform[v] = input.readFloat();
                //                           } else {
                //                               for (int v = start; v < end; v++)
                //                                   deform[v] = input.readFloat() * scale;
                //                           }
                //                           if (!weighted) {
                //                               for (int v = 0, vn = deform.length; v < vn; v++)
                //                                   deform[v] += vertices[v];
                //                           }
                //                       }

                //                       timeline.setFrame(frameIndex, time, deform);
                //                       if (frameIndex < frameCount - 1) readCurve(input, frameIndex, timeline);
                //                   }
                //                   timelines.add(timeline);
                //                   duration = Math.max(duration, timeline.getFrames()[frameCount - 1]);
                //               }
                //           }
            }

            // Draw order timeline.
            int drawOrderCount = input.readInt(true);
            if (drawOrderCount > 0) {
                //            Animation.DrawOrderTimeline timeline = new Animation.DrawOrderTimeline(drawOrderCount);
                //            int slotCount = skeletonData.slots.size;
                //            for (int i = 0; i < drawOrderCount; i++) {
                //                float time = input.readFloat();
                //                int offsetCount = input.readInt(true);
                //                int[] drawOrder = new int[slotCount];
                //                for (int ii = slotCount - 1; ii >= 0; ii--)
                //                    drawOrder[ii] = -1;
                //                int[] unchanged = new int[slotCount - offsetCount];
                //                int originalIndex = 0, unchangedIndex = 0;
                //                for (int ii = 0; ii < offsetCount; ii++) {
                //                    int slotIndex = input.readInt(true);
                //                    // Collect unchanged items.
                //                    while (originalIndex != slotIndex)
                //                        unchanged[unchangedIndex++] = originalIndex++;
                //                    // Set changed items.
                //                    drawOrder[originalIndex + input.readInt(true)] = originalIndex++;
                //                }
                //                // Collect remaining unchanged items.
                //                while (originalIndex < slotCount)
                //                    unchanged[unchangedIndex++] = originalIndex++;
                //                // Fill in unchanged items.
                //                for (int ii = slotCount - 1; ii >= 0; ii--)
                //                    if (drawOrder[ii] == -1) drawOrder[ii] = unchanged[--unchangedIndex];
                //                timeline.setFrame(i, time, drawOrder);
                //            }
                //            timelines.add(timeline);
                //            duration = Math.max(duration, timeline.getFrames()[drawOrderCount - 1]);
                //        }
//
                //        // Event timeline.
                //        int eventCount = input.readInt(true);
                //        if (eventCount > 0) {
                //            Animation.EventTimeline timeline = new Animation.EventTimeline(eventCount);
                //            for (int i = 0; i < eventCount; i++) {
                //                float time = input.readFloat();
                //                EventData eventData = skeletonData.events.get(input.readInt(true));
                //                Event event = new Event(time, eventData);
                //                event.intValue = input.readInt(false);
                //                event.floatValue = input.readFloat();
                //                event.stringValue = input.readBoolean() ? input.readString() : eventData.stringValue;
                //                timeline.setFrame(i, event);
                //            }
                //            timelines.add(timeline);
                //            duration = Math.max(duration, timeline.getFrames()[eventCount - 1]);
            }
        } catch (IOException ex) {
            throw new SerializationException("Error reading skeleton file.", ex);
        }

        //timelines.shrink();
        //skeletonData.animations.add(new Animation(name, timelines, duration));

    }

    private void readCurve(DataInput input, int frameIndex, JsonBuilder.List timeline) throws IOException {
        switch (input.readByte()) {
            case CURVE_STEPPED:
                // timeline.setStepped(frameIndex);
                break;
            case CURVE_BEZIER:
                setCurve(timeline, frameIndex, input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat());
                break;
        }
    }

    void setCurve(JsonBuilder.List timeline, int frameIndex, float cx1, float cy1, float cx2, float cy2) {
        //timeline.setCurve(frameIndex, cx1, cy1, cx2, cy2);
    }

}
