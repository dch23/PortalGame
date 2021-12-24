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
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Iterator;

public class GameMap {
    static protected TiledMapRenderer tiledMapRenderer;
    static protected TmxMapLoader tmxMapLoader = new TmxMapLoader();

    protected TiledMap tiledMap;
    protected OrthographicCamera camera;

    protected float renderScale;


    public GameMap(World world, String tiledMapDirectory, OrthographicCamera camera) {
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
        MapLayers layers = this.tiledMap.getLayers();
        MapLayer collisionLayer = layers.get("Collision");
        MapObjects objects = collisionLayer.getObjects();
        for (int i = 0; i < objects.getCount(); i++) {
            MapObject object = objects.get(i);

            Vector2 position = new Vector2((float)object.getProperties().get("x"), (float)object.getProperties().get("y"));
            Vector2 size = new Vector2((float)object.getProperties().get("width"), (float)object.getProperties().get("height"));

            // scale
            position = PMath.multVector2(position, this.renderScale);
            size = PMath.multVector2(size, this.renderScale);

            // translate
            position.add(PMath.multVector2(size, 0.5f));

            float density = 1;
            float friction = 0.1f;
//            System.out.println("position: " + position + ", size: " + size);

            Entity newEntity = new Entity(world, "map object", position, size, BodyDef.BodyType.StaticBody, null, density, friction, false, null);
        }
    }

    public void render () {
//        tiledMapRenderer.setView(this.camera);

        tiledMapRenderer.render();
    }

    public void dispose() {
        this.tiledMap.dispose();
    }
}
