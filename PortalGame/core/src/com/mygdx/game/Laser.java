package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class Laser {
    static ShapeRenderer shapeRenderer = new ShapeRenderer();

    private World world;
    private Vector2 position;
    private Color color;
    private int circleSegments = 40;
    private float thickness;
    private float maxLength;
    private Vector2 direction;

    public float angle;
    private int rotateDirection = 1;



    private ArrayList<RayHitInfo> raysHitInfo;
    private RayHitInfo closestRayHitInfo;

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
    }

    static public void beginRender() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }
    static public void endRender() {
        shapeRenderer.end();
    }

    public void render() {
        shapeRenderer.setColor(this.color);
        shapeRenderer.rectLine(this.position, getLaserEnd(), this.thickness);
        shapeRenderer.circle(getPosition().x, getPosition().y, this.thickness/2f, this.circleSegments);
        shapeRenderer.circle(getLaserEnd().x, getLaserEnd().y, this.thickness/2f, this.circleSegments);
    }

    public void update(){
//        if(angle==45) rotateDirection*=-1;
//        setAngle(this.angle);
    }



    private Vector2 getLaserEnd() {
        raysHitInfo = new ArrayList<>();            // refresh the rays information list
        closestRayHitInfo = null;                   // reset the closest ray to nothing

        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture == null || point == null || normal == null) return 0;
                raysHitInfo.add(new RayHitInfo(fixture, new Vector2(point), new Vector2(normal), fraction));
                return 1;
            }
        };

        Vector2 endRayPosition = PMath.multVector2(this.direction, this.maxLength);
        world.rayCast(callback, getPosition(), PMath.addVector2(getPosition(), endRayPosition));

        // Finding the closest ray hit through a searching algorithm
        if (raysHitInfo!=null) {
            if (raysHitInfo.size() == 0) return endRayPosition;
            closestRayHitInfo = raysHitInfo.get(0);
            for (RayHitInfo rayHitInfo : raysHitInfo) {
                float distance1 = PMath.magnitude(PMath.subVector2(closestRayHitInfo.point, getPosition()));
                float distance2 = PMath.magnitude(PMath.subVector2(rayHitInfo.point, getPosition()));
                if (distance2 < distance1) {
                    closestRayHitInfo = rayHitInfo;
                }
            }
        }
        if (closestRayHitInfo != null) {
            Entity.entityFromBody(closestRayHitInfo.fixture.getBody()).alive=false;
            return closestRayHitInfo.point;
        }
//        System.out.println(endRayPosition);
        return endRayPosition;
    }


    public Vector2 getPosition() {
        return new Vector2(this.position);
    }

    public void setAngle(float angle) {
        this.direction = new Vector2((float) Math.cos(Math.toRadians(angle)), (float) Math.sin(Math.toRadians(angle)));
        this.angle=angle;
//        this.endPosition = PMath.addVector2(this.position, PMath.multVector2(new Vector2((float) Math.cos(angle), (float) Math.sin(angle)), this.maxLength));
//        this.endPosition = getLaserEnd();

    }

    static public void dispose() {
        shapeRenderer.dispose();
    }
}
