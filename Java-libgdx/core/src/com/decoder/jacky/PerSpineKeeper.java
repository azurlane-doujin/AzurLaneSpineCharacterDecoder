package com.decoder.jacky;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.io.FileNotFoundException;
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

    public PerSpineKeeper() {
    }

    public PerSpineKeeper(String atlasPath, String texPath) {
        this("", atlasPath, texPath);
    }

    public PerSpineKeeper(String skeletonPath) {
        this(skeletonPath, "", "");
    }

    public PerSpineKeeper(String skeletonPath, String atlasPath, String texPath) {
        FileHandle temp;
        //is skeleton file exist
        setSkelPath(skeletonPath);
        //is atlas file exist
        setAtlasPath(atlasPath);
        //is texture file exist
        setTexPath(texPath);
    }

    public void setSkelPath(String skelPath) {
        FileHandle temp = Gdx.files.absolute(skelPath);
        if (temp.exists() && (skelPath.toLowerCase().endsWith(".skel.txt") || skelPath.toLowerCase().endsWith(".skel"))) {
            this.skelPath = temp;
            isAble = true;
        }
    }

    public void setAtlasPath(String atlasPath) {
        FileHandle temp = Gdx.files.absolute(atlasPath);
        if (temp.exists() && (atlasPath.toLowerCase().endsWith(".atlas.txt") || atlasPath.toLowerCase().endsWith(".atlas")))
            this.atlasPath = temp;
    }

    public void setTexPath(String texPath) {
        FileHandle temp = Gdx.files.absolute(texPath);
        if (temp.exists() && (texPath.toLowerCase().endsWith(".png"))&&!texPath.toLowerCase().endsWith("[alpha].png"))
            this.texPath = temp;
    }

    public void set(String path) {
        setSkelPath(path);
        setAtlasPath(path);
        setTexPath(path);
    }

    public void setSavePath(FileHandle savePath) {
        if (savePath.isDirectory())
            this.savePath = savePath;
    }

    public boolean isAble() {
        return isAble;
    }

    public boolean TranslateWork() {
        SpineCharacterDecoder decoder = new SpineCharacterDecoder();
        AtlasLoader atlasLoader = new AtlasLoader();
        Map<String, Array<Integer>> atlasInfo = null;
        FileHandle output;
        boolean hasAtlas = false, hasTex = false;
        if (!isAble && !savePath.isDirectory()) return false;
        else {
            if (atlasPath != null) {
                try {
                    atlasInfo = atlasLoader.getRegion(atlasPath);
                    hasAtlas = true;

                    String file_name = atlasPath.nameWithoutExtension().replace(".atlas", "");
                    output = Gdx.files.absolute(savePath + "/output/" + file_name + "/" + file_name + ".atlas");
                    atlasPath.copyTo(output);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (texPath != null) {
                output = Gdx.files.absolute(savePath + "/output/" + texPath.nameWithoutExtension());
                texPath.copyTo(output);
            }
            if (skelPath!=null){
                String jsonString, path = savePath.path();

                if (hasAtlas)
                    jsonString = decoder.decoder(skelPath, atlasInfo);
                else
                    jsonString = decoder.decoder(skelPath, null);

                output = Gdx.files.absolute(path + "/output/" + decoder.name + "/" + decoder.name + ".json");
                output.writeString(jsonString, false);

                name = decoder.name;

                if (jsonString != null && jsonString.startsWith("{\"error\""))
                    System.out.printf("error to load %s!\n", name);
                System.out.printf("finish:\t%s\n", name);

            }
        }
        return true;
    }

    static Array<PerSpineKeeper> loadFromArray(String[] args) {
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
                keeper.name=name;
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
    static Array<PerSpineKeeper> loadFromArray(List<String> args) {
        String []temp=new String[args.size()];
        for (int i = 0; i <args.size(); i++) {
            temp[i]=args.get(i);
        }
        return loadFromArray(temp);
    }


}
