// package io.github.AngryBird_2023124_2023371.Screens;
// import com.badlogic.gdx.Gdx;
// import com.badlogic.gdx.Screen;
// import com.badlogic.gdx.graphics.GL20;
// import com.badlogic.gdx.graphics.OrthographicCamera;
// import com.badlogic.gdx.graphics.Texture;
// import com.badlogic.gdx.graphics.g2d.SpriteBatch;
// import com.badlogic.gdx.graphics.g2d.TextureAtlas;
// import com.badlogic.gdx.graphics.g2d.TextureRegion;
// import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
// import com.badlogic.gdx.math.Vector2;
// import com.badlogic.gdx.physics.box2d.*;
// import com.badlogic.gdx.scenes.scene2d.Actor;
// import com.badlogic.gdx.scenes.scene2d.InputEvent;
// import com.badlogic.gdx.scenes.scene2d.Stage;
// import com.badlogic.gdx.scenes.scene2d.ui.*;
// import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
// import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
// import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
// import com.badlogic.gdx.utils.Align;
// import com.badlogic.gdx.utils.Array;
// import com.badlogic.gdx.utils.viewport.FitViewport;
// import com.badlogic.gdx.utils.viewport.StretchViewport;
// import io.github.AngryBird_2023124_2023371.Screens.*;
// import io.github.AngryBird_2023124_2023371.AngryBirds;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Iterator;

// public class Level3 implements Screen {
//     // Game and rendering
//     private AngryBirds game;
//     private OrthographicCamera camera;
//     private SpriteBatch batch;
//     private Stage stage;

//     // Physics
//     private World world;
//     private Box2DDebugRenderer debugRenderer;

//     // Assets
//     private Texture backgroundTexture;
//     private Texture catapultTexture;
//     private TextureAtlas atlas;
//     private List<Vector2> trajectoryPoints; // Stores the predicted trajectory points


//     // Game objects
//     private Vector2 catapultPosition;
//     private Vector2[] birdWaitingPositions;
//     private boolean[] birdUsed;
//     private List<BaseBird> birds;
//     private List<BasePig> pigs;
//     private List<BaseBlock> blocks;
//     private Array<Body> bodiesToDestroy;
//     private ShapeRenderer shapeRenderer;

//     // Game state
//     private int currentBirdIndex = 0;
//     private boolean isBirdInFlight = false;
//     private BaseBird currentBird = null;
//     private BaseBird birdOnCatapult = null;
//     private boolean[] birdLaunched; // Track which birds have been launched

//     // UI
//     private Slider powerSlider;
//     private Slider angleSlider;
//     private Label powerLabel;
//     private Label angleLabel;
//     private TextButton launchButton;

//     // Constants
//     private static final float PIG_SCALE = 0.07f;
//     private static final float PPM = 100f; // Pixels per meter for Box2D
//     private static final float WORLD_WIDTH = 800f;
//     private static final float WORLD_HEIGHT = 480f;
//     private static final float BIRD_SCALE = 0.15f;
//     private static final float BLOCK_SCALE = 0.1f; // Adjust this value (0.1 = 10% size)
//     private StretchViewport viewport;
//     private static final float CATAPULT_X = 100f;
//     private static final float CATAPULT_Y = 100f;
//     private static final float CATAPULT_SCALE = 0.8f; // 80% of original size
//     private static final float CATAPULT_HEAD_X = CATAPULT_X + 20f; // Adjust based on catapult sprite
//     private static final float CATAPULT_HEAD_Y = CATAPULT_Y + 60f; // Height for bird on catapult
//     private static final float GROUND_Y = 100f; // Adjust based on ground height
//     private static final float BIRD_SPACING = 50f; // Space between waiting birds
//     private static final float MAX_LAUNCH_POWER = 20f; // Adjust based on testing
//     private static final float LAUNCH_POWER_MULTIPLIER = 0.5f;
//     private static final float MAX_LAUNCH_SPEED = 10f; // Adjust this value as needed
//     private static final float SPEED_MULTIPLIER = 0.5f; // Reduces the overall speed
//     private static final float VELOCITY_THRESHOLD = 0.1f;
//     private static final float LOSE_CHECK_DELAY = 2.0f;
//     private static final float GROUND_CHECK_OFFSET = 20f;
//     private static final float REMOVE_VELOCITY_THRESHOLD = 0.05f;
//     private static final float REMOVE_HEIGHT_THRESHOLD = GROUND_Y + 20f;


