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

    private static final float SPEED = 2f;
    
    private Level level;
    private Image sprite;
    
    public boolean shaded;
    private float dx, dy;

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
        pickMushroom(obstacle);
        /* Or... */
        collectMushrooms(obstacle);
        /* Or... */
        moveOutOfIntersection(obstacle);
    }

    private void moveOutOfIntersection(Entity obstacle) {
        if (obstacle.getRole() == Role.OBSTACLE) {
        	Body b = (Body)obstacle;
        	b.repel(this);
        }
    }

    private void collectMushrooms(Entity obstacle) {
        if (obstacle.getRole() == Role.BASKET && next != null) {
            notifyCounters();
            Linkable head = next;
            while (head.next != null) {
                Linkable m = head;
                head = head.next;
                m.prev = null; /* Kill the node. */
                m.next = null;
                level.remove(m);
            }
            /* Kill the last damn mushroom. */
            next = null;
            head.prev = null;
            level.remove(head);
        }
    }

    private void notifyCounters() {
        for (MushroomCounter c : counters) {
            c.onCollect((Mushroom) next);
        }
    }

    private void pickMushroom(Entity obstacle) {
        if (obstacle.getRole() == Role.MUSHROOM) {
            Linkable m = (Linkable) obstacle;
            if (next == null) { /* First shroom picked. */
                next = m;
                next.prev = this;
                return; /* Exit, we're finished. */
            }
            Linkable head = next;
            /* Cycle through list and add to end, watch out for duplicates. */
            while (!head.equals(m) && head.next != null) {
                head = (Linkable) head.next;
            }
            if (m.equals(head)) {
                return; /* Exit we're not dealing with a duplicate. */
            }
            head.next = m;
            m.prev = head;
        }
    }

    public void render(Graphics g) {
        sprite.drawCentered(getCenterX(), getCenterY());
//        g.draw(shape);
    }

    public void update(StateBasedGame game, int delta) {
        testAndMove(game.getContainer().getInput(), delta);
        testAndWrap();
    }

    private void testAndMove(Input input, int delta) {
        dx = 0; 
        dy = 0;
        if (input.isKeyDown(Input.KEY_LEFT)) {
            dx--;
        }
        if (input.isKeyDown(Input.KEY_RIGHT)) {
            dx++;
        }
        if (input.isKeyDown(Input.KEY_UP)) {
            dy--;
        }
        if (input.isKeyDown(Input.KEY_DOWN)) {
            dy++;
        }
    	double mag = Math.sqrt(dx*dx+dy*dy);
        //make it uniform speed
       	dx = (float)(1.0*SPEED*dx/mag);
       	dy = (float)(1.0*SPEED*dy/mag);
        if(mag!=0){
        move(dx,dy);
        }
        else {
        	dx=0;
        	dy =0;
        }
        super.xVelocity = dx;
        super.yVelocity = dy;
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

    public Shape castShadow(float direction) {
//        Vector2f d = Geom.calculateVector(5 * getZIndex(), direction);
//        Transform t = Transform.createTranslateTransform(d.x, d.y);
//        return shape.transform(t);
        return null;
    }

    public int getZIndex() {
        return 3;
    }

    public int compareTo(ShadowCaster s) {
        return getZIndex() - s.getZIndex();
    }

	@Override
	public void repel(Entity repellee) {
		Body b = (Body) repellee;
		double playerx = b.getCenterX();
		double playery = b.getCenterY();
		double dist_x = playerx-getCenterX();
		double dist_y = playery-getCenterY();
		double mag = Math.sqrt(dist_x*dist_x + dist_y*dist_y);
		double playradius = b.getWidth()/2;
		double obstacleradius = getWidth()/2;
		double angle = Math.atan2(dist_y,dist_x);
		double move = (playradius+obstacleradius-mag)*1.5;
		b.move(Math.cos(angle)*move,Math.sin(angle)*move);
	}
    public void add(MushroomCounter counter) {
        counters.add(counter);
    }

}
