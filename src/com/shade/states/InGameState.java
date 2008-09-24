package com.shade.states;

import java.awt.Font;
import java.io.InputStream;
import java.util.LinkedList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.*;
import org.newdawn.slick.util.ResourceLoader;

import com.shade.controls.*;
import com.shade.crash.*;
import com.shade.entities.*;
import com.shade.shadows.*;

public class InGameState extends BasicGameState {

    public static final int ID = 1;

    private enum Status {
        NOT_STARTED, RUNNING, PAUSED, GAME_OVER
    };

    private Status currentStatus;

    private ShadowLevel level;
    private MeterControl meter;
    private CounterControl counter;

    private Image backgroundSprite, trimSprite;
    private Image counterSprite;
    private TrueTypeFont counterFont;

    private float sunAngle;

    private Player player;

    private int timer;

    @Override
    public int getID() {
        return ID;
    }

    public void init(GameContainer container, StateBasedGame game)
            throws SlickException {
        level = new ShadowLevel(new Grid(8, 6, 100));
        sunAngle = 2.5f;
        currentStatus = Status.NOT_STARTED;
        initSprites();
        initFonts();
    }

    private void initFonts() throws SlickException {
        try {
            InputStream oi = ResourceLoader
                    .getResourceAsStream("states/ingame/jekyll.ttf");
            Font jekyll = Font.createFont(Font.TRUETYPE_FONT, oi);
            counterFont = new TrueTypeFont(jekyll.deriveFont(36f), true);
            ;
        } catch (Exception e) {
            throw new SlickException("Failed to load font.", e);
        }
    }

    private void initSprites() throws SlickException {
        backgroundSprite = new Image("states/ingame/background.png");
        trimSprite = new Image("states/ingame/trim.png");
        counterSprite = new Image("states/ingame/counter.png");
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game)
            throws SlickException {
        currentStatus = Status.RUNNING;

        level.clear();
        level.updateShadowscape(sunAngle);
        meter = new MeterControl(20, 456, 100, 100);
        counter = new CounterControl(60, 520, counterSprite, counterFont);

        initObstacles();
        initBasket();
        initPlayer();
    }

    private void initObstacles() throws SlickException {
        LinkedList<ShadowCaster> casters = new LinkedList<ShadowCaster>();
        casters.add(new Block(55, 355, 125, 125, 16));
        casters.add(new Block(224, 424, 56, 56, 6));
        casters.add(new Block(324, 424, 56, 56, 6));
        casters.add(new Block(75, 225, 56, 56, 6));
        casters.add(new Block(545, 380, 80, 80, 10));
        casters.add(new Block(445, 460, 80, 80, 10));
        // domes
        casters.add(new Dome(288, 165, 32, 7));
        casters.add(new Dome(180, 95, 44, 10));
        casters.add(new Dome(300, 85, 25, 6));
        casters.add(new Dome(680, 70, 28, 6));
        casters.add(new Dome(600, 120, 40, 9));
        casters.add(new Dome(680, 220, 60, 13));
        // fences
        casters.add(new Fence(225, 225, 11, 120, 6));
        casters.add(new Fence(390, 140, 120, 11, 6));
        casters.add(new Fence(715, 368, 11, 120, 6));
        casters.add(new Fence(50, 50, 11, 120, 6));

        for (ShadowCaster c : casters) {
            level.add(c);
        }
    }

    private void initBasket() throws SlickException {
        Basket b = new Basket(400, 250, 65, 40);
        level.add(b);
    }

    private void initPlayer() throws SlickException {
        player = new Player(400, 350, 18);
        player.add(counter);
        player.add(meter);

        level.add(player);
    }

    public void render(GameContainer container, StateBasedGame game, Graphics g)
            throws SlickException {
        backgroundSprite.draw();
        level.render(g);
        trimSprite.draw();
        meter.render(g);
        counter.render(g);
        
        if (currentStatus == Status.GAME_OVER) {
            counterFont.drawString(320, 300, "Game Over");
        }
    }

    public void update(GameContainer container, StateBasedGame game, int delta)
            throws SlickException {
        updateShadow();
        
        if (currentStatus == Status.RUNNING) {
            level.update(game, delta);
            timer += delta;

            // Randomly plant mushrooms
            if (Math.random() > .996 || timer > 4000) {
                timer = 0;
                level.plant();
            }

            meter.update(game, delta);
            counter.update(game, delta);

            if (!player.shaded) {
                meter.decrement();
            }

            // Check for lose condition
            if (meter.isEmpty()) {
                currentStatus = Status.GAME_OVER;
            }
        }
    }

    private void updateShadow() {
        sunAngle += .0005f;
        level.updateShadowscape(sunAngle);
    }

}
