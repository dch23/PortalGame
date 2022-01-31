package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.HashMap;

public class PMath {

    static final HashMap<String, Boolean> emptyIgnoreMap = new HashMap<>();

    static public float magnitude(Vector2 v) {
        float x = Math.abs(v.x);
        float y = Math.abs(v.y);
        if (x + y == 0f) return 0;
        return (float) Math.sqrt((double) (x*x + y*y));
    }
    static public Vector2 multVector2(Vector2 a, float scale) {
        return new Vector2(a.x * scale, a.y * scale);
    }
    static public Vector2 multVector2(Vector2 a, Vector2 b) {
        return new Vector2(a.x * b.x, a.y * b.y);
    }
    static public Vector2 addVector2(Vector2 a, Vector2 b) {
        return new Vector2(a.x + b.x, a.y + b.y);
    }
    static public Vector2 subVector2(Vector2 a, Vector2 b) {
        return new Vector2(a.x - b.x, a.y - b.y);
    }
    static public Vector2 divideVector2(Vector2 a, float scale) {
        return new Vector2(a.x/scale, a.y/scale);
    }
    static public Vector2 normalizeVector2(Vector2 v) {
        return divideVector2(v, magnitude(v));
    }

    static public Vector2 absoluteVector2(Vector2 v) {
        return new Vector2(Math.abs(v.x), Math.abs(v.y));
    }

    static public float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    static public RayHitInfo getClosestRayHitInfo(World world, Vector2 startPoint, Vector2 endPoint, boolean detectSensor, HashMap<String, Boolean> ignoreMap) {
        final ArrayList<RayHitInfo> raysHitInfo = new ArrayList<>();

        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture == null || point == null || normal == null) return 0;
                // Multiple hits
                raysHitInfo.add(new RayHitInfo(fixture, new Vector2(point), new Vector2(normal), fraction));
                return 1;
            }
        };

        world.rayCast(callback, startPoint, endPoint);

        // Finding the closest ray hit through a searching algorithm
        if (raysHitInfo.size()==0) return null;
        RayHitInfo closestRayHitInfo = null;
        if (raysHitInfo != null) {
            for (RayHitInfo rayHitInfo : raysHitInfo) {
                if (!detectSensor) if (rayHitInfo.fixture.isSensor()) continue;

                String name = Entity.entityFromBody(rayHitInfo.fixture.getBody()).getName();
                if (name.equals("exit door") || name.equals("fireball") || name.equals("reflect fireball")) continue;

                if (ignoreMap.get(name) != null) continue;

                if (closestRayHitInfo == null) closestRayHitInfo = rayHitInfo;
                float distance1 = PMath.magnitude(PMath.subVector2(closestRayHitInfo.point, startPoint));
                float distance2 = PMath.magnitude(PMath.subVector2(rayHitInfo.point, startPoint));
                if (distance2 < distance1) closestRayHitInfo = rayHitInfo;
            }
        }
        return closestRayHitInfo;
    }
    static public RayHitInfo getClosestRayHitInfo(World world, Vector2 startPoint, Vector2 direction, float length, boolean detectSensor) {
        return getClosestRayHitInfo(world, startPoint, direction, length, detectSensor, emptyIgnoreMap);
    }

    static public RayHitInfo getClosestRayHitInfo(World world, Vector2 startPoint, Vector2 endPoint, boolean detectSensor) {
        return getClosestRayHitInfo(world, startPoint, endPoint, detectSensor, emptyIgnoreMap);
    }

    static public RayHitInfo getClosestRayHitInfo(World world, Vector2 startPoint, Vector2 direction, float length, boolean detectSensor, HashMap<String,Boolean> ignoreMap) {
        Vector2 endPoint = addVector2(startPoint, PMath.multVector2(direction, length));
//        System.out.println("end point found: " + endPoint);
        return getClosestRayHitInfo(world, startPoint, endPoint, detectSensor, ignoreMap);
    }

    public static int getRandomRangeInt(int min, int max) {
        return (int) (Math.random() * (max - min) + min);
    }

    public static float getRandomRangeFloat(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static float dir2Deg(Vector2 direction) {
        float rad = (float) Math.atan2(direction.y, direction.x);
        return (float) Math.toDegrees(rad);
    }

    public static Vector2 deg2dir(float angle) {
        angle = (float) Math.toRadians(angle);
        float x = (float) Math.cos(angle);
        float y = (float) Math.sin(angle);
        return new Vector2(x,y);
    }

    public static boolean inBounds(Vector2 pos, Vector2 b1, Vector2 b2) {
        return (pos.x >= b1.x && pos.x <= b2.x) && (pos.y >= b1.x && pos.y <= b2.y);
    }

    public static boolean inBounds(Vector2 pos, Vector2 size, Vector2 b1, Vector2 b2) {
        Vector2[] verticies = new Vector2[] {
                PMath.addVector2(pos, new Vector2(size.x/2f, size.y/2f)),
                PMath.addVector2(pos, new Vector2(-size.x/2f, size.y/2f)),
                PMath.addVector2(pos, new Vector2(-size.x/2f, -size.y/2f)),
                PMath.addVector2(pos, new Vector2(size.x/2f, -size.y/2f)),
        };
        for (Vector2 v : verticies) {
            if (inBounds(v, b1, b2)) return true;
        }
        return false;
    }
}


