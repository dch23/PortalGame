package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;

public class MyGdxGame extends ApplicationAdapter {
	// Window size is initialized at DesktopLauncher Class
	static final float STEP_TIME = 1f / 60f;
	static final int VELOCITY_ITERATIONS = 6;
	static final int POSITION_ITERATIONS = 2;
	static final float GAME_SCALE = 0.005f;

	private static final float SCENE_WIDTH = 1024f;
	private static final float SCENE_HEIGHT = 768f;

	private TextureAtlas textureAtlas;
	private float time = 0f;


	World world;

	Entity floor;
	Player player;

	ArrayList<Entity> boxes;
//	Entity box;
//	Entity box2;

	OrthographicCamera camera;
	Vector2 gravity = new Vector2(0,-4);
	boolean sleep = false;



//	ExtendViewport viewport;

	SpriteBatch batch;
	Texture img;

	Box2DDebugRenderer debugRenderer;

	@Override
	public void create () {
		Box2D.init();
		world = new World(gravity, sleep);
		floor = new Entity(world, new Vector2(1.5f,0.15f), new Vector2(3f,0.3f), BodyDef.BodyType.StaticBody, new Color(0,0,0,1), 0.1f, 0.1f, false);
//		box = new Entity(world, new Vector2(1.5f ,1.5f), new Vector2(0.2f,0.2f), BodyDef.BodyType.DynamicBody, new Color(1,0,0,1), 1f);
		boxes = new ArrayList<>();
		player = new Player(world, new Vector2(1.5f,3f), new Vector2(0.3f,0.3f), BodyDef.BodyType.DynamicBody, new Color(0,0,0,1), 0.1f, 0.1f, true);

		debugRenderer = new Box2DDebugRenderer();
		camera = new OrthographicCamera(scale(SCENE_WIDTH), scale(SCENE_HEIGHT));
		camera.translate(camera.viewportWidth/2f, camera.viewportHeight/2f);
		batch = new SpriteBatch();
		addBox(new Vector2(1.5f ,1.5f), new Vector2(0.2f, 0.2f));
		addBox(new Vector2(1.4f ,3f), new Vector2(0.4f, 0.4f));
//		img = new Texture("badlogic.jpg");
//		textureAtlas = new TextureAtlas();
	}

	private void addBox(Vector2 position, Vector2 size) {
		Entity newBox = new Entity(world, position, size, BodyDef.BodyType.DynamicBody, new Color(1,0,0,1), 0.1f, 0.1f, true);
		boxes.add(newBox);
	}

//	private void addSprites() {
//		Array<TextureAtlas.AtlasRegion> regions = textureAtlas.getRegions();
//
//		for (TextureAtlas.AtlasRegion region : regions) {
//			Sprite sprite = textureAtlas.createSprite(region.name);
//
//			float width = scale(sprite.getWidth());
//			float height = scale(sprite.getHeight());
//
//			sprite.setSize(width, height);
//			sprite.setOrigin(0, 0);
//
//			sprites.put(region.name, sprite);
//		}
//	}

	@Override
	public void render () {

		ScreenUtils.clear(1, 1, 1, 1);

		batch.setProjectionMatrix(camera.combined);

//		batch.begin();
//		batch.draw(img, 0, 0);
//		batch.end();

//		floor.applyForce(new Vector2(1,0), 10000f);
//		floor.render(camera);
//		box.render(camera);

		for (Entity box : boxes) {
			box.render(camera);

		}

		player.operate();
		debugRenderer.render(world, camera.combined);

		camera.update();
		stepWorld();
	}
	
	@Override
	public void dispose () {
//		batch.dispose();
//		img.dispose();
		Entity.dispose();
		world.dispose();
		for (Entity box : boxes) box.dispose();
	}

	private void stepWorld() {
		float delta = Gdx.graphics.getDeltaTime();
		world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
	}

	private float scale(float x) {
		return x*GAME_SCALE;
	}
	private Vector2 scale(Vector2 v) {
		return Vector2.Zero.mulAdd(v,GAME_SCALE);
	}



}
