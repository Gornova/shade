package com.shade.shadows;

import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Vector2f;

import com.shade.crash.Grid;
import com.shade.entities.Mushroom;

public class Shadowscape {
    
    private Grid grid;
    private LinkedList<Shape> shadows;
    
    public Shadowscape(ZBuffer buffer, float direction, Grid grid) {
        shadows = new LinkedList<Shape>();
        for (ShadowCaster c : buffer) {
            shadows.add(c.castShadow(direction));
        }
        this.grid = grid;
    }
    
    /**
     * Creates a mushroom somewhere on the shadowscape and returns it.
     * 
     * 1. The mushroom is not added to the level yet.
     * 2. The mushroom is in the shadowscape.
     * 3. The mushroom is not intersecting with anything.
     * 4. The mushroom is on the screen.
     * @return
     * @throws SlickException 
     */
    public Mushroom plant() throws SlickException {
        Shape shadow = getRandomShadow();
        
        int tries = 0;
        boolean finished = false;
        Vector2f p = null;
        // TODO protect against going off screen
        while (!finished) {
            if (tries == 3) {
                tries = 0;
                shadow = getRandomShadow();
            }
            tries++;
            p = getRandomPointIn(shadow);
            finished = checkPoint(p, shadow);
        }
        return new Mushroom(p.x, p.y);        
    }

    private boolean checkPoint(Vector2f p, Shape shadow) {
        // is it within bounds
        if (!(p.x > 0 && p.x < 800 && p.y > 0 && p.y < 600)) {
            return false;
        }
        // p is in the shadow
        if (!shadow.contains(p.x, p.y)) {
            return false;
        }
        // p is sufficiently far from another object
        if (!grid.hasRoom(p, 18)) {
            return false;
        }
        // ok we're done probing
        return true;
    }

    private Vector2f getRandomPointIn(Shape shadow) {
        float minX = shadow.getMinX();
        float minY = shadow.getMinY();
        float maxX = shadow.getMaxX();
        float maxY = shadow.getMaxY();
        
        float x = (float) ((maxX - minX) * Math.random()) + minX;
        float y = (float) ((maxY - minY) * Math.random()) + minY;
        
        return new Vector2f(x, y);
    }

    private Shape getRandomShadow() {
        int i = (int) (shadows.size() * Math.random());
        return shadows.get(i);
    }

    /**
     * Draw the shadowscape.
     * 
     * @param g
     */
    public void render(Graphics g) {
        g.flush();
        g.setDrawMode(Graphics.MODE_NORMAL);
        Color shade = Color.black;
//        shade.a = .5f;
        g.setColor(shade);
        for (Shape s : shadows) {
            g.fill(s);
        }
        g.flush();
        g.setDrawMode(Graphics.MODE_NORMAL);
        g.setColor(Color.white);
    }

}
