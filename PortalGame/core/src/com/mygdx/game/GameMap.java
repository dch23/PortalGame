package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Iterator;

public class GameMap {
    static protected TiledMapRenderer tiledMapRenderer;
    static protected TmxMapLoader tmxMapLoader = new TmxMapLoader();

    protected TiledMap tiledMap;
    protected OrthographicCamera camera;


    public GameMap(World world, String tiledMapDirectory, OrthographicCamera camera) {
        this.camera = camera;

        // Load map
        this.tiledMap = tmxMapLoader.load(tiledMapDirectory);
        MapProperties mapProperties = tiledMap.getProperties();

        // Create scale
        float scale = Math.min(MyGdxGame.SCENE_WIDTH / (mapProperties.get("width", Integer.class) * mapProperties.get("tilewidth", Integer.class)) * MyGdxGame.GAME_SCALE,
                MyGdxGame.SCENE_HEIGHT / (mapProperties.get("height", Integer.class) * mapProperties.get("tileheight", Integer.class)) * MyGdxGame.GAME_SCALE
        );

        // Create Renderer
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, scale);
        tiledMapRenderer.setView(this.camera);

        // Create Bodies
        MapLayers layers = this.tiledMap.getLayers();
        for (int a = 0; a < layers.getCount(); a++) {
            MapLayer layer = layers.get(a);

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
