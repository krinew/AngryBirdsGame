package io.github.AngryBird_2023124_2023371.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.AngryBird_2023124_2023371.AngryBirds;

public class PauseMenuScreen implements Screen {
    private Stage stage;
    private AngryBirds game;
    private Texture pauseMenuBackground;
    private ImageButton resumeButton;
    private ImageButton restartButton;
    private ImageButton mainMenuButton;
    
    // Store reference to current level
    private Screen currentLevel;
    private int levelNumber;

    // Update constructor to accept any level screen
    public PauseMenuScreen(final AngryBirds game, final Screen currentLevel) {
        this.game = game;
        this.currentLevel = currentLevel;
        
        // Determine which level we're in
        if (currentLevel instanceof Level1) {
            this.levelNumber = 1;
        } else if (currentLevel instanceof Level2) {
            this.levelNumber = 2;
        } else if (currentLevel instanceof Level3) {
            this.levelNumber = 3;
        }
        
        stage = new Stage(new StretchViewport(800, 400));
        Gdx.input.setInputProcessor(stage);

        pauseMenuBackground = new Texture(Gdx.files.internal("pausemenu_background.png"));

        float buttonWidth = 80;
        float buttonHeight = 50;
        float spacing = 20;
        float startX = 260;
        float buttonY = 150;

        // Create button textures
        Texture resumeTexture = new Texture(Gdx.files.internal("resume_btn.png"));
        ImageButton.ImageButtonStyle resumeStyle = new ImageButton.ImageButtonStyle();
        resumeStyle.up = new TextureRegionDrawable(new TextureRegion(resumeTexture));
        
        resumeButton = new ImageButton(resumeStyle);
        resumeButton.setPosition(startX, buttonY);
        resumeButton.setSize(buttonWidth, buttonHeight);

        Texture restartTexture = new Texture(Gdx.files.internal("restart_btn.png"));
        ImageButton.ImageButtonStyle restartStyle = new ImageButton.ImageButtonStyle();
        restartStyle.up = new TextureRegionDrawable(new TextureRegion(restartTexture));
        
        restartButton = new ImageButton(restartStyle);
        restartButton.setPosition(startX + buttonWidth + spacing, buttonY);
        restartButton.setSize(buttonWidth, buttonHeight);

        Texture mainMenuTexture = new Texture(Gdx.files.internal("mainmenu_btn.png"));
        ImageButton.ImageButtonStyle mainMenuStyle = new ImageButton.ImageButtonStyle();
        mainMenuStyle.up = new TextureRegionDrawable(new TextureRegion(mainMenuTexture));
        
        mainMenuButton = new ImageButton(mainMenuStyle);
        mainMenuButton.setPosition(startX + 2 * (buttonWidth + spacing), buttonY);
        mainMenuButton.setSize(buttonWidth, buttonHeight);

        // Resume button listener
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resumeGame(); // Use the new method
            }
        });

        // Restart button listener
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Screen newLevel = null;
                switch (levelNumber) {
                    case 1:
                        newLevel = new Level1(game);
                        break;
                    case 2:
                        newLevel = new Level2(game);
                        break;
                    case 3:
                        newLevel = new Level3(game);
                        break;
                }
                if (newLevel != null) {
                    game.setScreen(newLevel);
                }
            }
        });

        // Main menu button listener
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new mainMenuScreen(game, null));
            }
        });

        stage.addActor(resumeButton);
        stage.addActor(restartButton);
        stage.addActor(mainMenuButton);
    }

    // Add new method to properly handle resuming
    

private void resumeGame() {
    if (currentLevel instanceof Level1) {
        Level1 level1 = (Level1) currentLevel;
        // Clear existing UI elements
        level1.getStage().clear();
        // Reinitialize stage and UI with fresh values
        level1.reinitializeStage();
        // Set screen and unpause
        game.setScreen(currentLevel);
        level1.setPaused(false);
    } else if (currentLevel instanceof Level2) {
        Level2 level2 = (Level2) currentLevel;
        level2.getStage().clear();
        //level2.reinitializeStage();
        game.setScreen(currentLevel);
        level2.setPaused(false);
    } else if (currentLevel instanceof Level3) {
        Level3 level3 = (Level3) currentLevel;
        level3.getStage().clear();
        //level3.reinitializeStage();
        game.setScreen(currentLevel);
        level3.setPaused(false);
    }
}

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update stage
        stage.act(delta);
        
        // Draw background and stage
        stage.getBatch().begin();
        stage.getBatch().draw(pauseMenuBackground, 0, 0, 800, 400);
        stage.getBatch().end();
        
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (pauseMenuBackground != null) pauseMenuBackground.dispose();
    }
}

