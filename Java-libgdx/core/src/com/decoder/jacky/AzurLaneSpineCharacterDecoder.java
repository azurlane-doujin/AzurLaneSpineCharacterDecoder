package com.decoder.jacky;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AzurLaneSpineCharacterDecoder extends ApplicationAdapter {
	private SpriteBatch batch;
    private Texture img;
	private List<String> args =new ArrayList<String>();
	private OrthographicCamera camera;
	private SpineCharactDecoder decoder;
	private int index=0;
	private String path;

	public AzurLaneSpineCharacterDecoder(String[] args){
        for (String arg : args) {
            if (arg.endsWith(".skel.txt") || arg.endsWith(".skel"))
                this.args.add(arg);
        }
        this.path=System.getProperty("user.dir");
    }

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("WatchDog.png");
		camera=new OrthographicCamera();
        camera.setToOrtho(false);

        decoder=new SpineCharactDecoder();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, Gdx.graphics.getWidth()/2-img.getWidth()/2, Gdx.graphics.getHeight()/2-img.getHeight()/2);
		batch.end();

		if(index>=args.size())
		    System.exit(0);

		String val =decoder.decoder(Gdx.files.absolute(args.get(index)));
		System.out.println(val);
		System.out.println();

        FileHandle output=Gdx.files.absolute(path+"/"+decoder.name+"/"+decoder.name+".json");

        output.writeString(val,false);

		index+=1;
		decoder.clear();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false);
    }
}