//     private boolean gameEnded = false;


//     private List<BaseBird> birdsToRemove = new ArrayList<>();


//     private int totalBirds;
//     private int removedBirdsCount = 0;


//     private static final float FINAL_CHECK_DELAY = 9.0f;
//     private float finalCheckTimer = 0;
//     private boolean finalCheckStarted = false;

//     public Level3(AngryBirds game) {
//         this.game = game;

//         // Initialize camera and viewport
//         bodiesToDestroy = new Array<>();
//         camera = new OrthographicCamera();
//         viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

//         // Use viewport for stage
//         stage = new Stage(viewport);
//         batch = new SpriteBatch();
//         Gdx.input.setInputProcessor(stage);

//         // Center camera
//         camera.position.set(WORLD_WIDTH/2, WORLD_HEIGHT/2, 0);
//         camera.update();


//         // Rest of constructor remains same
//         world = new World(new Vector2(0, -9.8f), true); // Standard gravity (was -19.6f)
//         world.setContactListener(new MyContactListener());
//         createBoundaries(); // Add this line
//         debugRenderer = new Box2DDebugRenderer();

//         loadAssets();

//         catapultPosition = new Vector2(CATAPULT_X, CATAPULT_Y + 20f); // Slightly above catapult
//         initializeBirdPositions();


//         setupGameObjects();
//         createUI();
//         createGround();
//         birdLaunched = new boolean[3];
//         Arrays.fill(birdLaunched, false);
//         totalBirds = birds.size(); // Set after birds are created

//     }


//     private void initializeBirdPositions() {
//         // Initialize waiting positions for birds on ground
//         birdWaitingPositions = new Vector2[3];
//         birdWaitingPositions[0] = new Vector2(CATAPULT_X - 130f, GROUND_Y);
//         birdWaitingPositions[1] = new Vector2(CATAPULT_X - 180f, GROUND_Y);
//         birdWaitingPositions[2] = new Vector2(CATAPULT_X - 230f, GROUND_Y);

//         birdUsed = new boolean[3];
//         Arrays.fill(birdUsed, false);
//     }



//     private void loadAssets() {
//         backgroundTexture = new Texture(Gdx.files.internal("level1back.jpg"));
//         catapultTexture = new Texture(Gdx.files.internal("assetsforatlas/catapult_1.png"));
//         atlas = new TextureAtlas(Gdx.files.internal("character.atlas"));
//     }

//     private void setupGameObjects() {
//         birds = new ArrayList<>();
//         pigs = new ArrayList<>();
//         blocks = new ArrayList<>();

//         for (int i = 0; i < 3; i++) {
//             final int birdIndex = i;
//             BaseBird bird = null;

//             if(i == 1 || i == 2) {
//                 bird = new BombBird(atlas, birdWaitingPositions[i], world);
//                 bird.setScale(BIRD_SCALE * 0.5f);
//             } else {
//                 bird = new RedBird(atlas, birdWaitingPositions[i], world);
//                 bird.setScale(BIRD_SCALE );
//             }



//             // Set initial physics properties
//             Body body = bird.getBody();
//             if (body != null) {
//                 body.setType(BodyDef.BodyType.DynamicBody); // Make it affected by gravity
//                 body.setGravityScale(1); // Normal gravity for ground birds
//                 body.setFixedRotation(true); // Prevent rotation
//             }

//             bird.addListener(new ClickListener() {
//                 @Override
//                 public void clicked(InputEvent event, float x, float y) {
//                     if (!birdUsed[birdIndex] && !isBirdInFlight) {
//                         selectBird(birdIndex);
//                     }
//                 }
//             });

//             birds.add(bird);
//             stage.addActor(bird);
//         }
//         currentBirdIndex = 0;
//         birds.get(currentBirdIndex).setPosition(
//             catapultPosition.x - birds.get(currentBirdIndex).getWidth()/2,
//             catapultPosition.y - birds.get(currentBirdIndex).getHeight()/2
//         );

//         // Left tower (6 blocks high)
//         float leftTowerX = WORLD_WIDTH * 0.6f;
//         blocks.addAll(Arrays.asList(
//             new IronBlock(new Vector2(leftTowerX, 80f), world),
//             new IronBlock(new Vector2(leftTowerX, 130f), world),
//             new IronBlock(new Vector2(leftTowerX, 180f), world),
//             new IronBlock(new Vector2(leftTowerX, 230f), world),
//             new IronBlock(new Vector2(leftTowerX, 280f), world),
//             new IronBlock(new Vector2(leftTowerX, 330f), world)
//         ));

