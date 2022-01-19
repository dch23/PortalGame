package com.mygdx.game;

import com.badlogic.gdx.*;
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
	static final float GAME_SCALE = 1.0f/4.0f/4.0f/4.0f/4.0f;
	static final CollisionListener COLLISION_LISTENER = new CollisionListener();

	protected static final float SCENE_WIDTH = 1920f;
	protected static final float SCENE_HEIGHT = 1080f;

	// Maps
	GameMap map;

	// Physics World
	private World world;
	Vector2 gravity = new Vector2(0f,-6);

	// Camera
	OrthographicCamera camera;

	// Rendered variables for the entities
	static Renderer entityRenderer;
	Texture squareTexture;
	Sprite squareSprite;

	// Rendering Debug Objects
	Box2DDebugRenderer b2dr;

	// lasers are not used rn
	ArrayList<Laser> lasers;
	float angle = 0f;

	@Override
	public void create () {

		// Initialize Debug Renderer for making debug lines and debug shapes for the physics objects
		b2dr = new Box2DDebugRenderer();

		// Sprite Render Initialization
		squareTexture = new Texture("shapes/square.jpeg");
		squareSprite = new Sprite(squareTexture);

		// Initialize Sprite Renderer Variables
		entityRenderer = new Renderer(new SpriteBatch());

		// Initialize Camera
		camera = new OrthographicCamera(scale(SCENE_WIDTH), scale(SCENE_HEIGHT));
		camera.translate(camera.viewportWidth/2f, camera.viewportHeight/2f);
		camera.update();

		// Initialize Physics World
		world = new World(gravity, false);
		world.setContactListener(MyGdxGame.COLLISION_LISTENER);

		//Maps
		map = new GameMap(world,"DarkMap1/tiledAssets/Level3(IntroToEnemies).tmx", this.camera, entityRenderer);
		map.load();

		Laser.setProjectionMatrix(camera.combined);
		lasers = new ArrayList<>();
//		lasers.add(new Laser(world, new Vector2(2.2f,2.5f), new Color(1,0,0,1), 180f, 0.02f, 10));
//		lasers.add(new Laser(world, new Vector2(2.2f,3.5f), new Color(1,0,0,1), 0f, 0.02f, 10));
//		lasers.add(new Laser(world, new Vector2(4.2f,1f), new Color(1,0,0,1), 0f, 0.02f, 10));
//		lasers.add(new Laser(world, new Vector2(5f,2.5f), new Color(1,0,0,1), 0f, 0.02f, 10));

	}

	@Override
	public void render () {
		// Set Screen Background Colour to White with an Alpha of 100%
		ScreenUtils.clear(1, 1, 1, 1);

		// Set the Sprite Batch Renderer Set to The Camera Matrix
		entityRenderer.getBatch().setProjectionMatrix(camera.combined);

		Player.operate();

		WeakEnemyEntity.operate();

		map.renderBackground();

		Laser.beginRender();
		angle+=1f;
		for (Laser laser : lasers) {
			laser.setAngle(angle);
			laser.render();
		}
		Laser.endRender();


		entityRenderer.beginRender();
		entityRenderer.render();
		entityRenderer.endRender();

		map.renderForeground();

		Player.renderPortals();

		// Render Debug Lines for Physics Object in Physics World
		b2dr.render(world, camera.combined);


		// Update the Camera
		camera.update();

		// Next Physics frame
		stepWorld();
	}
	
	@Override
	public void dispose () {
		// MUST LOOK OVER THIS WELL OR ELSE MEMORY LEAKS WILL OCCUR, THROW AWAY EVERYTHING UNNEEDED AFTER GAME IS ENDED
		Entity.disposeAll();
		world.dispose();
		map.dispose();
	}

	private void stepWorld() {
		world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
	}

	// This Scale Methods ARE NECESSARY because of libGDXs poor physics scale. Makes everything small with a small camera to enable uses of small force magnitudes
	static public float scale(float x) {
		return x*GAME_SCALE;
	}
	static public Vector2 scale(Vector2 v) {
		return Vector2.Zero.mulAdd(v,GAME_SCALE);
	}

}
