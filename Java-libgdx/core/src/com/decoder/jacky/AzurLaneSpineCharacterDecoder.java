package com.decoder.jacky;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
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

	private Array<PerSpineKeeper> spineKeeperArray;

	private int index = 0;
	private String path;
	private boolean hasDone = false;
	private boolean jsonType = false;

	private int size = 0;
	private String[] array;

	private boolean nullType;
	private boolean realGo = false;
	private boolean isGiven = false;

	private float[][] colors = {
			{1, 1, 1, 1},
			{0, 1, 0, 1},
			{1, 0, 0, 1},
			{0, 0, 1, 1},
	};
	float scale=1.0f;

	public AzurLaneSpineCharacterDecoder(String[] args) {
		this.path = System.getProperty("user.dir");
		if (args.length > 0) {
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
			nullType = false;
		} else {
			nullType = true;
		}
	}

	@Override
	public void create() {
		batch = new SpriteBatch();
		img = new Texture("core/assets/WatchDog.png");
		camera = new OrthographicCamera();
		camera.setToOrtho(false);

		spineKeeperArray = new Array<PerSpineKeeper>();
		if (jsonType) {
			JsonReader reader = new JsonReader();
			JsonValue jsonValue = reader.parse(Gdx.files.absolute(args.get(0)));
			System.out.println();
			array = jsonValue.asStringArray();
			size = array.length;
		}
		path = System.getProperty("user.dir");

		scale=Float.parseFloat(Gdx.files.internal("core/assets/scale.txt").readString());
	}

	@Override
	public void render() {
		float[] showColor;
		if (nullType) showColor = colors[2];
		else if (hasDone) showColor = colors[3];
		else if (realGo) showColor = colors[1];
		else showColor = colors[0];
		//bg color work
		Gdx.gl.glClearColor(showColor[0], showColor[1], showColor[2], showColor[3]);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, Gdx.graphics.getWidth() / 2 - img.getWidth() / 2, Gdx.graphics.getHeight() / 2 - img.getHeight() / 2);
		batch.end();
		//main work
		if (!nullType && realGo) {
			if (index < size) {
				if (!isGiven) {
					if (jsonType) {
						spineKeeperArray = PerSpineKeeper.loadFromArray(array,scale);
					} else {
						spineKeeperArray = PerSpineKeeper.loadFromArray(args,scale);
					}
					isGiven = true;
					size=spineKeeperArray.size;

				}
				spineKeeperArray.get(index).setSavePath(Gdx.files.absolute(path));
				spineKeeperArray.get(index).TranslateWork();
				index += 1;
			} else {
				if (!hasDone) {
					System.out.println("Done");
					hasDone = true;
				}
			}
		}
		if (nullType) {
			//input files
			if (Gdx.input.isKeyPressed(Input.Keys.O) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
				try {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setMultiSelectionEnabled(true);

					fileChooser.setFileFilter(new FileFilter() {
						@Override
						public boolean accept(File f) {
							String fileName = f.getAbsolutePath().toLowerCase();
							return fileName.endsWith(".skel.txt") || fileName.endsWith(".skel") ||
									fileName.endsWith(".atlas.txt") || fileName.endsWith(".atlas") ||
									fileName.endsWith(".png") || f.isDirectory();
						}

						@Override
						public String getDescription() {
							return "Spine相关文件 (*.skel;*.skel.txt;*.atlas;*.atlas.txt;*.png)";
						}
					});

					int val = fileChooser.showOpenDialog(null);
					if (val == JFileChooser.APPROVE_OPTION) {
						args.clear();
						File[] files = fileChooser.getSelectedFiles();
						for (File file : files) {
							args.add(file.getAbsolutePath());
						}
						size = files.length;
						nullType = false;
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
			}
		}
		if (!realGo) {
			//save path
			if (Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
				try {
					JFileChooser dirChooser = new JFileChooser();
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

					int val = dirChooser.showSaveDialog(null);
					if (val == JFileChooser.APPROVE_OPTION) {
						this.path = dirChooser.getSelectedFile().getAbsolutePath();
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				}

			}
			if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
				realGo = true;
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
