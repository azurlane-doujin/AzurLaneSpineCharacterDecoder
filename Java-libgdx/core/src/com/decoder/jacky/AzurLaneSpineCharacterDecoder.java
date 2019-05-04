package com.decoder.jacky;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

	private String val="";
	private int size = 0;
	private String[] array;

	private String[] type = new String[]{"skeleton", "Texture", "Atlas"};
	private int indexType = 0;

	private boolean nullType
	;private 		boolean realGo=false;

	private float[][] colors={
		{1,1,1,1},
		{0,1,0,1},
		{1,0,0,1},
		{0,0,1,1},
	};
	private String name;

	public AzurLaneSpineCharacterDecoder(String[] args) {
		this.path = System.getProperty("user.dir");
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

			JsonValue jsonValue = reader.parse(Gdx.files.absolute(args.get(0)));

			System.out.println();
			array = jsonValue.asStringArray();
			size = array.length;

		}
		path="D:\\Users\\qz228\\Desktop";
	}

	@Override
	public void render() {
		float[] showColor;
		if (nullType) showColor=colors[2];
		else if (hasDone)showColor=colors[3];
		else if (realGo)showColor=colors[1];
		else showColor=colors[0];

		Gdx.gl.glClearColor(showColor[0],showColor[1],showColor[2],showColor[3]);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, Gdx.graphics.getWidth() / 2 - img.getWidth() / 2, Gdx.graphics.getHeight() / 2 - img.getHeight() / 2);
		batch.end();

		if (!nullType&&realGo){
			if (index < size) {
				FileHandle output;
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
				if (val!=null&&val.startsWith("{\"error\"")){
					System.out.printf("error to load %s\t%d\n",name,index+1);
					val="";
				}
				System.out.printf("finish:\t%s\ttype:%s\n\tN0.%d,\n\t%.2f%%\n", name, type[indexType], index + 1, a);

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
			if(Gdx.input.isKeyPressed(Input.Keys.O)&&Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
			{
				try{
					JFileChooser fileChooser =new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setMultiSelectionEnabled(true);

					fileChooser.setFileFilter(new FileFilter() {
						@Override
						public boolean accept(File f) {
							String fileName=f.getAbsolutePath();
							return fileName.endsWith(".skel.txt") || fileName.endsWith(".skel")||f.isDirectory();
						}

						@Override
						public String getDescription() {
							return "Spine骨架文件 (*.skel;*.skel.txt)";
						}
					});

					int val=fileChooser.showOpenDialog(null);
					if(val==JFileChooser.APPROVE_OPTION)
					{
						args.clear();
						File[] files=fileChooser.getSelectedFiles();
						for (File file : files) {
							args.add(file.getAbsolutePath());
						}
						size=files.length;
						nullType=false;
					}
				}catch (Throwable ex){
					ex.printStackTrace();
				}

			}

		}
		if (!realGo){
			if(Gdx.input.isKeyPressed(Input.Keys.S)&&Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
			{
				try{
					JFileChooser dirChooser =new JFileChooser();
					dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					dirChooser.setFileFilter(new FileFilter() {
						@Override
						public boolean accept(File f) {
							return f.isDirectory();
						}

						@Override
						public String getDescription() {
							return "导出文件夹";
						}
					});

					int val=dirChooser.showSaveDialog(null);
					if(val==JFileChooser.APPROVE_OPTION) {
						this.path = dirChooser.getSelectedFile().getAbsolutePath();
					}
				}catch (Throwable ex){
					ex.printStackTrace();
				}

			}
			if(Gdx.input.isKeyPressed(Input.Keys.SPACE))
				realGo=true;
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
