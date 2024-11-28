package io.github.AngryBird_2023124_2023371.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.AngryBird_2023124_2023371.AngryBirds;

public class levelLose implements Screen {
    private Stage stage;
    private AngryBirds game;
    private Texture backgroundTexture;
    private TextButton mainMenuButton;
    private PlayScreen playScreen;
    public levelLose(final AngryBirds game) {
        this.game = game;
        this.playScreen = new PlayScreen(game);
        // Set up the stage
        stage = new Stage(new StretchViewport(800, 400));
        Gdx.input.setInputProcessor(stage);

        // Load a background texture (optional)
        backgroundTexture = new Texture(Gdx.files.internal("LoosingScreen.jpeg")); // Replace with actual file

        // Create a skin for the button
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Create the "Go to Main Menu" button
        mainMenuButton = new TextButton("Home", skin);
        mainMenuButton.setSize(200, 50);
        mainMenuButton.setPosition(300, 75); // Centered in the screen

        // Add a listener to the button
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Redirect to the main menu
                game.setScreen(new mainMenuScreen(game,playScreen));
            }
        });

        // Add the button to the stage
        stage.addActor(mainMenuButton);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the background
        stage.getBatch().begin();
        if (backgroundTexture != null) {
            stage.getBatch().draw(backgroundTexture, 0, 0, 800, 400);
        }
        stage.getBatch().end();

        // Draw the stage
        stage.act(delta);
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
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}
