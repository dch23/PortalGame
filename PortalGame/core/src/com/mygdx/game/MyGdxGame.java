package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
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
	static float gameElapsedTime = 0f;
	static final float STEP_TIME = 1f / 60f;
	static final int VELOCITY_ITERATIONS = 6;
	static final int POSITION_ITERATIONS = 2;
	static final float GAME_SCALE = 1.0f/4.0f/4.0f/4.0f/4.0f;
	static final CollisionListener COLLISION_LISTENER = new CollisionListener();

	protected static float SCENE_WIDTH;
	protected static float SCENE_HEIGHT;
	protected static Vector2 gameBounds;
	public static int currentLevel = 5;
	public static boolean updateLevel = false;

	static ArrayList<GameMap> maps = new ArrayList<>();
	static GameMap currentMap;

	// music


	// Physics World
	private World world;
	Vector2 gravity = new Vector2(0f,-6);

	// Camera
	OrthographicCamera camera;

	// Rendered variables for the entities
	static Renderer entityRenderer;
	Texture squareTexture;
	Sprite squareSprite;
	String[] renderAboveForeground = new String[] {"Boss", "fireball", "fireTrail"};

	// Rendering Debug Objects
	Box2DDebugRenderer b2dr;

	// lasers are not used rn
	ArrayList<Laser> lasers;
	float angle = 0f;

	public MyGdxGame(float screenWidth, float screenHeight) {
		SCENE_WIDTH = screenWidth;
		SCENE_HEIGHT = screenHeight;
		gameBounds = PMath.multVector2(new Vector2(MyGdxGame.SCENE_WIDTH, MyGdxGame.SCENE_HEIGHT), MyGdxGame.GAME_SCALE);
	}

	public static void changeLevel(int level) {
		currentMap.unload();
		currentMap = maps.get(level);
		currentMap.load();
		currentLevel = level;
	}

	@Override
	public void create () {

		// start music
		Sound music = Gdx.audio.newSound(Gdx.files.internal("music/craz3.mp3"));
		AudioManager.playSound(music, 0.4f, true, false);


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
//		maps.add(new GameMap(world,"DarkMap1/tiledAssets/Level6(MidBoss).tmx", this.camera, entityRenderer));
		maps.add(new GameMap(world,	"DarkMap1/tiledAssets/Level7(IntroToLazers).tmx", this.camera, entityRenderer));
		maps.add(new GameMap(world,"DarkMap1/tiledAssets/Level8(ElevatorShafts).tmx", this.camera, entityRenderer));
		maps.add(new GameMap(world,"DarkMap1/tiledAssets/Level9(FinalBoss).tmx", this.camera, entityRenderer));


		currentMap = maps.get(currentLevel);
		currentMap.load();
//		map.unload();

		// Lasers set up
		Laser.setProjectionMatrix(camera.combined);

		// portal trails
		PortalTrails.setProjectionMatrix(camera.combined);

	}

	@Override
	public void render () {
		// Set Screen Background Colour to White with an Alpha of 100%
		ScreenUtils.clear(0, 0, 0, 1);

		// Set the Sprite Batch Renderer Set to The Camera Matrix

		entityRenderer.getBatch().setProjectionMatrix(camera.combined);

		currentMap.renderBackground();


		// operate
		Entity.operation();
		Player.operate();
		WeakEnemyEntity.operate();
		MidEnemyEntity.operate();
		ChargeEnemyEntity.operate();
		Laser.operate();
		Boss.operate();
		Fireball.operate();
		FireTrail.operate();

		// draw entities
		entityRenderer.renderBlackList(renderAboveForeground);

		currentMap.renderForeground();

		// render extra
		entityRenderer.renderWhiteList(renderAboveForeground);

		// draw the portals
		Player.renderPortals();
		PortalTrails.draw();

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

		// elapse time
		gameElapsedTime += Gdx.graphics.getDeltaTime();

		// collect
		System.gc();

	}
	
	@Override
	public void dispose () {
		// MUST LOOK OVER THIS WELL OR ELSE MEMORY LEAKS WILL OCCUR, THROW AWAY EVERYTHING UNNEEDED AFTER GAME IS ENDED
		Fireball.disposeAll();
		FireTrail.disposeAll();
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

}
