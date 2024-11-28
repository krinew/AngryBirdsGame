package io.github.AngryBird_2023124_2023371.Screens;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.AngryBird_2023124_2023371.Screens.*;
import io.github.AngryBird_2023124_2023371.AngryBirds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.io.*;

import com.badlogic.gdx.utils.Json;

import com.badlogic.gdx.files.FileHandle;

public class Level1 implements Screen,Serializable{

   private static final long serialVersionUID = 1L;
   // Game and rendering
   private AngryBirds game;
   private transient OrthographicCamera camera;
   private transient SpriteBatch batch;
   private transient Stage stage;

   // Physics
   private transient World world;
   private transient Box2DDebugRenderer debugRenderer;

   // Assets
   private transient Texture backgroundTexture;
   private transient Texture catapultTexture;
   private transient TextureAtlas atlas;
   private List<Vector2> trajectoryPoints; // Stores the predicted trajectory points


   // Game objects
   private Vector2 catapultPosition;
   private Vector2[] birdWaitingPositions;
   private boolean[] birdUsed;
   private List<BaseBird> birds;
   private List<BasePig> pigs;
   private List<BaseBlock> blocks;
   private Array<Body> bodiesToDestroy;
   private transient ShapeRenderer shapeRenderer;

   // Game state
   private int currentBirdIndex = 0;
   private boolean isBirdInFlight = false;
   private BaseBird currentBird = null;
   private BaseBird birdOnCatapult = null;
   private boolean[] birdLaunched; // Track which birds have been launched

   // UI
   private  transient Slider powerSlider;
   private  transient Slider angleSlider;
   private transient Label powerLabel;
   private transient Label angleLabel;
   private TextButton launchButton;
   private transient ImageButton.ImageButtonStyle pauseButtonStyle;
   private transient Texture pauseTexture;

   // Constants
   private static final float PIG_SCALE = 0.07f;
   private static final float PPM = 100f; // Pixels per meter for Box2D
   private static final float WORLD_WIDTH = 800f;
   private static final float WORLD_HEIGHT = 480f;
   private static final float BIRD_SCALE = 0.15f;
   private static final float BLOCK_SCALE = 0.1f; // Adjust this value (0.1 = 10% size)
   private transient StretchViewport viewport;
   private static final float CATAPULT_X = 100f;
   private static final float CATAPULT_Y = 100f;
   private static final float CATAPULT_SCALE = 0.8f; // 80% of original size
   private static final float CATAPULT_HEAD_X = CATAPULT_X + 20f; // Adjust based on catapult sprite
   private static final float CATAPULT_HEAD_Y = CATAPULT_Y + 60f; // Height for bird on catapult
   private static final float GROUND_Y = 100f; // Adjust based on ground height
   private static final float BIRD_SPACING = 50f; // Space between waiting birds
   private static final float MAX_LAUNCH_POWER = 20f; // Adjust based on testing
   private static final float LAUNCH_POWER_MULTIPLIER = 0.5f;
   private static final float MAX_LAUNCH_SPEED = 10f; // Adjust this value as needed
   private static final float SPEED_MULTIPLIER = 0.5f; // Reduces the overall speed
   private static final float VELOCITY_THRESHOLD = 0.1f;
   private static final float LOSE_CHECK_DELAY = 2.0f;
   private static final float GROUND_CHECK_OFFSET = 20f;
   private static final float REMOVE_VELOCITY_THRESHOLD = 0.005f;
   private static final float REMOVE_HEIGHT_THRESHOLD = GROUND_Y + 20f;


   private boolean gameEnded = false;


   private List<BaseBird> birdsToRemove = new ArrayList<>();


   private int totalBirds;
   private int removedBirdsCount = 0;


   private static final float FINAL_CHECK_DELAY = 9.0f;
   private float finalCheckTimer = 0;
   private boolean finalCheckStarted = false;

   // Add new fields
   private static final String SAVE_FILE = "level1_save.dat";
   private boolean isPaused = false;

   public Level1(AngryBirds game) {
       this.game = game;

       // Initialize camera and viewport
       bodiesToDestroy = new Array<>();
       camera = new OrthographicCamera();
       viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

       // Use viewport for stage
       stage = new Stage(viewport);
       batch = new SpriteBatch();
       Gdx.input.setInputProcessor(stage);

       // Center camera
       camera.position.set(WORLD_WIDTH/2, WORLD_HEIGHT/2, 0);
       camera.update();


       // Rest of constructor remains same
       world = new World(new Vector2(0, -9.8f), true); // Standard gravity (was -19.6f)
       world.setContactListener(new MyContactListener());
       createBoundaries(); // Add this line
       debugRenderer = new Box2DDebugRenderer();

       loadAssets();

       catapultPosition = new Vector2(CATAPULT_X, CATAPULT_Y + 20f); // Slightly above catapult
       initializeBirdPositions();


       setupGameObjects();
       createUI();
       createGround();
       birdLaunched = new boolean[3];
       Arrays.fill(birdLaunched, false);
       totalBirds = birds.size(); // Set after birds are created

   }


   private void initializeBirdPositions() {
       // Initialize waiting positions for birds on ground
       birdWaitingPositions = new Vector2[3];
       birdWaitingPositions[0] = new Vector2(CATAPULT_X - 130f, GROUND_Y);
       birdWaitingPositions[1] = new Vector2(CATAPULT_X - 180f, GROUND_Y);
       birdWaitingPositions[2] = new Vector2(CATAPULT_X - 230f, GROUND_Y);

       birdUsed = new boolean[3];
       Arrays.fill(birdUsed, false);
   }



