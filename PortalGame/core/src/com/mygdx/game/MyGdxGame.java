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

	protected static float SCENE_WIDTH;
	protected static float SCENE_HEIGHT;
	public static int currentLevel = 2;
	public static boolean updateLevel = false;

	static ArrayList<GameMap> maps = new ArrayList<>();
	static GameMap currentMap;

	// Physics World
	private World world;
	Vector2 gravity = new Vector2(0f,-6);

	// Camera
	OrthographicCamera camera;


	// Objects in the physics world
	Player player;

	// Rendered variables for the entities
	static Renderer entityRenderer;
	Texture squareTexture;
	Sprite squareSprite;

	// Rendering Debug Objects
	Box2DDebugRenderer b2dr;

	// lasers are not used rn
	ArrayList<Laser> lasers;
	float angle = 0f;

	public MyGdxGame(float screenWidth, float screenHeight) {
		SCENE_WIDTH = screenWidth;
		SCENE_HEIGHT = screenHeight;
	}

	public static void changeLevel(int level) {
		currentMap.unload();
		currentMap = maps.get(level);
		currentMap.load();
		currentLevel = level;
	}

	@Override
	public void create () {
		// Initialize Physics World
		world = new World(gravity, false);
		world.setContactListener(MyGdxGame.COLLISION_LISTENER);

		// initialize Entity world
		Entity.setWorld(world);

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

		// levels
		maps.add(new GameMap(world,"DarkMap1/tiledAssets/Level1(Tutorial).tmx", this.camera, entityRenderer));
		maps.add(new GameMap(world,"DarkMap1/tiledAssets/Level2(EasyPuzzle).tmx", this.camera, entityRenderer));
		maps.add(new GameMap(world,"DarkMap1/tiledAssets/Level3(IntroToEnemies).tmx", this.camera, entityRenderer));
		maps.add(new GameMap(world,"DarkMap1/tiledAssets/Level5(BeforeBoss).tmx", this.camera, entityRenderer));
		maps.add(new GameMap(world,"DarkMap1/tiledAssets/Level6(MidBoss).tmx", this.camera, entityRenderer));
		maps.add(new GameMap(world,"DarkMap1/tiledAssets/Level7(IntroToLazers).tmx", this.camera, entityRenderer));
		maps.add(new GameMap(world,"DarkMap1/tiledAssets/Level8(ElevatorShafts).tmx", this.camera, entityRenderer));

		currentMap = maps.get(currentLevel);
		currentMap.load();
//		map.unload();


		Laser.setProjectionMatrix(camera.combined);
		lasers = new ArrayList<>();
//		lasers.add(new Laser(world, new Vector2(2.2f,2.5f), new Color(1,0,0,1), 180f, 0.02f, 10));
//		lasers.add(new Laser(world, new Vector2(2.2f,3.5f), new Color(1,0,0,1), 0f, 0.02f, 10));
//		lasers.add(new Laser(world, new Vector2(4.2f,1f), new Color(1,0,0,1), 0f, 0.02f, 10));
//		lasers.add(new Laser(world, new Vector2(5f,2.5f), new Color(1,0,0,1), 0f, 0.02f, 10));

		// portal trails
		PortalTrails.setProjectionMatrix(camera.combined);

	}

	@Override
	public void render () {
		// Set Screen Background Colour to White with an Alpha of 100%
		ScreenUtils.clear(0, 0, 0, 1);

		// Set the Sprite Batch Renderer Set to The Camera Matrix
		entityRenderer.getBatch().setProjectionMatrix(camera.combined);

		Player.operate();

		WeakEnemyEntity.operate();
		MidEnemyEntity.operate();

		currentMap.renderBackground();


		Laser.beginRender();
		angle+=1f;
		for (Laser laser : lasers) {
			laser.setAngle(angle);
			laser.render();
		}
		Laser.endRender();

		PortalTrails.draw();

		entityRenderer.beginRender();
		entityRenderer.render();
		entityRenderer.endRender();

		currentMap.renderForeground();

		Player.renderPortals();

		// Render Debug Lines for Physics Object in Physics World
		b2dr.render(world, camera.combined);


		// Update the Camera
		camera.update();


		if (updateLevel) {
			changeLevel(currentLevel);
			updateLevel = false;
		}

		// Next Physics frame
		stepWorld();
	}
	
	@Override
	public void dispose () {
		// MUST LOOK OVER THIS WELL OR ELSE MEMORY LEAKS WILL OCCUR, THROW AWAY EVERYTHING UNNEEDED AFTER GAME IS ENDED
		Entity.disposeAll();
		world.dispose();
		for (GameMap map : maps) {
			map.dispose();
		}
		maps = null;
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