//         // Add a pig on top of the left tower
//         BasePig leftPig = new AngryPig(world,atlas, new Vector2(leftTowerX+20f, 380f));
//         leftPig.setScale(PIG_SCALE);
//         pigs.add(leftPig);
//         stage.addActor(leftPig);
//         stage.addActor(leftPig.getHealthLabel());

//         // Middle tower (3 blocks high)
//         float middleTowerX = WORLD_WIDTH * 0.75f;
//         blocks.addAll(Arrays.asList(
//             new IronBlock(new Vector2(middleTowerX, 80f), world),
//             new IronBlock(new Vector2(middleTowerX, 130f), world),
//             new IronBlock(new Vector2(middleTowerX, 180f), world)
//         ));

//         // Add a pig on top of the middle tower
//         BasePig middlePig = new AngryPig(world,atlas, new Vector2(middleTowerX+20f, 230f));
//         middlePig.setScale(PIG_SCALE);
//         pigs.add(middlePig);
//         stage.addActor(middlePig);
//         stage.addActor(middlePig.getHealthLabel());

//         // Right tower (6 blocks high)
//         float rightTowerX = WORLD_WIDTH * 0.9f;
//         blocks.addAll(Arrays.asList(
//             new IronBlock(new Vector2(rightTowerX, 80f), world),
//             new IronBlock(new Vector2(rightTowerX, 130f), world),
//             new IronBlock(new Vector2(rightTowerX, 180f), world),
//             new IronBlock(new Vector2(rightTowerX, 230f), world),
//             new IronBlock(new Vector2(rightTowerX, 280f), world),
//             new IronBlock(new Vector2(rightTowerX, 330f), world)
//         ));

//         // Add a pig on top of the right tower
//         BasePig rightPig = new AngryPig(world,atlas, new Vector2(rightTowerX+20f, 380f));
//         rightPig.setScale(PIG_SCALE);
//         pigs.add(rightPig);
//         stage.addActor(rightPig);
//         stage.addActor(rightPig.getHealthLabel());
//         // Add blocks to the stage in order (bottom to top)
//         for (BaseBlock block : blocks) {
//             block.setScale(BLOCK_SCALE);
//             stage.addActor(block);
//         }

//         createGround();
//     }

//     private void selectBird(int birdIndex) {
//         // Check if bird exists and hasn't been launched
//         if (birdIndex >= 0 && birdIndex < birds.size() && !birdLaunched[birdIndex] && !isBirdInFlight) {
//             BaseBird selectedBird = birds.get(birdIndex);

//             // Store the clicked bird's original position
//             Vector2 oldPosition = new Vector2(selectedBird.getX(), selectedBird.getY());

//             // If there's a bird on catapult, return it to ground
//             if (birdOnCatapult != null) {
//                 int oldBirdIndex = birds.indexOf(birdOnCatapult);
//                 if (oldBirdIndex != -1 && !birdLaunched[oldBirdIndex]) {
//                     birdOnCatapult.returnToGround(oldPosition.x, oldPosition.y);
//                     birdOnCatapult.makeStatic();
//                     birdOnCatapult.getBody().setGravityScale(1);
//                 }
//             }

//             // Move selected bird to catapult
//             selectedBird.placeOnCatapult(CATAPULT_HEAD_X, CATAPULT_HEAD_Y);
//             selectedBird.makeStatic();
//             birdOnCatapult = selectedBird;
//             currentBirdIndex = birdIndex;
//         }
//     }

//     private void launchCurrentBird() {
//         if (birdOnCatapult != null && birdOnCatapult.getState() == BaseBird.BirdState.ON_CATAPULT && !gameEnded) {
//             // Get values from sliders
//             float power = powerSlider.getValue() * LAUNCH_POWER_MULTIPLIER;
//             float angle = angleSlider.getValue();
//             // Convert angle to radians
//             float angleRad = (float) Math.toRadians(angle);

//             // Calculate launch vector
//             float forceX = power * (float) Math.cos(angleRad);
//             float forceY = power * (float) Math.sin(angleRad);

