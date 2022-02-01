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
    protected TiledMapRenderer tiledMapRenderer;
    protected TmxMapLoader tmxMapLoader = new TmxMapLoader();

    protected World world;
    protected TiledMap tiledMap;
    protected OrthographicCamera camera;

    protected float renderScale;

    protected int[] backgroundIndexes;
    protected int[] foregroundIndexes;

    protected boolean loaded = false;

    public GameMap(World world, String tiledMapDirectory, OrthographicCamera camera, Renderer entityRenderer) {
        this.world = world;
        this.camera = camera;

        // Load map
        this.tiledMap = tmxMapLoader.load(tiledMapDirectory);
        MapProperties mapProperties = tiledMap.getProperties();

        // Create scale
        int mapWidth = (mapProperties.get("width", Integer.class) * mapProperties.get("tilewidth", Integer.class));
        int mapHeight = (mapProperties.get("height", Integer.class) * mapProperties.get("tileheight", Integer.class));
        this.renderScale = Math.min(MyGdxGame.SCENE_WIDTH / mapWidth * MyGdxGame.GAME_SCALE,
                MyGdxGame.SCENE_HEIGHT / mapHeight * MyGdxGame.GAME_SCALE
        );

        // Create Renderer
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, this.renderScale);
        tiledMapRenderer.setView(this.camera);

        // move camera to center the map
        camCenterMap(mapWidth, mapHeight);

        // Create Bodies
