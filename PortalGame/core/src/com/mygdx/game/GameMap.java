package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class GameMap {
    static protected TiledMapRenderer tiledMapRenderer;
    static protected TmxMapLoader tmxMapLoader = new TmxMapLoader();

    protected TiledMap tiledMap;
    protected OrthographicCamera camera;


    public GameMap(OrthographicCamera camera, String tiledMapDirectory) {
        this.camera = camera;
        this.tiledMap = tmxMapLoader.load(tiledMapDirectory);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        tiledMapRenderer.setView(this.camera);
    }

    public void render () {
        tiledMapRenderer.render();
    }
}
