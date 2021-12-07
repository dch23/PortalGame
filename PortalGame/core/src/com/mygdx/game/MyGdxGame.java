package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class MyGdxGame extends ApplicationAdapter {
	// Window size is initialized at DesktopLauncher Class
	static final float STEP_TIME = 1f / 60f;
	static final int VELOCITY_ITERATIONS = 6;
	static final int POSITION_ITERATIONS = 2;
	static final float GAME_SCALE = 0.01f;

	private static final float SCENE_WIDTH = 1024f * GAME_SCALE;
	private static final float SCENE_HEIGHT = 768f * GAME_SCALE;




	World world;

	Entity floor;

	OrthographicCamera camera;
	Vector2 gravity = new Vector2(0,-120);
	boolean sleep = false;



//	ExtendViewport viewport;

	SpriteBatch batch;
	Texture img;

	Box2DDebugRenderer debugRenderer;

	@Override
	public void create () {
		Box2D.init();
		world = new World(gravity, sleep);
		floor = new Entity(world, new Vector2(0,0), new Vector2(10f,10f), new Color(0,0,0,1), 1f);
		debugRenderer = new Box2DDebugRenderer();
		camera = new OrthographicCamera(SCENE_WIDTH, SCENE_HEIGHT);
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
	}

	@Override
	public void render () {

		ScreenUtils.clear(1, 1, 1, 1);

		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();

//		floor.applyForce(new Vector2(1,0), 10000f);
//		floor.render(camera);

//		debugRenderer.render(world, camera.combined);

		camera.update();
		stepWorld();
	}
	
	@Override
	public void dispose () {
//		batch.dispose();
//		img.dispose();
		Entity.dispose();
		world.dispose();
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