//        Iterator<TiledMapTileSet> tiledSetsIterator = this.tiledMap.getTileSets().iterator();
//        for (TiledMapTileSet tiledSets = tiledSetsIterator.next(); tiledSetsIterator.hasNext(); tiledSets = tiledSetsIterator.next()) {
//            Iterator<TiledMapTile> tiledMapTileIterator = tiledSets.iterator();
//            for (TiledMapTile tile = tiledMapTileIterator.next(); tiledMapTileIterator.hasNext(); tile = tiledMapTileIterator.next()) {
//                Iterator<> tile.getProperties().getKeys();
//            }
//        }


    }

    private void camCenterMap(float mapWidth, float mapHeight) {
//        float widthScale = MyGdxGame.SCENE_WIDTH / mapWidth;
//        float heightScale = MyGdxGame.SCENE_HEIGHT / mapHeight;
//        float scale = Math.min(widthScale, heightScale);
//        mapWidth *= scale;
//        mapHeight *= scale;
//        System.out.println(mapWidth + " " + mapHeight + " vs " + MyGdxGame.SCENE_WIDTH + " " + MyGdxGame.SCENE_HEIGHT);
//        if (mapWidth != MyGdxGame.SCENE_WIDTH) {
////            float extraSpace = Math.abs(mapWidth - MyGdxGame.SCENE_WIDTH);
////            camera.translate(new Vector2(extraSpace/2f,0));
//        }
//        else if (mapHeight != MyGdxGame.SCENE_HEIGHT) {
//            float extraSpace = Math.abs(mapHeight - MyGdxGame.SCENE_HEIGHT);
//            extraSpace *= MyGdxGame.GAME_SCALE;
//            camera.translate(new Vector2(0,-extraSpace/2f));
//        }
////        camera.translate(camera.viewportWidth/2f, camera.viewportHeight/2f);
    }



    private void spawnPlayer(Vector2 enterDoorPosition) {
        Vector2 playerPosition = enterDoorPosition;
        RayHitInfo ray = PMath.getClosestRayHitInfo(world, playerPosition, new Vector2(0, -1), 10, false);
        if (ray != null) {
            playerPosition = PMath.addVector2(ray.point, new Vector2(0, Player.regularSize.y));
        }
        Player player = new Player(camera, "Player", playerPosition, Player.regularSize,
                BodyDef.BodyType.DynamicBody, new Color(1,0,0,1),
                10f, 0.0f, true, null);
    }

    public void renderBackground () {
        if (!loaded) return;
//        tiledMapRenderer.setView(this.camera);
        if (backgroundIndexes == null) return;
//        for (int i=0; i<backgroundIndexes.length; i++) {
//            System.out.print(backgroundIndexes[i] + " ");
//        }
//        System.out.println();
        tiledMapRenderer.render(backgroundIndexes);
    }

    public void renderForeground () {
        if (!loaded) return;
        tiledMapRenderer.render(this.foregroundIndexes);
    }

    public void dispose() {
        this.tiledMap.dispose();
    }

    private void addEnemy(MapObject object) {
        // get name
        String enemyName = (String) object.getProperties().get("name");
        if (enemyName == null) return;

        // get position and scale it
        Vector2 position = new Vector2((float) object.getProperties().get("x"), (float) object.getProperties().get("y"));
        position = PMath.multVector2(position, renderScale);

        // shoot ray down if this enemy isn't a laser
        if (!enemyName.equals("laser") && !enemyName.equals("Boss")) {
            RayHitInfo ray = PMath.getClosestRayHitInfo(world, position, new Vector2(0, -1), 10, false);
            if (ray != null) position = ray.point;
        }

        Vector2 regularSize = new Vector2(0.5f,0.5f);
        switch (enemyName) {
            case "weakEnemy":
                regularSize = WeakEnemyEntity.getRegularSize();
                position = PMath.addVector2(position, new Vector2(0, regularSize.y/2f));
                new WeakEnemyEntity(enemyName, position, regularSize, BodyDef.BodyType.DynamicBody, null, 10f, 0.1f, true, null);
                break;
            case "midEnemy":
                regularSize = MidEnemyEntity.getRegularSize();
                position = PMath.addVector2(position, new Vector2(0, regularSize.y/2f));
                new MidEnemyEntity(enemyName, position, regularSize, BodyDef.BodyType.DynamicBody, null, 10f, 0.1f, true, null);
                break;
            case "chargeEnemy":
                regularSize = ChargeEnemyEntity.getRegularSize();
                position = PMath.addVector2(position, new Vector2(0, regularSize.y/2f));
                new ChargeEnemyEntity(enemyName, position, regularSize, BodyDef.BodyType.DynamicBody, null, 10f, 0.1f, true, null);
                break;
            case "laser":
                float angle = (float) object.getProperties().get("angle");
                new Laser(world, position, Color.RED, angle, 0.03f, 10f);
                break;
            case "Boss":
                regularSize = Boss.getRegularSize();
                position = PMath.addVector2(position, new Vector2(regularSize.x/2f, regularSize.y/2f));
                new Boss(enemyName, position, regularSize, BodyDef.BodyType.DynamicBody, null, 10f, 0.1f, false, null);
                break;
        }
    }

    public void load() {
        if (tiledMap == null) return;
        if (loaded) return;

        // collision entities
        MapLayers layers = this.tiledMap.getLayers();
        MapLayer collisionLayer = layers.get("Collision");
        MapObjects collisionObjects = collisionLayer.getObjects();

        for (int i = 0; i < collisionObjects.getCount(); i++) {
            MapObject object = collisionObjects.get(i);

            Object nameObject = object.getProperties().get("name");
            Vector2 position = new Vector2((float)object.getProperties().get("x"), (float)object.getProperties().get("y"));
            Vector2 size = new Vector2((float)object.getProperties().get("width"), (float)object.getProperties().get("height"));
            Object angleObject = object.getProperties().get("rotation");
            Float angle = angleObject == null ? null : -(float) angleObject;
//            Iterator<String> props = object.getProperties().getKeys();
//            for (String s = props.next(); props.hasNext(); s = props.next()) {
//                if (s == "rotation") {
//                    System.out.println("rot: " + object.getProperties().get(s));
//                }
//            }

            // scale
            position = PMath.multVector2(position, this.renderScale);
            size = PMath.multVector2(size, this.renderScale);

            // translate
            position.add(PMath.multVector2(size, 0.5f));

            // configure
            float density = 1;
            float friction = 0.1f;

            // create
            Entity newEntity = new Entity("map object", position, size, BodyDef.BodyType.StaticBody,
                    null, density, friction, false, null);

            // set angle
            if (angle != null) newEntity.setAngle(angle, false);

            // can portal on this entity?
            Object canPortalOnProperty = object.getProperties().get("canPortalOn");
            boolean canPortalOn = true;
            if (canPortalOnProperty != null) canPortalOn = (boolean) canPortalOnProperty;
            newEntity.canPortalOn = canPortalOn;

            // is a sharp object
            if (nameObject != null) {
                String name = (String) nameObject;
                if (name.equals("die")) {
                    newEntity.setName(name);
//                    newEntity.getBody().getFixtureList().first().setSensor(true);
                }
            }
        }

        // Enemies
        MapLayer enemiesLayer = layers.get("Enemies");
        MapObjects enemiesObjects = enemiesLayer.getObjects();
        for (int i = 0; i < enemiesObjects.getCount(); i++) {
            MapObject enemyObject = enemiesObjects.get(i);
            addEnemy(enemyObject);
        }

        // block enemies
        MapLayer blockEnemyLayer = layers.get("BlockEnemy");
        if (blockEnemyLayer != null) {
            MapObjects objects = blockEnemyLayer.getObjects();
            for (int i = 0; i < objects.getCount(); i++) {
                MapObject ob = objects.get(i);

                // fetch data
                Vector2 position = new Vector2((float)ob.getProperties().get("x"), (float)ob.getProperties().get("y"));
                Vector2 size = new Vector2((float)ob.getProperties().get("width"), (float)ob.getProperties().get("height"));
                Object angleObject = ob.getProperties().get("rotation");
                Float angle = angleObject == null ? null : -(float) angleObject;

                // scale
                position = PMath.multVector2(position, this.renderScale);
                size = PMath.multVector2(size, this.renderScale);

                // translate
                position.add(PMath.multVector2(size, 0.5f));

                // density and such
                float density = 1;
                float friction = 0.1f;

                // create enemy
                Entity newEntity = new Entity("block enemy", position, size, BodyDef.BodyType.StaticBody,
                        null, density, friction, false, null);

                // angle
                if (angle != null) newEntity.setAngle(angle, false);

                // set sensor
                newEntity.getBody().getFixtureList().first().setSensor(true);
            }
        }

        // spawn player
        MapLayer doorLayer = layers.get("Doors");
        MapObjects doorObjects = doorLayer.getObjects();
        MapObject enterDoor = null, exitDoor = null;
        for (int i=0; i<doorObjects.getCount(); i++) {
            MapObject object = doorObjects.get(i);
            String objectName = (String) object.getProperties().get("name");
            if (objectName == null) continue;
            if (objectName.equals("enter")) enterDoor = object;
            else if (objectName.equals("exit")) exitDoor = object;
        }
        if (enterDoor != null) {
            Vector2 enterDoorPosition = new Vector2((float) enterDoor.getProperties().get("x"),
                    (float) enterDoor.getProperties().get("y"));
            Vector2 enterDoorSize = new Vector2((float) enterDoor.getProperties().get("width"), (float) enterDoor.getProperties().get("height"));
            enterDoorPosition = PMath.addVector2(enterDoorPosition, new Vector2(enterDoorSize.x / 2f, enterDoorSize.y / 2f));
            enterDoorPosition = PMath.multVector2(enterDoorPosition, this.renderScale);
            spawnPlayer(enterDoorPosition);
        }

        // assign exit door
        if (exitDoor != null) {
            Vector2 exitDoorPosition = new Vector2((float) exitDoor.getProperties().get("x"),
                    (float) exitDoor.getProperties().get("y"));
            Vector2 exitDoorSize = new Vector2((float) exitDoor.getProperties().get("width"), (float) exitDoor.getProperties().get("height"));
            exitDoorPosition = PMath.addVector2(exitDoorPosition, new Vector2(exitDoorSize.x / 2f, exitDoorSize.y / 2f));
            exitDoorPosition = PMath.multVector2(exitDoorPosition, this.renderScale);
            exitDoorSize = PMath.multVector2(exitDoorSize, this.renderScale);

            Entity exitDoorEntity = new Entity("exit door", exitDoorPosition, exitDoorSize, BodyDef.BodyType.StaticBody,
                    null, 0.1f, 0.1f, false, null);
            exitDoorEntity.getBody().getFixtureList().first().setSensor(true);
        }

        // foreground and background indexes for rendering order
        ArrayList<Integer> backgroundIndexesList = new ArrayList<>();
        ArrayList<Integer> foregroundIndexesList = new ArrayList<>();
        backgroundIndexesList.add(layers.getIndex("Background"));
        foregroundIndexesList.add(layers.getIndex("Foreground"));

        backgroundIndexes = new int[backgroundIndexesList.size()];
        foregroundIndexes = new int[foregroundIndexesList.size()];

        for (int i = 0; i < backgroundIndexes.length; ++i) backgroundIndexes[i] = backgroundIndexesList.get(i);
        for (int i = 0; i < foregroundIndexes.length; ++i) foregroundIndexes[i] = foregroundIndexesList.get(i);

        // loaded
        loaded = true;
    }

    public void unload() {
        if (!loaded) return;
//        System.out.println("unloaded");
        Entity.disposeAll();
        Laser.disposeALl();
        loaded = false;
    }
}
