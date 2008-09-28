package com.shade.entities;

import java.util.LinkedList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.state.StateBasedGame;

import com.shade.base.Entity;
import com.shade.base.Level;
import com.shade.crash.Body;
import com.shade.controls.MushroomCounter;
import com.shade.shadows.ShadowCaster;

public class Player extends Linkable implements ShadowCaster {

    private static final float SPEED = 1.4f;

    private Level level;
    private Image sprite;

    public boolean shaded;

    private LinkedList<MushroomCounter> counters;

    public Player(float x, float y, float r) throws SlickException {
        initShape(x, y, r);
        initSprite();
        counters = new LinkedList<MushroomCounter>();
    }

    private void initSprite() throws SlickException {
        sprite = new Image("entities/player/player.png");
    }

    private void initShape(float x, float y, float r) {
        shape = new Circle(x, y, r);
    }

    public Role getRole() {
        return Role.PLAYER;
    }

    public void addToLevel(Level l) {
        level = l;
    }

    public void removeFromLevel(Level l) {
        // TODO Auto-generated method stub
    }

    public void onCollision(Entity obstacle) {
        collectMushrooms(obstacle);
        /* Or... */
        moveOutOfIntersection(obstacle);
    }

    private void moveOutOfIntersection(Entity obstacle) {
        if (obstacle.getRole() == Role.OBSTACLE) {
            Body b = (Body) obstacle;
            b.repel(this);
        }
    }

    private void collectMushrooms(Entity obstacle) {
        if (obstacle.getRole() == Role.BASKET && next != null) {
            notifyCounters();

            Linkable head = next;
            while (head != null) {
                head.detach();
                level.remove(head);
                head = next;
            }
            next = null;
        }
    }

    private void notifyCounters() {
        for (MushroomCounter c : counters) {
            c.onCollect((Mushroom) next);
        }
    }

    public void render(StateBasedGame game, Graphics g) {
        sprite.drawCentered(getCenterX(), getCenterY());
        // g.draw(shape);
    }

    public void update(StateBasedGame game, int delta) {
        testAndMove(game.getContainer().getInput(), delta);
        testAndWrap();
    }

    private void testAndMove(Input input, int delta) {
        xVelocity = 0;
        yVelocity = 0;
        if (input.isKeyDown(Input.KEY_LEFT)) {
            xVelocity--;
        }
        if (input.isKeyDown(Input.KEY_RIGHT)) {
            xVelocity++;
        }
        if (input.isKeyDown(Input.KEY_UP)) {
            yVelocity--;
        }
        if (input.isKeyDown(Input.KEY_DOWN)) {
            yVelocity++;
        }
        double mag = Math.sqrt(xVelocity * xVelocity + yVelocity * yVelocity);
        // make it uniform speed
        xVelocity = (float) (1.0 * SPEED * xVelocity / mag);
        yVelocity = (float) (1.0 * SPEED * yVelocity / mag);
        if (mag != 0) {
            move(xVelocity, yVelocity);
        } else {
            xVelocity = 0;
            yVelocity = 0;
        }
        if (getCenterX() <= 0) {
            shape.setCenterX(799);
        }
        if (getCenterX() > 799) {
            shape.setCenterX(0);
        }
        if (getCenterY() <= 0) {
            shape.setCenterY(599);
        }
        if (getCenterY() > 599) {
            shape.setCenterY(0);
        }
    }

    public Shape castShadow(float direction, float depth) {
        return null;
    }

    public int getZIndex() {
        return 4;
    }

    public int compareTo(ShadowCaster s) {
        return getZIndex() - s.getZIndex();
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

    public void add(MushroomCounter counter) {
        counters.add(counter);
    }

}