//             // Prepare bird for launch
//             Body birdBody = birdOnCatapult.getBody();
//             birdBody.setType(BodyDef.BodyType.DynamicBody);
//             birdBody.setGravityScale(1);
//             birdBody.setAwake(true);
//             birdBody.setFixedRotation(false);

//             // Apply launch force
//             Vector2 force = new Vector2(forceX, forceY);
//             Vector2 position = birdBody.getWorldCenter();
//             birdBody.applyLinearImpulse(force, position, true);

//             // Update bird state and list
//             birdOnCatapult.setState(BaseBird.BirdState.LAUNCHED);
//             birdUsed[currentBirdIndex] = true;

//             // Reset state variables
//             birdOnCatapult = null;

//             Gdx.app.log("Bird Launch", "Bird " + currentBirdIndex + " launched");

//         }
//         else{
//             System.out.println( "All Birds have been launched!!! Now displaying the results screen from the level wheather the birds won or lost!!");
//         }
//     }



//     private void createUI() {
//         Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

//         // Power Slider
//         powerLabel = new Label("Power", skin);
//         powerLabel.setPosition(180-130f, 370-50f);
//         powerLabel.setFontScale(0.7f);
//         stage.addActor(powerLabel);

//         powerSlider = new Slider(0, 100, 1, false, skin);
//         powerSlider.setPosition(245-130f, 380-50f);
//         powerSlider.setSize(120, 10);
//         stage.addActor(powerSlider);

//         // Angle Slider
//         angleLabel = new Label("Angle", skin);
//         angleLabel.setPosition(180-130f, 410-50f);
//         angleLabel.setFontScale(0.7f);
//         stage.addActor(angleLabel);

//         angleSlider = new Slider(0, 90, 1, false, skin);
//         angleSlider.setPosition(245-130f, 420-50f);
//         angleSlider.setSize(120, 10);
//         stage.addActor(angleSlider);

//         // Launch Button
//         launchButton = new TextButton("Launch", skin);
//         launchButton.setPosition(120+10f, 275+5f);
//         launchButton.setSize(70, 30);
//         launchButton.getLabel().setAlignment(Align.center);
//         launchButton.getLabelCell().pad(5);
//         launchButton.getLabel().setFontScale(0.5f);

//         powerSlider.addListener(new ChangeListener() {
//             @Override
//             public void changed(ChangeEvent event, Actor actor) {
//                 updateTrajectory();
//             }
//         });

//         angleSlider.addListener(new ChangeListener() {
//             @Override
//             public void changed(ChangeEvent event, Actor actor) {
//                 updateTrajectory();
//             }
//         });

//         launchButton.addListener(new ClickListener() {
//             @Override
//             public void clicked(InputEvent event, float x, float y) {
//                 if (!isBirdInFlight && birdOnCatapult != null) {
//                     launchCurrentBird();
//                 }
//             }
//         });

//         stage.addActor(launchButton);
//         Texture pauseTexture = new Texture(Gdx.files.internal("pausebtn.png"));
//         ImageButton.ImageButtonStyle pauseButtonStyle = new ImageButton.ImageButtonStyle();
//         pauseButtonStyle.up = new TextureRegionDrawable(new TextureRegion(pauseTexture));

// // Create the ImageButton with the style
//         ImageButton pauseButton = new ImageButton(pauseButtonStyle);
//         pauseButton.setPosition(700, 330); // Adjust position as needed
//         pauseButton.setSize(200, 200); // Adjust size as needed

// // Add a listener to handle the click event
//         pauseButton.addListener(new ClickListener() {
//             @Override
//             public void clicked(InputEvent event, float x, float y) {
//                 // Switch to the PauseMenuScreen
//                 game.setScreen(new PauseScreenLevel3(game));
//             }
//         });

// // Add the pause button to the stage
//         stage.addActor(pauseButton);
//     }

//     private void updateTrajectory() {
//         if (birdOnCatapult != null) {
//             trajectoryPoints = new ArrayList<>();

//             float power = powerSlider.getValue() * LAUNCH_POWER_MULTIPLIER;
//             float angle = angleSlider.getValue();
//             float angleRad = (float) Math.toRadians(angle);

//             // Initial velocity components
//             float velocityX = power * (float) Math.cos(angleRad);
//             float velocityY = power * (float) Math.sin(angleRad);

//             // Starting position (catapult head position)
//             float startX = birdOnCatapult.getBody().getPosition().x * PPM;
//             float startY = birdOnCatapult.getBody().getPosition().y * PPM;

