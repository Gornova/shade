package com.shade.entities;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import com.shade.base.Entity;
import com.shade.base.Level;
import com.shade.crash.Body;
import com.shade.crash.util.CrashGeom;
import com.shade.shadows.ShadowEntity;
import com.shade.shadows.ShadowLevel;
import com.shade.util.Geom;

public class Mole extends Linkable implements ShadowEntity {

    private static final int RADIUS = 12;
    private static final float SPEED = .7f;

    private enum Status {
        DIGGING, WAKING, IDLING, SEEKING, WORKING, CONFUSED
    }

    private ShadowLevel level;
    private Status status;
    private Mushroom target;
    private float heading;
    private int timer, cooldown;
    private Animation sniff, move;
    private ShadowIntensity shadowStatus;

    public Mole(int cool) throws SlickException {
        initShape();
        initSprites();
        status = Status.DIGGING;
        cooldown = cool;
        heading = (float) Math.PI;
    }

    private void initShape() {
        shape = new Circle(0, 0, RADIUS);
    }

    private void initSprites() throws SlickException {
        SpriteSheet sniffs = new SpriteSheet("entities/mole/sniff.png", 40, 40);

        sniff = new Animation(sniffs, 300);
        sniff.setAutoUpdate(false);
        sniff.setPingPong(true);

        SpriteSheet moves = new SpriteSheet("entities/mole/move.png", 40, 40);

        move = new Animation(moves, 300);
        move.setAutoUpdate(false);
    }

    public void addToLevel(Level l) {
        level = (ShadowLevel) l;
    }

    public Role getRole() {
        return Role.MOLE;
    }

    public boolean hasIntensity(ShadowIntensity s) {
        return s == shadowStatus;
    }

    public void setIntensity(ShadowIntensity s) {
        shadowStatus = s;
    }

    public void onCollision(Entity obstacle) {
        if (status == Status.DIGGING) {
            return;
        }
        if (status != Status.WORKING && obstacle.getRole() == Role.MUSHROOM) {
            // start working
            heading += Math.PI;
            target = (Mushroom) obstacle;
            status = Status.WORKING;
            timer = 0;
        }

        if (status == Status.WORKING && obstacle.getRole() == Role.OBSTACLE) {
            // change direction
            heading += Math.PI / 2;
        }

        if (status == Status.SEEKING && obstacle.getRole() == Role.OBSTACLE) {
            // die
            stopWork();
        }

        if (status == Status.WAKING && obstacle.getRole() == Role.OBSTACLE) {
            // back underground
            status = Status.DIGGING;
        }
        if (obstacle.getRole() == Role.PLAYER) {
            // he got ya
            repel(obstacle);
        }
    }

    public void removeFromLevel(Level l) {
        // TODO Auto-generated method stub

    }

    public void render(StateBasedGame game, Graphics g) {
        if (status == Status.DIGGING) {
            return;
        }
        g.rotate(getCenterX(), getCenterY(), (float) Math.toDegrees(heading));
        if (status == Status.WAKING || status == Status.IDLING) {
            sniff.draw(getX(), getY(), getWidth(), getHeight());
        }
        if (status == Status.SEEKING || status == Status.WORKING) {
            move.draw(getX(), getY(), getWidth(), getHeight());
        }
        g.resetTransform();

        // g.draw(shape);
    }

    public void update(StateBasedGame game, int delta) {
        timer += delta;
        sniff.update(delta);
        move.update(delta);
        if (status == Status.DIGGING && timer > cooldown) {
            Vector2f p = level.randomPoint(game.getContainer());
            shape.setCenterX(p.x);
            shape.setCenterY(p.y);
            status = Status.WAKING;
            sniff.restart();
            timer = 0;
        }
        if (status == Status.WAKING && timer > cooldown * 1.5) {
            // wake up!
            timer = 0;
            status = Status.IDLING;
        }
        if (status == Status.IDLING && findTarget()) {
            // target found
            // status = Status.SEEKING;
            // move.restart();
            // TODO figure out where to put state changes
        }
        if (status == Status.IDLING && timer > cooldown) {
            // go back underground start over
            stopWork();
        }
        if (status == Status.SEEKING) {
            // move towards target
            seekTarget();
        }
        if (status == Status.SEEKING && target.isDead()) {
            status = Status.WAKING;
            heading = (float) Math.PI;
            timer = 0;
        }
        if (status == Status.WORKING) {
            // move the target
            move(SPEED, heading);
        }
        if (status == Status.WORKING && timer > cooldown) {
            stopWork();
        }
        if (status == Status.WORKING && target.isDead()) {
            stopWork();
        }
        testAndWrap();
    }

    private void seekTarget() {
        float[] d = new float[3];
        d[0] = CrashGeom.distance2(target, this);
        d[1] = d[0];
        d[2] = d[0];
        // if I'm left of my target
        if (getX() < target.getX()) {
            d[1] = CrashGeom
                    .distance2(target, getCenterX() + 800, getCenterY());
        } else {
            d[1] = CrashGeom.distance2(this, target.getCenterX() + 800, target
                    .getCenterY());
        }

        // if I'm above my target
        if (getY() < target.getY()) {
            d[2] = CrashGeom
                    .distance2(target, getCenterX(), getCenterY() + 600);
        } else {
            d[2] = CrashGeom.distance2(this, target.getCenterX(), target
                    .getCenterY() + 600);
        }

        heading = CrashGeom.calculateAngle(target, this);
        if (d[1] < d[0] || d[2] < d[0]) {
            heading += Math.PI;
        }

        move(SPEED, heading);
    }

    private boolean findTarget() {
        ShadowEntity[] entities = level.nearByEntities(this, 300);

        boolean lineOfSight = false;
        int i = 0;
        while (!lineOfSight && i < entities.length) {
            if (((Entity) entities[i]).getRole() == Role.MUSHROOM) {
                lineOfSight = level.lineOfSight(this, entities[i]);
            }
            i++;
        }
        i--;

        if (lineOfSight) {
            target = (Mushroom) entities[i];
            status = Status.SEEKING;
            move.restart();
            return true;
        }
        return false;
    }

    private void stopWork() {
        status = Status.DIGGING;
        timer = 0;
        heading = (float) Math.PI;
        if (next != null) {
            Linkable head = next;
            while (head != null) {
                head.detach();
                head = next;
            }
            next = null;
            target = null;
        }
    }

    /* Move the shape a given amount across two dimensions. */
    private void move(float magnitude, float direction) {
        Vector2f d = Geom.calculateVector(magnitude, direction);
        shape.setCenterX(shape.getCenterX() + d.x);
        shape.setCenterY(shape.getCenterY() + d.y);
    }

    public void repel(Entity repellee) {
        Body b = (Body) repellee;
        double playerx = b.getCenterX();
        double playery = b.getCenterY();
        double dist_x = playerx - getCenterX();
        double dist_y = playery - getCenterY();
        double mag = Math.sqrt(dist_x * dist_x + dist_y * dist_y);
        double playradius = b.getWidth() / 2;
        double obstacleradius = getWidth() / 2;
        double angle = Math.atan2(dist_y, dist_x);
        double move = (playradius + obstacleradius - mag) * 1.5;
        b.move(Math.cos(angle) * move, Math.sin(angle) * move);
    }

    public boolean digging() {
        return status == Status.DIGGING;
    }

    public int getZIndex() {
        return 4;
    }

    public int compareTo(ShadowEntity s) {
        return getZIndex() - s.getZIndex();
    }

}
