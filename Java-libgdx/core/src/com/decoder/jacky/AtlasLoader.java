package com.decoder.jacky;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtlasLoader {

    private static Pattern value = Pattern.compile(
            "(.+\\n" +
                    "\\s{2}rotate:\\s(?:true|false)\\n" +
                    "\\s{2}xy:\\s\\d+,\\s\\d+\\n" +
                    "\\s{2}size:\\s\\d+,\\s\\d+\\n)");

    Map<String, Array<Integer>> getRegion(FileHandle atlasFile) throws FileNotFoundException {
        if (!atlasFile.exists())
            throw new FileNotFoundException("file:" + atlasFile.path() + "do not exist");
        Map<String, Array<Integer>> reValue = new HashMap<String, Array<Integer>>();

        String fileData = atlasFile.readString().replace("\r\n", "\n");
        Matcher match = value.matcher(fileData);
        int count=0;
        while (match.find()) {
            count++;
            //System.out.println(match.group(0));
            int x, y, w, h;
            String region = match.group();
            String[] perLine = region.split("\n\\s{2}");
            String name = perLine[0];
            String xy = perLine[2].replace("\n",""), size = perLine[3].replace("\n","");
            int post_1 = xy.lastIndexOf(","), post_2 = size.lastIndexOf(",");
            x = Integer.parseInt(xy.substring(4, post_1));
            y = Integer.parseInt(xy.substring(post_1 + 2));
            w = Integer.parseInt(size.substring(6, post_2));
            h = Integer.parseInt(size.substring(post_2 + 2));

            Array<Integer> temp = new Array<Integer>();
            temp.add(x);
            temp.add(y);
            temp.add(w);
            temp.add(h);

            reValue.put(name, temp);
            /*System.out.println(name);
            System.out.println(xy);
            System.out.println(size);
            System.out.println(x);
            System.out.println(y);
            System.out.println(w);
            System.out.println(h);
            */
        }
        return reValue;
    }
}