   private void loadAssets() {
       backgroundTexture = new Texture(Gdx.files.internal("level1back.jpg"));
       catapultTexture = new Texture(Gdx.files.internal("assetsforatlas/catapult_1.png"));
       atlas = new TextureAtlas(Gdx.files.internal("character.atlas"));
   }

   private void setupGameObjects() {
       birds = new ArrayList<>();
       pigs = new ArrayList<>();
       blocks = new ArrayList<>();

       for (int i = 0; i < 3; i++) {
           final int birdIndex = i;
           BaseBird bird = null;

           if(i == 2) {
               bird = new BombBird(atlas, birdWaitingPositions[i], world);
               bird.setScale(BIRD_SCALE*0.4f);
           } else {
               bird = new RedBird(atlas, birdWaitingPositions[i], world);
               bird.setScale(BIRD_SCALE);
           }


           // Set initial physics properties
           Body body = bird.getBody();
           if (body != null) {
               body.setType(BodyDef.BodyType.DynamicBody); // Make it affected by gravity
               body.setGravityScale(1); // Normal gravity for ground birds
               body.setFixedRotation(true); // Prevent rotation
           }

           bird.addListener(new ClickListener() {
               @Override
               public void clicked(InputEvent event, float x, float y) {
                   if (!birdUsed[birdIndex] && !isBirdInFlight) {
                       selectBird(birdIndex);
                   }
               }
           });

           stage.addListener(new ClickListener() {
            private long lastClickTime = 0;
            private static final long DOUBLE_CLICK_TIME = 300; // ms

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < DOUBLE_CLICK_TIME) {
                    // Double click detected
                    for (BaseBird bird : birds) {
                        if (bird instanceof BombBird && bird.getState() == BaseBird.BirdState.LAUNCHED) {
                            ((BombBird) bird).explode();
                        }
                        else if (bird instanceof YellowBird && bird.getState() == BaseBird.BirdState.LAUNCHED) {
                            ((YellowBird) bird).activateSpeedBoost();
                        }
                    }
                }
                lastClickTime = currentTime;
                return super.touchDown(event, x, y, pointer, button);
            }
        });



