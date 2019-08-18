package com.decoder.jacky.SkeletonData;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DataInput;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.decoder.jacky.JsonBuilder;
import com.decoder.jacky.SkeletonData.Attachment.Attachment;
import com.decoder.jacky.SkeletonData.Attachment.VertexAttachment;

import java.io.IOException;

public class Skin extends BasicData {
    static private final Key lookup = new Key();
    public String name;
    private ObjectMap<Key, Attachment> attachments = new ObjectMap<Key, Attachment>();
    private Pool<Key> keyPool = new Pool<Key>(64) {
        protected Key newObject() {
            return new Key();
        }
    };
    private SkeletonData data;

    private Skin(String name) {
        super(name);
        this.name = name;
    }

    static public Skin loadData(
            DataInput input,
            boolean nonessential,
            SkeletonData skeletonData,
            int count,
            String skinName,
            TextureAtlas atlas,
            float scaleNum) {
        try {
            int slotCount = input.readInt(true);
            if (slotCount == 0) return null;
            Skin skin = new Skin(skinName);
            for (int i = 0; i < slotCount; i++) {
                int slotIndex = input.readInt(true);
                for (int ii = 0, nn = input.readInt(true); ii < nn; ii++) {
                    String name = input.readString();
                    Attachment attachment = Attachment.loadData(input, skin, name, nonessential, atlas, scaleNum);
                    if (attachment != null) skin.addAttachment(slotIndex, name, attachment);
                }
            }
            skeletonData.skin.add(skin);
            skin.data = skeletonData;
            return skin;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void buildJsonDict(JsonBuilder.Dict dict) {
        for (int i = 0; i < data.slots.size; i++) {
            Array<Key> keys = new Array<Key>();
            findNamesForSlot(i, keys);
            JsonBuilder.Dict slot = dict.addKeyDict(data.slots.get(i).name);
            for (Key key : keys) {
                Attachment attachment = attachments.get(key);
                attachment.buildJson(slot);
            }
            dict.insert(slot);
        }
    }

    private void addAttachment(int slotIndex, String name, Attachment attachment) {
        Key key = keyPool.obtain();
        key.set(slotIndex, name);
        attachments.put(key, attachment);
    }

    public void findNamesForSlot(int slotIndex, Array<Key> keys) {
        for (Key key : attachments.keys())
            if (key.slotIndex == slotIndex) keys.add(key);
    }

    public Attachment getAttachment(int slotIndex, String name) {
        lookup.set(slotIndex, name);
        return attachments.get(lookup);
    }

    static class Key {
        int slotIndex;
        String name;
        int hashCode;

        public void set(int slotIndex, String name) {
            if (name == null) throw new IllegalArgumentException("name cannot be null.");
            this.slotIndex = slotIndex;
            this.name = name;
            hashCode = 31 * (31 + name.hashCode()) + slotIndex;
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object object) {
            if (object == null) return false;
            Key other = (Key) object;
            if (slotIndex != other.slotIndex) return false;
            if (!name.equals(other.name)) return false;
            return true;
        }

        public String toString() {
            return slotIndex + ":" + name;
        }
    }
}