//             // Time-step for calculating trajectory points
//             float timeStep = 0.1f; // Adjust for smoothness

//             for (float t = 0; t < 5; t += timeStep) {
//                 // Calculate position at time t
//                 float x = startX + velocityX * t;
//                 float y = startY + velocityY * t - 0.5f * Math.abs(world.getGravity().y) * t * t * PPM;

//                 // Break if the point goes below ground level
//                 if (y < GROUND_Y) break;

//                 trajectoryPoints.add(new Vector2(x, y));
//             }
//         } else {
//             trajectoryPoints = null; // No trajectory to display
//         }
//     }

//     private void createGround() {
//         BodyDef groundBodyDef = new BodyDef();
//         groundBodyDef.position.set(WORLD_WIDTH / 2 / PPM, 32 / PPM);
//         groundBodyDef.type = BodyDef.BodyType.StaticBody;

//         Body groundBody = world.createBody(groundBodyDef);

//         PolygonShape groundShape = new PolygonShape();
//         groundShape.setAsBox(WORLD_WIDTH / 2 / PPM, 32 / PPM);

//         FixtureDef fixtureDef = new FixtureDef();
//         fixtureDef.shape = groundShape;
//         fixtureDef.density = 1.0f;
//         fixtureDef.friction = 0.4f;
//         fixtureDef.restitution = 0.2f;

//         groundBody.createFixture(fixtureDef);
//         groundShape.dispose();
//     }

//     private void createBoundaries() {
//         // Create body definition for static boundaries
//         BodyDef boundaryDef = new BodyDef();
//         boundaryDef.type = BodyDef.BodyType.StaticBody;

//         // Create fixture definition
//         FixtureDef fixtureDef = new FixtureDef();
//         fixtureDef.friction = 0.3f;
//         fixtureDef.restitution = 0.2f;

//         // Create shape for boundaries
//         EdgeShape edgeShape = new EdgeShape();

//         // Left wall
//         Body leftWall = world.createBody(boundaryDef);
//         edgeShape.set(
//             new Vector2(0, 0),
//             new Vector2(0, WORLD_HEIGHT / PPM)
//         );
//         fixtureDef.shape = edgeShape;
//         leftWall.createFixture(fixtureDef);

//         // Right wall
//         Body rightWall = world.createBody(boundaryDef);
//         edgeShape.set(
//             new Vector2(WORLD_WIDTH / PPM, 0),
//             new Vector2(WORLD_WIDTH / PPM, WORLD_HEIGHT / PPM)
//         );
//         fixtureDef.shape = edgeShape;
//         rightWall.createFixture(fixtureDef);

//         Body ceiling = world.createBody(boundaryDef);
//         edgeShape.set(
//             new Vector2(0, WORLD_HEIGHT / PPM),
//             new Vector2(WORLD_WIDTH / PPM, WORLD_HEIGHT / PPM)
//         );
//         fixtureDef.shape = edgeShape;
//         ceiling.createFixture(fixtureDef);


//         edgeShape.dispose();
//     }

//     private void checkEndLevel() {
//         boolean allPigsDestroyed = true;
//         for (BasePig pig : pigs) {
//             if (pig.getHealth() > 0) {
//                 allPigsDestroyed = false;
//                 break;
//             }
//         }

//         if (allPigsDestroyed) {
//             // Clean up resources
//             world.dispose();
//             stage.dispose();
//             // Transition to win screen
//             game.setScreen(new EndLevelWin(game));
//         }
//     }

//     // Update checkGameState method
//     private void checkGameState(float delta) {
//         if (gameEnded) return;

//         // Start final check timer when last bird is removed
//         if (removedBirdsCount >= totalBirds && !finalCheckStarted) {
//             finalCheckStarted = true;
//             Gdx.app.log("Game Check", "Starting final check timer");
//         }

//         // Count down timer and check level status
//         if (finalCheckStarted) {
//             finalCheckTimer += delta;

//             if (finalCheckTimer >= FINAL_CHECK_DELAY) {
//                 // Check for remaining pigs
//                 boolean pigAlive = false;
//                 for (BasePig pig : pigs) {
//                     if (pig.getHealth() > 0) {
//                         pigAlive = true;
//                         break;
//                     }
//                 }

