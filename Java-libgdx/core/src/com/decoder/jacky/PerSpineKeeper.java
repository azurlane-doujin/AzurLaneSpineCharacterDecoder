package com.decoder.jacky;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PerSpineKeeper {
    private FileHandle
            skelPath,
            atlasPath,
            texPath,
            savePath;

    private boolean isAble;

    public String name;
    public float scale = 1.0f;

    private PerSpineKeeper() {
    }

    private void setSkelPath(String skelPath) {
        FileHandle temp = Gdx.files.absolute(skelPath);
        if (temp.exists() && (skelPath.toLowerCase().endsWith(".skel.txt") || skelPath.toLowerCase().endsWith(".skel"))) {
            this.skelPath = temp;
            isAble = true;
        }
    }

    private void setAtlasPath(String atlasPath) {
        FileHandle temp = Gdx.files.absolute(atlasPath);
        if (temp.exists() && (atlasPath.toLowerCase().endsWith(".atlas.txt") || atlasPath.toLowerCase().endsWith(".atlas")))
            this.atlasPath = temp;
    }

    private void setTexPath(String texPath) {
        FileHandle temp = Gdx.files.absolute(texPath);
        if (temp.exists() && (texPath.toLowerCase().endsWith(".png")) && !texPath.toLowerCase().endsWith("[alpha].png"))
            this.texPath = temp;
    }

    private void set(String path) {
        setSkelPath(path);
        setAtlasPath(path);
        setTexPath(path);
    }

    void setSavePath(FileHandle savePath) {
        if (savePath.isDirectory())
            this.savePath = savePath;
    }

    void TranslateWork() {
        SpineCharacterDecoder decoder = new SpineCharacterDecoder();
        TextureAtlas atlasInfo = null;
        FileHandle output;
        boolean hasAtlas = false;
        if (!(!isAble && !savePath.isDirectory())) {
            if (atlasPath != null) {
                atlasInfo = new TextureAtlas(atlasPath);
                hasAtlas = true;

                String file_name = atlasPath.nameWithoutExtension().replace(".atlas", "");
                output = Gdx.files.absolute(savePath + "/output/" + file_name + "/" + file_name + ".atlas");
                atlasPath.copyTo(output);
            }
            if (texPath != null) {
                output = Gdx.files.absolute(savePath + "/output/" + texPath.nameWithoutExtension());
                texPath.copyTo(output);
            }
            if (skelPath != null) {
                String jsonString, path = savePath.path();

                System.out.println(String.format("now work with %s", name));
                if (hasAtlas)
                    jsonString = decoder.decoder(skelPath, atlasInfo, scale);
                else
                    jsonString = decoder.decoder(skelPath, null, scale);

                output = Gdx.files.absolute(path + "/output/" + decoder.name + "/" + decoder.name + ".json");
                output.writeString(jsonString, false);

                name = decoder.name;

                if (jsonString != null && jsonString.startsWith("{\"error\""))
                    System.out.printf("error to load %s!\n", name);
                System.out.printf("finish:\t%s\nsave at:\n\t%s\n", name, output.path());
            }
        }
    }

    static Array<PerSpineKeeper> loadFromArray(String[] args, float scale) {
        Map<String, PerSpineKeeper> array = new HashMap<String, PerSpineKeeper>();
        String path, name;
        FileHandle fileHandle;

        for (String arg : args) {
            path = arg;
            fileHandle = Gdx.files.absolute(path.replace(".txt", ""));
            String[] arr = fileHandle.pathWithoutExtension().split("/");
            name = arr[arr.length - 1];
            if (array.containsKey(name)) {
                array.get(name).set(path);
            } else {
                PerSpineKeeper keeper = new PerSpineKeeper();
                keeper.set(path);
                keeper.name = name;
                keeper.scale = scale;
                array.put(name, keeper);
            }
        }
        Array<PerSpineKeeper> keeperArray = new Array<PerSpineKeeper>();
        Iterator<String> keySet = array.keySet().iterator();
        String key;
        while (keySet.hasNext()) {
            key = keySet.next();
            keeperArray.add(array.get(key));
        }
        return keeperArray;
    }

    static Array<PerSpineKeeper> loadFromArray(List<String> args, float scale) {
        String[] temp = new String[args.size()];
        for (int i = 0; i < args.size(); i++) {
            temp[i] = args.get(i);
        }
        return loadFromArray(temp, scale);
    }
}
