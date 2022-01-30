package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class Laser {
    static ShapeRenderer shapeRenderer = new ShapeRenderer();
    static ArrayList<Laser> lasers = new ArrayList<>();

    private World world;
    private Vector2 position;
    private Color color;
    private  int circleSegments = 40;
    private float thickness;
    private float maxLength;

    private Vector2 direction;

    public float angle;

    static public void setProjectionMatrix(Matrix4 matrix4) {
        shapeRenderer.setProjectionMatrix(matrix4);
    }

    public Laser(World world, Vector2 position, Color color, float angle, float thickness, float maxLength) {
        this.world = world;
        this.position = position;

        this.color = color;
        this.thickness = thickness;
        this.maxLength = maxLength;
        setAngle(angle);
        //sounds
//        sounds.put("LaserSound", Gdx.audio.newSound(Gdx.files.internal("music/laser sound.mp3")));

        // add to lasers list
        lasers.add(this);
    }

    static public void beginRender() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }
    static public void endRender() {
        shapeRenderer.end();
    }

    public static void operate() {
        if (lasers.size() == 0) return;

        beginRender();
        for (Laser laser : lasers) {
            laser.render();
        }
        endRender();
    }



    public void render() {
        shapeRenderer.setColor(this.color);
        shapeRenderer.rectLine(this.position, getLaserEnd(), this.thickness);
        shapeRenderer.circle(getPosition().x, getPosition().y, this.thickness/2f, this.circleSegments);
        shapeRenderer.circle(getLaserEnd().x, getLaserEnd().y, this.thickness/2f, this.circleSegments);
    }

    private Vector2 getLaserEnd() {
        Vector2 end = PMath.addVector2(getPosition(), PMath.multVector2(direction, maxLength));

        RayHitInfo ray =  PMath.getClosestRayHitInfo(world, getPosition(), end, false);
        if (ray != null) {
            end = ray.point;
        }

        return end;
    }


    public Vector2 getPosition() {
        return new Vector2(this.position);
    }

    public void setAngle(float angle) {
        direction = new Vector2((float) Math.cos(Math.toRadians(angle)), (float) Math.sin(Math.toRadians(angle)));
        this.angle = angle;
    }

    private void dispose() {

    }


    public static void disposeALl() {
        shapeRenderer.dispose();
        for (Laser laser : lasers) {
            laser.dispose();
        }
        lasers = new ArrayList<>();

        // reset Shape Renderer
        Matrix4 matrix4 = shapeRenderer.getProjectionMatrix();
        shapeRenderer = new ShapeRenderer();
        setProjectionMatrix(matrix4);

    }
}
