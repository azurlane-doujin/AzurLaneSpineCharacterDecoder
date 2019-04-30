package com.decoder.jacky;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.*;

public class AzurLaneSpineCharacterDecoder extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture img;

	private List<String> args = new ArrayList<String>();
	private OrthographicCamera camera;

	private SpineCharacterDecoder decoder;

	private int index = 0;
	private String path;
	private boolean hasDone = false;
	private boolean jsonType = false;

	private JsonValue jsonValue;
	private String val;
	private int size = 0;
	private String[] array;
	private FileHandle output;

	private String[] type = new String[]{"skeleton", "Texture", "Atlas"};
	int indexType = 0;

	private boolean nullType=false;

	private float[][] colors={
		{1,1,1,1},
		{0,1,0,1},
		{1,0,0,1},
		{0,0,1,1},
	};
	private String name;

	public AzurLaneSpineCharacterDecoder(String[] args) {
		if (args.length>0){
			if (args.length == 1 && args[0].endsWith(".json")) {
				jsonType = true;
				this.args.add(args[0]);
			} else {
				for (String arg : args) {
					if (arg.endsWith(".skel.txt") || arg.endsWith(".skel"))
						this.args.add(arg);
				}
				size = this.args.size();
			}
			this.path = System.getProperty("user.dir");
			nullType=false;
		}
		else {
			nullType=true;
		}
	}

	@Override
	public void create() {
		batch = new SpriteBatch();
		img = new Texture("core/assets/WatchDog.png");
		camera = new OrthographicCamera();
		camera.setToOrtho(false);

		decoder = new SpineCharacterDecoder();

		if (jsonType) {
			JsonReader reader = new JsonReader();

			jsonValue = reader.parse(Gdx.files.absolute(args.get(0)));

			System.out.println();
			array = jsonValue.asStringArray();
			size = array.length;

		}
	}

	@Override
	public void render() {
		float[] showColor=new float[4];
		if (nullType) showColor=colors[2];
		else if (hasDone)showColor=colors[3];
		else showColor=colors[0];

		Gdx.gl.glClearColor(showColor[0],showColor[1],showColor[2],showColor[3]);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, Gdx.graphics.getWidth() / 2 - img.getWidth() / 2, Gdx.graphics.getHeight() / 2 - img.getHeight() / 2);
		batch.end();

		if (!nullType){
			if (index < size) {
				if (jsonType) {
					String fileName = array[index];
					if (fileName != null && (fileName.endsWith(".skel.txt") || fileName.endsWith(".skel"))) {
						indexType = 0;
						FileHandle input = Gdx.files.absolute(fileName);
						if (input.exists())
							val = decoder.decoder(input);
					} else if (fileName != null && (fileName.toLowerCase().endsWith(".png"))) {
						indexType = 1;
						FileHandle file = Gdx.files.absolute(fileName);
						output = Gdx.files.absolute(path + "/output/" + file.nameWithoutExtension());
						file.copyTo(output);
					} else if (fileName != null && (fileName.toLowerCase().endsWith(".atlas.txt") || fileName.toLowerCase().endsWith(".atlas"))) {
						indexType = 2;
						FileHandle file = Gdx.files.absolute(fileName);
						String file_name = file.nameWithoutExtension().replace(".atlas", "");
						output = Gdx.files.absolute(path + "/output/" + file_name + "/" + file_name + ".atlas");
						file.copyTo(output);
					}
				}
				else
					val = decoder.decoder(Gdx.files.absolute(args.get(index)));

				float a = ((index + 1) * 100f) / size;
				System.out.printf("finish\t%s\t%s\n\tN0.%d,\n\t%.2f%%\n", name, type[indexType], index + 1, a);

				if (val != null) {

					output = Gdx.files.absolute(path + "/output/" + decoder.name + "/" + decoder.name + ".json");

					output.writeString(val, false);

					name=decoder.name;
					decoder=new SpineCharacterDecoder();
					val=null;
				}
				index += 1;

			} else {
				if (!hasDone) {
					System.out.println("Done");
					hasDone = true;
				}
			}
		}
		if(nullType){
			if(Gdx.input.isKeyJustPressed(Input.Keys.O))
			{
				nullType=false;
			}

		}

	}

	@Override
	public void dispose() {
		batch.dispose();
		img.dispose();
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false);
	}
}