//                 if (pigAlive) {
//                     Gdx.app.log("Game Over", "Level Failed - Pigs remaining");
//                     gameEnded = true;
//                     world.dispose();
//                     stage.dispose();
//                     game.setScreen(new levelLose(game));
//                 }
//                 // Only check once
//                 finalCheckStarted = false;
//             }
//         }
//     }



//     // New loss check method
//     private void checkLossCondition() {
//         if (gameEnded) return;

//         // Check if all birds have been removed
//         if (removedBirdsCount >= totalBirds) {
//             // Check remaining pigs
//             boolean pigAlive = false;
//             for (BasePig pig : pigs) {
//                 if (pig.getHealth() > 0) {
//                     pigAlive = true;
//                     break;
//                 }
//             }

//             if (pigAlive) {
//                 Gdx.app.log("Game Over", "Level Failed - Birds used: " + removedBirdsCount);
//                 gameEnded = true;
//                 world.dispose();
//                 stage.dispose();
//                 game.setScreen(new levelLose(game));
//             }
//         }
//     }

//     // Update checkAndRemoveBirds method
//     private void checkAndRemoveBirds() {
//         // First identify birds to remove
//         for (BaseBird bird : birds) {
//             if (bird.getState() == BaseBird.BirdState.LAUNCHED && bird.getBody() != null) {
//                 Body birdBody = bird.getBody();
//                 float velocity = birdBody.getLinearVelocity().len();

//                 if (velocity < REMOVE_VELOCITY_THRESHOLD) {
//                     birdsToRemove.add(bird);
//                 }
//             }
//         }

//         // Then safely remove them
//         for (BaseBird bird : birdsToRemove) {
//             if (bird.getBody() != null) {
//                 world.destroyBody(bird.getBody());
//                 bird.setBody(null);
//                 bird.remove();
//                 removedBirdsCount++;
//                 Gdx.app.log("Bird Removed", removedBirdsCount + "/" + totalBirds);
//             }
//         }

//         birdsToRemove.clear();

//         // Check loss condition after removing birds
//         checkLossCondition();
//     }

//     @Override
//     public void render(float delta) {

//         Gdx.gl.glClearColor(1, 1, 1, 1);
//         Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//         // Update camera matrices
//         camera.update();
//         batch.setProjectionMatrix(camera.combined);

//         // Draw background to fill viewport
//         batch.begin();
//         batch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
//         float catapultWidth = 100 * CATAPULT_SCALE;
//         float catapultHeight = 100 * CATAPULT_SCALE;
//         batch.draw(catapultTexture,
//             CATAPULT_X - catapultWidth/2,  // Center horizontally
//             CATAPULT_Y - catapultHeight/2,  // Center vertically
//             catapultWidth,
//             catapultHeight
//         );
//         batch.end();
//         // Step the physics simulation with the frame's delta time
//         float timeStep = 1/60f;
//         int velocityIterations = 8;
//         int positionIterations = 3;
//         world.step(timeStep, velocityIterations, positionIterations);

//         // Clean up physics only, keep birds in main list
//         checkAndRemoveBirds();

//         for (BasePig pig : pigs) {
//             if (pig.isMarkedForDestruction() && pig.getBody() != null) {
//                 world.destroyBody(pig.getBody());
//                 pig.remove();
//                 pig.setBody(null);
//             }
//         }
//         // Remove destroyed pigs from list
//         for (Iterator<BasePig> iterator = pigs.iterator(); iterator.hasNext(); ) {
//             BasePig pig = iterator.next();
//             if (pig.getBody() == null) {
//                 iterator.remove();
//             }
//         }

//         // Update stage with viewport
//         stage.getViewport().apply();
//         stage.act(delta);
//         stage.draw();
//         checkEndLevel();

//         if (!gameEnded) {
//             checkGameState(delta);
//         }

//         if (!gameEnded) {
//             checkLossCondition();
//         }
//     }

//     @Override
//     public void resize(int width, int height) {
//         viewport.update(width, height, true);
//         camera.position.set(WORLD_WIDTH/2, WORLD_HEIGHT/2, 0);
//         camera.update();
//     }

//     @Override
//     public void pause() {}

//     @Override
//     public void resume() {}

//     @Override
//     public void show() {}

//     @Override
//     public void hide() {}

//     @Override
//     public void dispose() {
//         world.dispose();
//         debugRenderer.dispose();
//         stage.dispose();
//         batch.dispose();
//         backgroundTexture.dispose();
//         catapultTexture.dispose();
//         atlas.dispose();
//     }
// }
