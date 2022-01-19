package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.graalvm.compiler.phases.common.inlining.info.elem.InlineableGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class GameMap {
    static protected TiledMapRenderer tiledMapRenderer;
    static protected TmxMapLoader tmxMapLoader = new TmxMapLoader();

    protected World world;
    protected TiledMap tiledMap;
    protected OrthographicCamera camera;

    protected float renderScale;
    protected Vector2 exitDoorPosition;

    protected int[] backgroundIndexes;
    protected int[] foregroundIndexes;

    public void load() {
        if (tiledMap == null) return;

        // collision entities
        MapLayers layers = this.tiledMap.getLayers();
        MapLayer collisionLayer = layers.get("Collision");
        MapObjects collisionObjects = collisionLayer.getObjects();

        for (int i = 0; i < collisionObjects.getCount(); i++) {
            MapObject object = collisionObjects.get(i);

            Vector2 position = new Vector2((float)object.getProperties().get("x"), (float)object.getProperties().get("y"));
            Vector2 size = new Vector2((float)object.getProperties().get("width"), (float)object.getProperties().get("height"));
            Object angleObject = object.getProperties().get("rotation");
            Float angle = angleObject == null ? null : -(float) angleObject;

            // scale
            position = PMath.multVector2(position, this.renderScale);
            size = PMath.multVector2(size, this.renderScale);

            // translate
            position.add(PMath.multVector2(size, 0.5f));

            float density = 1;
            float friction = 0.1f;

            Entity newEntity = new Entity(world, "map object", position, size, BodyDef.BodyType.StaticBody,
                    null, density, friction, false, null);
            if (angle != null) {
                newEntity.setAngle(angle, false);
            }
        }

        // Enemies
        MapLayer enemiesLayer = layers.get("Enemies");
        MapObjects enemiesObjects = enemiesLayer.getObjects();
        for (int i = 0; i < enemiesObjects.getCount(); i++) {
            MapObject enemyObject = enemiesObjects.get(i);
            addEnemy(enemyObject);
        }

        // spawn player
        MapLayer doorLayer = layers.get("Doors");
        MapObjects doorObjects = doorLayer.getObjects();
        MapObject enterDoor = null, exitDoor = null;
        for (int i=0; i<doorObjects.getCount(); i++) {
            MapObject object = doorObjects.get(i);
            String objectName = (String) object.getProperties().get("name");
            if (objectName.equals("enter")) enterDoor = object;
            else if (objectName.equals("exit")) exitDoor = object;
        }
        Vector2 doorPosition = new Vector2((float) enterDoor.getProperties().get("x"),
                (float) enterDoor.getProperties().get("y"));
        Vector2 doorSize = new Vector2((float) enterDoor.getProperties().get("width"), (float) enterDoor.getProperties().get("height"));
        doorPosition = PMath.addVector2(doorPosition, new Vector2(doorSize.x/2f, doorSize.y/2f));
        doorPosition = PMath.multVector2(doorPosition, this.renderScale);
        spawnPlayer(doorPosition);

        // assign exit door
        Vector2 exitDoorPosition = new Vector2((float) exitDoor.getProperties().get("x"),
                (float) exitDoor.getProperties().get("y"));
        exitDoorPosition = PMath.multVector2(exitDoorPosition, this.renderScale);

        // foreground and background indexes for rendering order
        ArrayList<Integer> backgroundIndexesList = new ArrayList<>();
        ArrayList<Integer> foregroundIndexesList = new ArrayList<>();
        backgroundIndexesList.add(layers.getIndex("Background"));
        foregroundIndexesList.add(layers.getIndex("Foreground"));

        backgroundIndexes = new int[backgroundIndexesList.size()];
        foregroundIndexes = new int[foregroundIndexesList.size()];

        for (int i = 0; i < backgroundIndexes.length; ++i) backgroundIndexes[i] = backgroundIndexesList.get(i);
        for (int i = 0; i < foregroundIndexes.length; ++i) foregroundIndexes[i] = foregroundIndexesList.get(i);
    }

    public GameMap(World world, String tiledMapDirectory, OrthographicCamera camera, Renderer entityRenderer) {
        this.world = world;
        this.camera = camera;

        // Load map
        this.tiledMap = tmxMapLoader.load(tiledMapDirectory);
        MapProperties mapProperties = tiledMap.getProperties();

        // Create scale
        this.renderScale = Math.min(MyGdxGame.SCENE_WIDTH / (mapProperties.get("width", Integer.class) * mapProperties.get("tilewidth", Integer.class)) * MyGdxGame.GAME_SCALE,
                MyGdxGame.SCENE_HEIGHT / (mapProperties.get("height", Integer.class) * mapProperties.get("tileheight", Integer.class)) * MyGdxGame.GAME_SCALE
        );

        // Create Renderer
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, this.renderScale);
        tiledMapRenderer.setView(this.camera);

        // Create Bodies
//        Iterator<TiledMapTileSet> tiledSetsIterator = this.tiledMap.getTileSets().iterator();
//        for (TiledMapTileSet tiledSets = tiledSetsIterator.next(); tiledSetsIterator.hasNext(); tiledSets = tiledSetsIterator.next()) {
//            Iterator<TiledMapTile> tiledMapTileIterator = tiledSets.iterator();
//            for (TiledMapTile tile = tiledMapTileIterator.next(); tiledMapTileIterator.hasNext(); tile = tiledMapTileIterator.next()) {
//                Iterator<> tile.getProperties().getKeys();
//            }
//        }


    }

    private void spawnPlayer(Vector2 enterDoorPosition) {
        Vector2 playerPosition = enterDoorPosition;
        RayHitInfo ray = PMath.getClosestRayHitInfo(world, playerPosition, new Vector2(0, -1), 10, false);
        if (ray != null) {
            playerPosition = PMath.addVector2(ray.point, new Vector2(0, Player.regularSize.y));
        }
        Player player = new Player(world, camera, "Player", playerPosition, Player.regularSize,
                BodyDef.BodyType.DynamicBody, new Color(1,0,0,1),
                10f, 0.0f, true, null);
    }

    public void renderBackground () {
//        tiledMapRenderer.setView(this.camera);
        if (backgroundIndexes == null) return;
        tiledMapRenderer.render(backgroundIndexes);
    }

    public void renderForeground () {
        tiledMapRenderer.render(this.foregroundIndexes);
    }

    public void dispose() {
        this.tiledMap.dispose();
    }

    private void addEnemy(MapObject object) {
        String enemyName = (String) object.getProperties().get("name");
        Vector2 position = new Vector2((float) object.getProperties().get("x"),
                (float) object.getProperties().get("y"));
        position = PMath.multVector2(position, renderScale);
//        System.out.println(position);
        RayHitInfo ray = PMath.getClosestRayHitInfo(world, position, new Vector2(0,-1), 10, false);
        if (ray != null) {
            position = ray.point;
        }

        Vector2 regularSize = new Vector2(0.5f,0.5f);
        switch (enemyName) {
            case "weakEnemy":
                regularSize = WeakEnemyEntity.getSize();
                position = PMath.addVector2(position, new Vector2(0, regularSize.y/2f));
                new WeakEnemyEntity(world, enemyName, position, regularSize, BodyDef.BodyType.DynamicBody, null, 0.1f, 0.1f, true, null);
                break;
        }
    }

}
