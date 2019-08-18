package com.decoder.jacky.SkeletonData.Attachment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.DataInput;
import com.badlogic.gdx.utils.ShortArray;
import com.decoder.jacky.JsonBuilder;
import com.decoder.jacky.SkeletonData.EnumGroup.AttachmentType;
import com.decoder.jacky.SkeletonData.Skin;
import com.decoder.jacky.SkeletonData.Vertices;

import java.io.IOException;

import static com.decoder.jacky.SkeletonData.SkeletonData.readFloatArray;
import static com.decoder.jacky.SkeletonData.SkeletonData.readShortArray;

public class MeshAttachment extends VertexAttachment {
    private TextureRegion region=new TextureRegion();
    private String path;
    private float[] regionUVs, worldVertices;
    private short[] triangles;
    private final Color color = new Color(1, 1, 1, 1);
    private int hullLength;
    private boolean inheritDeform;

    // Nonessential.
    private short[] edges;
    private float width, height;

    private int vertexCount;
    private float[] uvs;
    private AttachmentType type = AttachmentType.mesh;

    private String skinName, parent, name;
    private Vertices verticesData;

    private MeshAttachment(Skin skin, String name, String path, TextureAtlas atlas) {
        super(skin, name, path, atlas);
        this.name = name;
    }

    static public MeshAttachment loadData(DataInput input, boolean nonessential, String name,
                                          Skin skin, TextureAtlas atlas, float scale, AttachmentType type) {
        try {
            if (type == AttachmentType.mesh) {
                String path = input.readString();
                int color = input.readInt();
                int vertexCount = input.readInt(true);
                float[] uvs = readFloatArray(input, vertexCount << 1, 1);
                short[] triangles = readShortArray(input);
                Vertices vertices = Vertices.loadData(input, vertexCount);

                int hullLength = input.readInt(true);
                short[] edges = null;
                float width = 0, height = 0;
                if (nonessential) {
                    edges = readShortArray(input);
                    width = input.readFloat();
                    height = input.readFloat();
                }

                if (path == null) path = name;
                MeshAttachment mesh = new MeshAttachment(skin, name, path, atlas);
                if (mesh.name == null) return null;

                mesh.path = (path);
                mesh.vertexCount = vertexCount;
                mesh.uvs = uvs;
                mesh.triangles = triangles;
                Color.rgba8888ToColor(mesh.color, color);
                if (vertices != null) {
                    mesh.bones = (vertices.bones);
                    mesh.vertices = (vertices.vertices);
                }
                mesh.worldVerticesLength = (vertexCount << 1);
                mesh.triangles = (triangles);
                mesh.regionUVs = (uvs);
                mesh.updateUVs();
                mesh.hullLength = (hullLength);
                if (nonessential) {
                    mesh.edges = (edges);
                    mesh.width = (width * scale);
                    mesh.height = (height * scale);
                } else {
                    ShortArray temp = new ShortArray();
                    short data;
                    for (int i = 0; i < mesh.hullLength - 1; i++) {
                        data = (short) (2 * i);
                        temp.add(data);
                        data = (short) (2 * i + 2);
                        temp.add(data);
                    }
                    temp.add((short) 0);
                    temp.add((short) (2 * hullLength - 2));
                    mesh.edges = temp.toArray();
                    if (atlas != null) {
                        TextureAtlas.AtlasRegion regionData = atlas.findRegion(path);
                        mesh.width = regionData.getRegionWidth();
                        mesh.height = regionData.getRegionHeight();
                    }
                }
                mesh.type = type;
                mesh.verticesData = vertices;
                return mesh;

            } else if (type == AttachmentType.linkedmesh) {
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
                MeshAttachment mesh = new MeshAttachment(skin, name, path, atlas);
                if (mesh.name == null) return null;
                mesh.path = (path);
                Color.rgba8888ToColor(mesh.color, color);
                mesh.inheritDeform = (inheritDeform);
                mesh.skinName = skinName;
                mesh.parent = parent;
                if (nonessential) {
                    mesh.width = (width * scale);
                    mesh.height = (height * scale);
                }
                mesh.type = type;
                return mesh;
            } else return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void buildJsonDict(JsonBuilder.Dict attach) {
        if (type == AttachmentType.mesh) {
            attach.addKeyValue("type", "mesh");
            attach.addKeyValue("path", path);

            attach.addKeyValue("vertexCount", vertexCount);

            attach.addKeyArray("uvs", uvs);
            attach.addKeyArray("triangles", triangles);

            verticesData.buildJson(attach);

            attach.addKeyValue("hull", hullLength);
            if (edges != null) {
                attach.addKeyArray("edges", edges);
                attach.addKeyValue("width", width);
                attach.addKeyValue("height", height);
            }
            attach.addKeyValue("color", color.toString());
        }
        if (type == AttachmentType.linkedmesh) {
            attach.addKeyValue("type", "linkedmesh");
            attach.addKeyValue("path", path);
            attach.addKeyValue("skin", skinName);
            attach.addKeyValue("parent", parent);
            attach.addKeyValue("deform", inheritDeform);
            attach.addKeyValue("width", width);
            attach.addKeyValue("height", height);
            attach.addKeyValue("color", color.toString());
        }
    }

    private void updateUVs() {
        float[] regionUVs = this.regionUVs;
        int verticesLength = regionUVs.length;
        int worldVerticesLength = (verticesLength >> 1) * 5;
        if (worldVertices == null || worldVertices.length != worldVerticesLength)
            worldVertices = new float[worldVerticesLength];

        float u, v, width, height;
        if (region == null) {
            u = v = 0;
            width = height = 1;
        } else {
            u = region.getU();
            v = region.getV();
            width = region.getU2() - u;
            height = region.getV2() - v;
        }
        if (region instanceof TextureAtlas.AtlasRegion && ((TextureAtlas.AtlasRegion) region).rotate) {
            for (int i = 0, w = 3; i < verticesLength; i += 2, w += 5) {
                worldVertices[w] = u + regionUVs[i + 1] * width;
                worldVertices[w + 1] = v + height - regionUVs[i] * height;
            }
        } else {
            for (int i = 0, w = 3; i < verticesLength; i += 2, w += 5) {
                worldVertices[w] = u + regionUVs[i] * width;
                worldVertices[w + 1] = v + regionUVs[i + 1] * height;
            }
        }
    }
}