           birds.add(bird);
           stage.addActor(bird);
       }
       currentBirdIndex = 0;
       birds.get(currentBirdIndex).setPosition(
           catapultPosition.x - birds.get(currentBirdIndex).getWidth()/2,
           catapultPosition.y - birds.get(currentBirdIndex).getHeight()/2
       );

       float rightTowerX = WORLD_WIDTH * .9f + 20f;
       float leftTowerX = WORLD_WIDTH * 0.5f;
       float baseX = leftTowerX + 130f;
       float baseY = 80f;
       float blockSpacing = 70f;

       // First tower (2 blocks)
       for (int i = 0; i < 2; i++) {
           float x = rightTowerX + 35 - 200 + 13f;
           float y = baseY + i * blockSpacing;
           BaseBlock block = new GlassBlock(new Vector2(x, y), world);
           blocks.add(block);
       }

       // Second tower (3 blocks)
       for (int i = 0; i < 3; i++) {
           float x = rightTowerX + 35 - 200 + 13f + 40f;
           float y = baseY + i * blockSpacing;
           BaseBlock block = new GlassBlock(new Vector2(x, y), world);
           blocks.add(block);
       }

       // Third tower (4 blocks)
       for (int i = 0; i < 4; i++) {
           float x = rightTowerX + 35 - 200 + 13f + 80f;
           float y = baseY + i * blockSpacing;
           BaseBlock block = new GlassBlock(new Vector2(x, y), world);
           blocks.add(block);
       }

       // Add pigs on towers
       BasePig rightPig = new MechanicPig(atlas,
           new Vector2(rightTowerX + 35 - 200 + 13f + 20f, 350), world);
       rightPig.setScale(PIG_SCALE);
       pigs.add(rightPig);
       stage.addActor(rightPig);
       stage.addActor(rightPig.getHealthLabel());

       BasePig middlePig = new MechanicPig(atlas,
           new Vector2(rightTowerX + 35 - 200 + 13f + 40f + 20f, 350), world);
       middlePig.setScale(PIG_SCALE);
       pigs.add(middlePig);
       stage.addActor(middlePig);
       stage.addActor(middlePig.getHealthLabel());

       BasePig leftPig = new MechanicPig(atlas,
           new Vector2(rightTowerX + 35 - 200 + 13f + 80f + 20f, 350), world);
       leftPig.setScale(PIG_SCALE);
       pigs.add(leftPig);
       stage.addActor(leftPig);
       stage.addActor(leftPig.getHealthLabel());
       // Add blocks to the stage in order (bottom to top)
       for (BaseBlock block : blocks) {
           block.setScale(BLOCK_SCALE);
           stage.addActor(block);
       }

       createGround();
   }

   private void selectBird(int birdIndex) {
       // Check if bird exists and hasn't been launched
       if (birdIndex >= 0 && birdIndex < birds.size() && !birdLaunched[birdIndex] && !isBirdInFlight) {
           BaseBird selectedBird = birds.get(birdIndex);

           // Store the clicked bird's original position
           Vector2 oldPosition = new Vector2(selectedBird.getX(), selectedBird.getY());

           // If there's a bird on catapult, return it to ground
           if (birdOnCatapult != null) {
               int oldBirdIndex = birds.indexOf(birdOnCatapult);
               if (oldBirdIndex != -1 && !birdLaunched[oldBirdIndex]) {
                   birdOnCatapult.returnToGround(oldPosition.x, oldPosition.y);
                   birdOnCatapult.makeStatic();
                   birdOnCatapult.getBody().setGravityScale(1);
               }
           }

           // Move selected bird to catapult
           selectedBird.placeOnCatapult(CATAPULT_HEAD_X, CATAPULT_HEAD_Y);
           selectedBird.makeStatic();
           birdOnCatapult = selectedBird;
           currentBirdIndex = birdIndex;
       }
   }

   private void launchCurrentBird() {
       if (birdOnCatapult != null && birdOnCatapult.getState() == BaseBird.BirdState.ON_CATAPULT && !gameEnded) {
           // Get values from sliders
           float power = powerSlider.getValue() * LAUNCH_POWER_MULTIPLIER;
           float angle = angleSlider.getValue();
           // Convert angle to radians
           float angleRad = (float) Math.toRadians(angle);

           // Calculate launch vector
           float forceX = power * (float) Math.cos(angleRad);
           float forceY = power * (float) Math.sin(angleRad);

           // Prepare bird for launch
           Body birdBody = birdOnCatapult.getBody();
           birdBody.setType(BodyDef.BodyType.DynamicBody);
           birdBody.setGravityScale(1);
           birdBody.setAwake(true);
           birdBody.setFixedRotation(false);

           // Apply launch force
           Vector2 force = new Vector2(forceX, forceY);
           Vector2 position = birdBody.getWorldCenter();
           birdBody.applyLinearImpulse(force, position, true);

           // Update bird state and list
           birdOnCatapult.setState(BaseBird.BirdState.LAUNCHED);
           birdUsed[currentBirdIndex] = true;

           // Reset state variables
           birdOnCatapult = null;

           Gdx.app.log("Bird Launch", "Bird " + currentBirdIndex + " launched");

       }
       else{
           System.out.println( "All Birds have been launched!!! Now displaying the results screen from the level wheather the birds won or lost!!");
       }
   }



   private void createUI() {
       Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

       // Clear any existing UI elements
       if (powerSlider != null) powerSlider.remove();
       if (angleSlider != null) angleSlider.remove();
       if (powerLabel != null) powerLabel.remove();
       if (angleLabel != null) angleLabel.remove();
       if (launchButton != null) launchButton.remove();

       // Create fresh sliders with default values
       powerSlider = new Slider(0, MAX_LAUNCH_POWER, 1, false, skin);
       powerSlider.setValue(0); // Reset to default
       angleSlider = new Slider(0, 90, 1, false, skin);
       angleSlider.setValue(45); // Reset to default

       // Power Slider
       powerLabel = new Label("Power", skin);
       powerLabel.setPosition(180-130f, 370-50f);
       powerLabel.setFontScale(0.7f);
       stage.addActor(powerLabel);

       powerSlider = new Slider(0, 100, 1, false, skin);
       powerSlider.setPosition(245-130f, 380-50f);
       powerSlider.setSize(120, 10);
       stage.addActor(powerSlider);

       // Angle Slider
       angleLabel = new Label("Angle", skin);
       angleLabel.setPosition(180-130f, 410-50f);
       angleLabel.setFontScale(0.7f);
       stage.addActor(angleLabel);

       angleSlider = new Slider(0, 90, 1, false, skin);
       angleSlider.setPosition(245-130f, 420-50f);
       angleSlider.setSize(120, 10);
       stage.addActor(angleSlider);

       // Launch Button
       launchButton = new TextButton("Launch", skin);
       launchButton.setPosition(120+10f, 275+5f);
       launchButton.setSize(70, 30);
       launchButton.getLabel().setAlignment(Align.center);
       launchButton.getLabelCell().pad(5);
       launchButton.getLabel().setFontScale(0.5f);

       powerSlider.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               updateTrajectory();
           }
       });

       angleSlider.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               updateTrajectory();
           }
       });

       launchButton.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               if (!isBirdInFlight && birdOnCatapult != null) {
                   launchCurrentBird();
               }
           }
       });




       TextButton saveLoadButton = new TextButton("Save/Load", skin);
       saveLoadButton.addListener(new ClickListener() {
       @Override
       public void clicked(InputEvent event, float x, float y) {
           game.setScreen(new SaveLoadMenu(game, Level1.this));
       }
       });

   stage.addActor(saveLoadButton);

       stage.addActor(launchButton);

       // Add Pause Button
       pauseButtonStyle = new ImageButton.ImageButtonStyle();
       pauseTexture = new Texture(Gdx.files.internal("pausebtn.png"));
       pauseButtonStyle.up = new TextureRegionDrawable(new TextureRegion(pauseTexture));
       pauseButtonStyle.up = new TextureRegionDrawable(new TextureRegion(pauseTexture));

       ImageButton pauseButton = new ImageButton(pauseButtonStyle);
       pauseButton.setPosition(700, 330); // Adjust position as needed
       pauseButton.setSize(200, 200); // Adjust size as needed

       pauseButton.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               setPaused(true);
               saveGameToJson(SaveSlot.SLOT_1);
               game.setScreen(new PauseMenuScreen(game, Level1.this));
           }
       });

       stage.addActor(pauseButton);
   }

   private void updateTrajectory() {
       if (birdOnCatapult != null) {
           trajectoryPoints = new ArrayList<>();

           float power = powerSlider.getValue() * LAUNCH_POWER_MULTIPLIER;
           float angle = angleSlider.getValue();
           float angleRad = (float) Math.toRadians(angle);

           // Initial velocity components
           float velocityX = power * (float) Math.cos(angleRad);
           float velocityY = power * (float) Math.sin(angleRad);

           // Starting position (catapult head position)
           float startX = birdOnCatapult.getBody().getPosition().x * PPM;
           float startY = birdOnCatapult.getBody().getPosition().y * PPM;

           // Time-step for calculating trajectory points
           float timeStep = 0.1f; // Adjust for smoothness

           for (float t = 0; t < 5; t += timeStep) {
               // Calculate position at time t
               float x = startX + velocityX * t;
               float y = startY + velocityY * t - 0.5f * Math.abs(world.getGravity().y) * t * t * PPM;

               // Break if the point goes below ground level
               if (y < GROUND_Y) break;

               trajectoryPoints.add(new Vector2(x, y));
           }
       } else {
           trajectoryPoints = null; // No trajectory to display
       }
   }

   private void createGround() {
       BodyDef groundBodyDef = new BodyDef();
       groundBodyDef.position.set(WORLD_WIDTH / 2 / PPM, 32 / PPM);
       groundBodyDef.type = BodyDef.BodyType.StaticBody;

       Body groundBody = world.createBody(groundBodyDef);

       PolygonShape groundShape = new PolygonShape();
       groundShape.setAsBox(WORLD_WIDTH / 2 / PPM, 32 / PPM);

       FixtureDef fixtureDef = new FixtureDef();
       fixtureDef.shape = groundShape;
       fixtureDef.density = 1.0f;
       fixtureDef.friction = 0.4f;
       fixtureDef.restitution = 0.2f;

       groundBody.createFixture(fixtureDef);
       groundShape.dispose();
   }

   private void createBoundaries() {
       // Create body definition for static boundaries
       BodyDef boundaryDef = new BodyDef();
       boundaryDef.type = BodyDef.BodyType.StaticBody;

       // Create fixture definition
       FixtureDef fixtureDef = new FixtureDef();
       fixtureDef.friction = 0.3f;
       fixtureDef.restitution = 0.2f;

       // Create shape for boundaries
       EdgeShape edgeShape = new EdgeShape();

       // Left wall
       Body leftWall = world.createBody(boundaryDef);
       edgeShape.set(
           new Vector2(0, 0),
           new Vector2(0, WORLD_HEIGHT / PPM)
       );
       fixtureDef.shape = edgeShape;
       leftWall.createFixture(fixtureDef);

       // Right wall
       Body rightWall = world.createBody(boundaryDef);
       edgeShape.set(
           new Vector2(WORLD_WIDTH / PPM, 0),
           new Vector2(WORLD_WIDTH / PPM, WORLD_HEIGHT / PPM)
       );
       fixtureDef.shape = edgeShape;
       rightWall.createFixture(fixtureDef);

       Body ceiling = world.createBody(boundaryDef);
       edgeShape.set(
           new Vector2(0, WORLD_HEIGHT / PPM),
           new Vector2(WORLD_WIDTH / PPM, WORLD_HEIGHT / PPM)
       );
       fixtureDef.shape = edgeShape;
       ceiling.createFixture(fixtureDef);


       edgeShape.dispose();
   }

   private void checkEndLevel() {
       boolean allPigsDestroyed = true;
       for (BasePig pig : pigs) {
           if (pig.getHealth() > 0) {
               allPigsDestroyed = false;
               break;
           }
       }

       if (allPigsDestroyed) {
           // Clean up resources
           world.dispose();
           stage.dispose();
           // Transition to win screen
           game.setScreen(new EndLevelWin(game));
       }
   }

   // Update checkGameState method
   private void checkGameState(float delta) {
       if (gameEnded) return;

       // Start final check timer when last bird is removed
       if (removedBirdsCount >= totalBirds && !finalCheckStarted) {
           finalCheckStarted = true;
           Gdx.app.log("Game Check", "Starting final check timer");
       }

       // Count down timer and check level status
       if (finalCheckStarted) {
           finalCheckTimer += delta;

           if (finalCheckTimer >= FINAL_CHECK_DELAY) {
               // Check for remaining pigs
               boolean pigAlive = false;
               for (BasePig pig : pigs) {
                   if (pig.getHealth() > 0) {
                       pigAlive = true;
                       break;
                   }
               }

               if (pigAlive) {
                   Gdx.app.log("Game Over", "Level Failed - Pigs remaining");
                   gameEnded = true;
                   world.dispose();
                   stage.dispose();
                   game.setScreen(new levelLose(game));
               }
               // Only check once
               finalCheckStarted = false;
           }
       }
   }



   // New loss check method
   private void checkLossCondition() {
       if (gameEnded) return;

       // Check if all birds have been removed
       if (removedBirdsCount >= totalBirds) {
           // Check remaining pigs
           boolean pigAlive = false;
           for (BasePig pig : pigs) {
               if (pig.getHealth() > 0) {
                   pigAlive = true;
                   break;
               }
           }

           if (pigAlive) {
               Gdx.app.log("Game Over", "Level Failed - Birds used: " + removedBirdsCount);
               gameEnded = true;
               world.dispose();
               stage.dispose();
               game.setScreen(new levelLose(game));
           }
       }
   }

   // Update checkAndRemoveBirds method
   private void checkAndRemoveBirds() {
       // First identify birds to remove
       for (BaseBird bird : birds) {
           if (!birdsToRemove.contains(bird)&&bird.getState() == BaseBird.BirdState.LAUNCHED && bird.getBody() != null) {
               Body birdBody = bird.getBody();
               float velocity = birdBody.getLinearVelocity().len();

               if (velocity < REMOVE_VELOCITY_THRESHOLD) {
                   birdsToRemove.add(bird);
               }
           }
       }

       for (BaseBird bird : birdsToRemove) {
           if (bird.getBody() != null) {
               world.destroyBody(bird.getBody());
               bird.setBody(null);
               bird.remove();
               removedBirdsCount++;
               Gdx.app.log("Bird Removed", removedBirdsCount + "/" + totalBirds);
           }
       }

       birdsToRemove.clear();

       // Check loss condition after removing birds
       checkLossCondition();
   }


   // Add save method
   public void saveGame() {
       try {
           GameState state = new GameState();
           state.setCurrentLevel(1);

           // Save pig states
           for (BasePig pig : pigs) {
               GameState.PigState pigState = new GameState.PigState();
               pigState.x = pig.getBody().getPosition().x;
               pigState.y = pig.getBody().getPosition().y;
               pigState.health = pig.getHealth();
               state.getPigStates().add(pigState);
           }

           // Save bird states
           for (BaseBird bird : birds) {
               GameState.BirdState birdState = new GameState.BirdState();
               birdState.x = bird.getBody().getPosition().x;
               birdState.y = bird.getBody().getPosition().y;
               birdState.isLaunched = bird.getState() == BaseBird.BirdState.LAUNCHED;
               birdState.birdType = bird.getClass().getSimpleName();
               state.getBirdStates().add(birdState);
           }

           // Save block states
           for (BaseBlock block : blocks) {
               GameState.BlockState blockState = new GameState.BlockState();
               blockState.x = block.getBody().getPosition().x;
               blockState.y = block.getBody().getPosition().y;
               blockState.health = block.getHealth();
               state.getBlockStates().add(blockState);
           }

           // Write to file
           FileOutputStream fos = new FileOutputStream(SAVE_FILE);
           ObjectOutputStream oos = new ObjectOutputStream(fos);
           oos.writeObject(state);
           oos.close();
           fos.close();

           Gdx.app.log("Save Game", "Game saved successfully");
       } catch (IOException e) {
           Gdx.app.error("Save Game", "Failed to save game", e);
       }
   }

   // Add load method
   public void loadGame() {
       try {
           FileInputStream fis = new FileInputStream(SAVE_FILE);
           ObjectInputStream ois = new ObjectInputStream(fis);
           GameState state = (GameState) ois.readObject();
           ois.close();
           fis.close();

           // Clear existing objects
           clearLevel();

           // Restore pigs
           for (GameState.PigState pigState : state.getPigStates()) {
               BasePig pig = new MechanicPig(atlas,
                   new Vector2(pigState.x * PPM, pigState.y * PPM), world);
               pig.setHealth(pigState.health);
               pigs.add(pig);
               stage.addActor(pig);
           }

           // Restore birds
           for (GameState.BirdState birdState : state.getBirdStates()) {
               BaseBird bird;
               switch (birdState.birdType) {
                   case "RedBird":
                       bird = new RedBird(atlas,
                           new Vector2(birdState.x * PPM, birdState.y * PPM), world);
                       break;
                   case "BombBird":
                       bird = new BombBird(atlas,
                           new Vector2(birdState.x * PPM, birdState.y * PPM), world);
                       break;
                   default:
                       continue;
               }
               if (birdState.isLaunched) {
                   bird.setState(BaseBird.BirdState.LAUNCHED);
               }
               birds.add(bird);
               stage.addActor(bird);
           }

           // Restore blocks
           for (GameState.BlockState blockState : state.getBlockStates()) {
               BaseBlock block = new GlassBlock(
                   new Vector2(blockState.x * PPM, blockState.y * PPM), world);
               block.setHealth((int)blockState.health);
               blocks.add(block);
               stage.addActor(block);
           }

           Gdx.app.log("Load Game", "Game loaded successfully");
       } catch (IOException | ClassNotFoundException e) {
           Gdx.app.error("Load Game", "Failed to load game", e);
       }
   }

   public Stage getStage() {
       return stage;
   }

   private void clearLevel() {
       // Clear existing objects
       for (BasePig pig : pigs) {
           if (pig.getBody() != null) {
               world.destroyBody(pig.getBody());
           }
           pig.remove();
       }
       pigs.clear();

       for (BaseBird bird : birds) {
           if (bird.getBody() != null) {
               world.destroyBody(bird.getBody());
           }
           bird.remove();
       }
       birds.clear();

       for (BaseBlock block : blocks) {
           if (block.getBody() != null) {
               world.destroyBody(block.getBody());
           }
           block.remove();
       }
       blocks.clear();
   }
   public void initializeTransientFields() {
       // Reinitialize physics world and contact listener
       world = new World(new Vector2(0, -9.8f), true);
       world.setContactListener(new MyContactListener());
       debugRenderer = new Box2DDebugRenderer();

       // Initialize rendering components
       camera = new OrthographicCamera();
       viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
       stage = new Stage(viewport);
       batch = new SpriteBatch();
       shapeRenderer = new ShapeRenderer();

       // Load assets
       loadAssets();

       // Re-add input processor
       Gdx.input.setInputProcessor(stage);

       // Recreate physics bodies for game objects
       recreatePhysicsBodies();
   }

    private void recreatePhysicsBodies() {
       for (BaseBird bird : birds) {
           bird.createPhysicsBody(world);
           bird.initializeTexture(atlas);
           stage.addActor(bird);
       }
       for (BasePig pig : pigs) {
           pig.definePigBody(world);
           // In BasePig.java
           pig.initializeTexture(atlas);
           stage.addActor(pig);
       }
       List<BaseBlock> newBlocks = new ArrayList<>();
       for (BaseBlock oldBlock : blocks) {
           // Extract necessary data from the old block
           Vector2 position = oldBlock.getPosition(); // Ensure this returns a copy of the position
           float health = oldBlock.getHealth();
           String blockType = oldBlock.getClass().getSimpleName();

           BaseBlock newBlock = null;

           // Create a new block instance based on the block type
           if (blockType.equals("GlassBlock")) {
               newBlock = new GlassBlock(position, world);
           }
           // Add more block types as needed

           if (newBlock != null) {
               newBlocks.add(newBlock);
               stage.addActor(newBlock);
           }
       }
       // Replace old blocks list with the new one
       blocks = newBlocks;
   }

   public void saveGame(String saveSlot) {
       try {
           GameState state = new GameState();
           state.setCurrentLevel(1);

           // Save bird states without textures
           for (BaseBird bird : birds) {
               if (bird != null && bird.getBody() != null) {
                   GameState.BirdState birdState = new GameState.BirdState();
                   birdState.x = bird.getBody().getPosition().x;
                   birdState.y = bird.getBody().getPosition().y;
                   birdState.isLaunched = bird.getState() == BaseBird.BirdState.LAUNCHED;
                   birdState.birdType = bird.getClass().getSimpleName();
                   state.getBirdStates().add(birdState);
               }
           }

           // Save pig states without textures
           for (BasePig pig : pigs) {
               if (pig != null && pig.getBody() != null) {
                   GameState.PigState pigState = new GameState.PigState();
                   pigState.x = pig.getBody().getPosition().x;
                   pigState.y = pig.getBody().getPosition().y;
                   pigState.health = pig.getHealth();
                   state.getPigStates().add(pigState);
               }
           }

           // Save block states without textures
           for (BaseBlock block : blocks) {
               if (block != null && block.getBody() != null) {
                   GameState.BlockState blockState = new GameState.BlockState();
                   blockState.x = block.getBody().getPosition().x;
                   blockState.y = block.getBody().getPosition().y;
                   blockState.health = block.getHealth();
                   blockState.blockType = block.getClass().getSimpleName();
                   state.getBlockStates().add(blockState);
               }
           }

           // Write state to file
           FileOutputStream fos = new FileOutputStream(Gdx.files.getLocalStoragePath() + saveSlot);
           ObjectOutputStream oos = new ObjectOutputStream(fos);
           oos.writeObject(state);
           oos.close();
           fos.close();

           Gdx.app.log("Save Game", "Game saved successfully to " + saveSlot);
       } catch (IOException e) {
           Gdx.app.error("Save Game", "Failed to save game", e);
       }
   }


   public static Level1 loadGame(AngryBirds game, String saveSlot) {
       try {
           FileInputStream fis = new FileInputStream(Gdx.files.getLocalStoragePath() + saveSlot);
           ObjectInputStream ois = new ObjectInputStream(fis);
           GameState state = (GameState) ois.readObject();
           ois.close();
           fis.close();

           Level1 loadedLevel = new Level1(game);
           loadedLevel.clearLevel();

           // Restore birds
           for (GameState.BirdState birdState : state.getBirdStates()) {
               BaseBird bird;
               if ("BombBird".equals(birdState.birdType)) {
                   bird = new BombBird(loadedLevel.atlas,
                       new Vector2(birdState.x * PPM, birdState.y * PPM),
                       loadedLevel.world);
               } else {
                   bird = new RedBird(loadedLevel.atlas,
                       new Vector2(birdState.x * PPM, birdState.y * PPM),
                       loadedLevel.world);
               }
               if (birdState.isLaunched) {
                   bird.setState(BaseBird.BirdState.LAUNCHED);
               }
               loadedLevel.birds.add(bird);
               loadedLevel.stage.addActor(bird);
           }

           // Restore pigs
           for (GameState.PigState pigState : state.getPigStates()) {
               BasePig pig = new MechanicPig(loadedLevel.atlas,
                   new Vector2(pigState.x * PPM, pigState.y * PPM),
                   loadedLevel.world);
               pig.setHealth(pigState.health);
               loadedLevel.pigs.add(pig);
               loadedLevel.stage.addActor(pig);
           }

           // Restore blocks
           for (GameState.BlockState blockState : state.getBlockStates()) {
               BaseBlock block = new GlassBlock(
                   new Vector2(blockState.x * PPM, blockState.y * PPM),
                   loadedLevel.world);
               block.setHealth(blockState.health);
               loadedLevel.blocks.add(block);
               loadedLevel.stage.addActor(block);
           }

           Gdx.app.log("Load Game", "Game loaded successfully from " + saveSlot);
           return loadedLevel;
       } catch (IOException | ClassNotFoundException e) {
           Gdx.app.error("Load Game", "Failed to load game", e);
           return null;
       }
   }

   private void setGame(AngryBirds game) {
       this.game = game;
   }

   public void saveGameToJson(String saveSlot) {
       try {
           Json json = new Json();
           JsonGameState state = new JsonGameState();

           // Save birds with complete state
           for (BaseBird bird : birds) {
               if (bird.getBody() != null) {
                   JsonBird jsonBird = new JsonBird();
                   jsonBird.x = bird.getBody().getPosition().x;
                   jsonBird.y = bird.getBody().getPosition().y;
                   jsonBird.type = bird.getClass().getSimpleName();
                   jsonBird.launched = bird.getState() == BaseBird.BirdState.LAUNCHED;
                   jsonBird.scale = bird.getScaleX(); // Save scale
                   jsonBird.width = bird.getWidth();
                   jsonBird.height = bird.getHeight();
                   state.birds.add(jsonBird);
               }
           }

           // Save pigs with complete state
           for (BasePig pig : pigs) {
               if (pig.getBody() != null) {
                   JsonPig jsonPig = new JsonPig();
                   jsonPig.x = pig.getBody().getPosition().x;
                   jsonPig.y = pig.getBody().getPosition().y;
                   jsonPig.health = pig.getHealth();
                   jsonPig.scale = pig.getScaleX();
                   jsonPig.width = pig.getWidth();
                   jsonPig.height = pig.getHeight();
                   state.pigs.add(jsonPig);
               }
           }

           // Save blocks with complete state
           for (BaseBlock block : blocks) {
               if (block.getBody() != null) {
                   JsonBlock jsonBlock = new JsonBlock();
                   jsonBlock.x = block.getBody().getPosition().x;
                   jsonBlock.y = block.getBody().getPosition().y;
                   jsonBlock.health = block.getHealth();
                   jsonBlock.type = block.getClass().getSimpleName();
                   jsonBlock.scale = block.getScaleX();
                   jsonBlock.width = block.getWidth();
                   jsonBlock.height = block.getHeight();
                   state.blocks.add(jsonBlock);
               }
           }

           String jsonString = json.prettyPrint(state);
           FileHandle file = Gdx.files.local(saveSlot);
           file.writeString(jsonString, false);

           Gdx.app.log("Save Game", "Game saved successfully to " + saveSlot);
       } catch (Exception e) {
           Gdx.app.error("Save Game", "Failed to save game", e);
       }
   }



