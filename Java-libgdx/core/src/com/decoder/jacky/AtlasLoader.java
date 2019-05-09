package com.decoder.jacky;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.FloatArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtlasLoader {

    private final FileHandle atlasFile;

    private String inputString;

    private final Pattern pattern=Pattern.compile(
            ".+\\n" +
            "\\s(2)rotate: (false|true)\\n" +
            "\\s(2)xy: \\d+, \\d+\\n" +
            "\\s(2)size: \\d+, \\d+\\n" +
            "\\s(2)orig: \\d+, \\d+\\n" +
            "\\s(2)offset: \\d+, \\d+\\n" +
            "\\s(2)index: -?\\d+\\n");
    private final  Pattern name =Pattern.compile("^.+\\n");
    private final Pattern xy=Pattern.compile("\\s(2)xy: \\d+, \\d+\\n");
    private final Pattern size=Pattern.compile("\\s(2)size: \\d+, \\d+\\n");

    public AtlasLoader(FileHandle atlasFile)
    {
        this.atlasFile=atlasFile;
    }

    public Map<String, FloatArray> load()
    {
        inputString=atlasFile.readString();

        Map<String, FloatArray> information=new HashMap<String, FloatArray>();
        String[] pattens =inputString.split("none\\n")[1].split("-1\\n");

        for (String value :pattens) {
            String[] info=value.split("\\n\\s\\s");
            String slotName = info[0];
            String xy=info[2];
            xy=xy.substring(4);
            String[] x_y=xy.split(",\\s");
            int x=Integer.parseInt(x_y[0]),
                    y=Integer.parseInt(x_y[1]);

            String size=info[3];
            size=size.substring(6);
            String[] size_wh=size.split(",\\s");
            float width=Integer.parseInt(size_wh[0]),
                    height=Integer.parseInt(size_wh[1]);

            FloatArray array=new FloatArray(4);
            array.add(x);
            array.add(y);
            array.add(width);
            array.add(height);

            information.put(slotName,array);


        }

        return information;
    }


}