public static Level1 loadGameFromJson(AngryBirds game, String saveSlot) {
   try {
       FileHandle file = Gdx.files.local(saveSlot);
       if (!file.exists()) {
           Gdx.app.error("Load Game", "Save file not found: " + saveSlot);
           return null;
       }

       Json json = new Json();
       JsonGameState state = json.fromJson(JsonGameState.class, file.readString());

       Level1 loadedLevel = new Level1(game);
       loadedLevel.clearLevel();
       loadedLevel.birdLaunched = new boolean[3];
       loadedLevel.birds.clear();
       loadedLevel.pigs.clear();
       loadedLevel.blocks.clear();

       // Restore birds first
       int birdIndex = 0;
       boolean foundUnlaunchedBird = false;
       loadedLevel.removedBirdsCount = 0; // Reset removed birds count

       // Count launched birds for game state tracking
       int launchedBirdsCount = 0;

       for (JsonBird jsonBird : state.birds) {
           BaseBird bird = null;
           Vector2 position;

           if (!jsonBird.launched && !foundUnlaunchedBird) {
               position = new Vector2(CATAPULT_HEAD_X, CATAPULT_HEAD_Y);
               foundUnlaunchedBird = true;
           } else if (!jsonBird.launched) {
               position = loadedLevel.birdWaitingPositions[birdIndex];
           } else {
               position = new Vector2(jsonBird.x * PPM, jsonBird.y * PPM);
               launchedBirdsCount++;
           }

           if ("BombBird".equals(jsonBird.type)) {
               bird = new BombBird(loadedLevel.atlas, position, loadedLevel.world);
               bird.setScale(BIRD_SCALE * 0.4f);
           } else {
               bird = new RedBird(loadedLevel.atlas, position, loadedLevel.world);
               bird.setScale(BIRD_SCALE);
           }

           // Set bird state and physics
           if (jsonBird.launched) {
               bird.setState(BaseBird.BirdState.LAUNCHED);
               loadedLevel.birdLaunched[birdIndex] = true;
               bird.getBody().setType(BodyDef.BodyType.DynamicBody);
           } else {
               bird.setState(BaseBird.BirdState.WAITING);
               if (!foundUnlaunchedBird) {
                   bird.setState(BaseBird.BirdState.ON_CATAPULT);
                   loadedLevel.birdOnCatapult = bird;
               }
               bird.makeStatic();
           }

           // Add click listener for unlaunched birds
           final int currentBirdIndex = birdIndex;
           if (!jsonBird.launched) {
               bird.addListener(new ClickListener() {
                   @Override
                   public void clicked(InputEvent event, float x, float y) {
                       if (!loadedLevel.birdLaunched[currentBirdIndex] && !loadedLevel.isBirdInFlight) {
                           loadedLevel.selectBird(currentBirdIndex);
                       }
                   }
               });
           }

           loadedLevel.birds.add(bird);
           loadedLevel.stage.addActor(bird);
           birdIndex++;
       }

       // Update removed birds count
       loadedLevel.removedBirdsCount = launchedBirdsCount;
       loadedLevel.totalBirds = loadedLevel.birds.size();

       // Restore pigs with complete state
       for (JsonPig jsonPig : state.pigs) {
           BasePig pig = new MechanicPig(loadedLevel.atlas,
               new Vector2(jsonPig.x * PPM+20.0f, jsonPig.y * PPM+20.f),
               loadedLevel.world);
           pig.setHealth(jsonPig.health);
           pig.setScale(PIG_SCALE);
           pig.setSize(jsonPig.width, jsonPig.height);
           loadedLevel.pigs.add(pig);
           loadedLevel.stage.addActor(pig);
           loadedLevel.stage.addActor(pig.getHealthLabel());
       }

       // Check if all pigs are destroyed
       boolean allPigsDestroyed = true;
       for (BasePig pig : loadedLevel.pigs) {
           if (pig.getHealth() > 0) {
               allPigsDestroyed = false;
               break;
           }
       }

       // If all pigs are destroyed, show win screen
       if (allPigsDestroyed) {
           loadedLevel.gameEnded = true;
           game.setScreen(new EndLevelWin(game));
           return null;
       }

       // If all birds are used and pigs remain, show lose screen
       if (loadedLevel.removedBirdsCount >= loadedLevel.totalBirds) {
           boolean pigAlive = false;
           for (BasePig pig : loadedLevel.pigs) {
               if (pig.getHealth() > 0) {
                   pigAlive = true;
                   break;
               }
           }
           if (pigAlive) {
               loadedLevel.gameEnded = true;
               game.setScreen(new levelLose(game));
               return null;
           }
       }

       // Restore blocks with complete state
       for (JsonBlock jsonBlock : state.blocks) {
           BaseBlock block = new GlassBlock(
               new Vector2(jsonBlock.x * PPM, jsonBlock.y * PPM),
               loadedLevel.world);
           block.setHealth(jsonBlock.health);
           block.setScale(BLOCK_SCALE);
           block.setSize(jsonBlock.width, jsonBlock.height);
           loadedLevel.blocks.add(block);
           loadedLevel.stage.addActor(block);
       }

       // Re-create physics world boundaries
       loadedLevel.createBoundaries();
       loadedLevel.createGround();

       Gdx.app.log("Load Game", "Game loaded successfully from " + saveSlot);
       loadedLevel.reinitializeStage();
       loadedLevel.stage.act();
       return loadedLevel;
   } catch (Exception e) {
       Gdx.app.error("Load Game", "Failed to load game", e);
       e.printStackTrace();
       return null;
   }
}

   // JSON state classes
   private static class JsonGameState {
       public ArrayList<JsonBird> birds = new ArrayList<>();
       public ArrayList<JsonPig> pigs = new ArrayList<>();
       public ArrayList<JsonBlock> blocks = new ArrayList<>();
   }

   private static class JsonBird {
       public float x, y;
       public String type;
       public boolean launched;
       public float scale;
       public float width;
       public float height;
   }

   private static class JsonPig {
       public float x, y;
       public float health;
       public float scale;
       public float width;
       public float height;
   }

   private static class JsonBlock {
       public float x, y;
       public float health;
       public String type;
       public float scale;
       public float width;
       public float height;
   }

   public void setPaused(boolean paused) {
       this.isPaused = paused;
       if (!paused) {
           // Reinitialize input handling when unpausing
           Gdx.input.setInputProcessor(stage);
           // Ensure stage is active
           stage.act();
       }
   }

   public void reinitializeStage() {
       // Reattach input listeners to birds
       for (int i = 0; i < birds.size(); i++) {
           final int birdIndex = i;
           BaseBird bird = birds.get(i);
           if (!birdLaunched[i]) {
               bird.clearListeners(); // Clear existing listeners
               bird.addListener(new ClickListener() {
                   @Override
                   public void clicked(InputEvent event, float x, float y) {
                       if (!birdLaunched[birdIndex] && !isBirdInFlight) {
                           selectBird(birdIndex);
                       }
                   }
               });
           }
       }
       
       // Ensure UI elements are properly initialized
       createUI();
       
       // Set input processor
       Gdx.input.setInputProcessor(stage);
   }

   @Override
   public void render(float delta) {
       Gdx.gl.glClearColor(1, 1, 1, 1);
       Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

       // Update camera matrices
       camera.update();
       batch.setProjectionMatrix(camera.combined);

       // Draw background to fill viewport
       batch.begin();
       batch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
       float catapultWidth = 100 * CATAPULT_SCALE;
       float catapultHeight = 100 * CATAPULT_SCALE;
       batch.draw(catapultTexture,
           CATAPULT_X - catapultWidth/2,  // Center horizontally
           CATAPULT_Y - catapultHeight/2,  // Center vertically
           catapultWidth,
           catapultHeight
       );
       batch.end();
       // Only step physics if not paused
       if (!isPaused) {
           // Step the physics simulation with the frame's delta time
           float timeStep = 1/60f;
           int velocityIterations = 8;
           int positionIterations = 3;
           world.step(timeStep, velocityIterations, positionIterations);

           for (BaseBird bird : birds) {
            if (bird instanceof BombBird) {
                BombBird bombBird = (BombBird) bird;
                if (bombBird.updateExplosion(delta)) {
                    birdsToRemove.add(bird);
                }
            }
        }

           checkAndRemoveBirds();

           for (BasePig pig : pigs) {
               if (pig.isMarkedForDestruction() && pig.getBody() != null) {
                   world.destroyBody(pig.getBody());
                   pig.remove();
                   pig.setBody(null);
               }
           }
           // Remove destroyed pigs from list
           for (Iterator<BasePig> iterator = pigs.iterator(); iterator.hasNext(); ) {
               BasePig pig = iterator.next();
               if (pig.getBody() == null) {
                   iterator.remove();
               }
           }

           // Update stage with viewport
           stage.getViewport().apply();
           stage.act(delta);
           stage.draw();
           checkEndLevel();

           if (!gameEnded) {
               checkGameState(delta);
           }

           if (!gameEnded) {
               checkLossCondition();
           }
       }
       stage.draw();
   }

   @Override
   public void resize(int width, int height) {
       viewport.update(width, height, true);
       camera.position.set(WORLD_WIDTH/2, WORLD_HEIGHT/2, 0);
       camera.update();
   }

   @Override
   public void pause() {}

   @Override
   public void resume() {}

   @Override
   public void show() {}

   @Override
   public void hide() {}

   @Override
   public void dispose() {
       world.dispose();
       debugRenderer.dispose();
       stage.dispose();
       batch.dispose();
       backgroundTexture.dispose();
       catapultTexture.dispose();
       atlas.dispose();
       // Add this line
       ((TextureRegionDrawable)pauseButtonStyle.up).getRegion().getTexture().dispose();
   }
}
