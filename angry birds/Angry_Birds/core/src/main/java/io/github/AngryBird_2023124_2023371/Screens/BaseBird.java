package io.github.AngryBird_2023124_2023371.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import java.io.Serializable;

// First add BirdState enum


public abstract class BaseBird extends Actor implements Serializable {

    public enum BirdState {
        WAITING,
        ON_CATAPULT,
        LAUNCHED
    }
    private static final long serialVersionUID = 1L;
    protected Vector2 position;
    protected TextureRegion birdTexture;
    protected transient Body body;
    protected BirdState state;
    protected static final float PPM = GamePhysicsConstants.PPM;
    protected static final float PHYSICS_RADIUS_SCALE = 0.4f;
    private static final float MAX_VELOCITY = 10f; // Was 20f
    private static final float SPEED_MULTIPLIER = 0.3f; // Was 0.5f
    private int health = 100;

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setState(BirdState state) {
        this.state = state;
    }

    public Body getBody() {
        return body;
    }

    // Constructor where position is explicitly passed as Vector2
    public BaseBird(TextureRegion texture, Vector2 position, World world) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }

        this.birdTexture = texture;
        this.position = position; // Correctly set the position here
        this.state = BirdState.WAITING;

        if (this.birdTexture == null) {
            System.out.println("Error: birdTexture is null. Using a fallback texture.");
        }

        // Set the size based on texture dimensions before creating the physics body
        setSize(birdTexture.getRegionWidth(), birdTexture.getRegionHeight());

        // Log for texture validation
        Gdx.app.log("BaseBird", "Texture loaded with width: " + birdTexture.getRegionWidth() + ", height: " + birdTexture.getRegionHeight());

        // Create the physics body after setting the position
        createPhysicsBody(world);
        makeStatic(); // Start as static
    }

    // In BaseBird.java, update createPhysicsBody method
    protected void createPhysicsBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
            (position.x + getWidth() / 2) / PPM,
            (position.y + getHeight() / 2) / PPM
        );

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        float radius = (Math.min(getWidth(), getHeight()) * PHYSICS_RADIUS_SCALE) / 4;
        shape.setRadius(radius / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 3.0f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.4f;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this); // Set fixture's user data to this bird instance
        shape.dispose();
    }


    public void setBody(Body body) {
        this.body = body;
    }

    public void makeStatic() {
        if (body != null) {
            body.setType(BodyDef.BodyType.StaticBody);
            body.setGravityScale(0);
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            body.setFixedRotation(true);
            body.setAwake(false);
        }
    }

    public void makeDynamic() {
        if (body != null) {
            body.setType(BodyDef.BodyType.DynamicBody);
            body.setGravityScale(1);
            body.setFixedRotation(false);
            body.setAwake(true);
        }
    }

    public void moveTo(float x, float y) {
        setPosition(x, y);
        if (body != null) {
            body.setTransform(x / PPM, y / PPM, 0);
            makeStatic();
        }
    }
        public void initializeTexture(TextureAtlas atlas) {
        this.birdTexture = atlas.findRegion("redbird"); // Use appropriate region
    }

    public void placeOnCatapult(float catapultX, float catapultY) {
        moveTo(catapultX, catapultY);
        state = BirdState.ON_CATAPULT;
    }

    public void returnToGround(float groundX, float groundY) {
        moveTo(groundX, groundY);
        state = BirdState.WAITING;
    }

    // Add this method to clamp velocity
    private Vector2 clampVelocity(Vector2 velocity) {
        float speed = velocity.len();
        if (speed > MAX_VELOCITY) {
            velocity.nor().scl(MAX_VELOCITY);
        }
        return velocity;
    }

    // Modify the launch method to include speed control
    public void launch(float forceX, float forceY) {
        if (state == BirdState.ON_CATAPULT) {
            state = BirdState.LAUNCHED;
            Vector2 force = new Vector2(forceX, forceY).scl(SPEED_MULTIPLIER);
            // Apply impulse with controlled force
            body.applyLinearImpulse(force, body.getWorldCenter(), true);

            // Add velocity clamping in act method
            Vector2 velocity = body.getLinearVelocity();
            velocity = clampVelocity(velocity);
            body.setLinearVelocity(velocity);
        }
    }

    public BirdState getState() {
        return state;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (birdTexture != null && body != null) {
            // Get the body's position and convert to screen coordinates
            Vector2 pos = body.getPosition();
            float width = getWidth() * getScaleX();
            float height = getHeight() * getScaleY();

            // Calculate screen position (centered)
            float x = (pos.x * GamePhysicsConstants.PPM) - width/2;
            float y = (pos.y * GamePhysicsConstants.PPM) - height/2;

            batch.draw(
                birdTexture,
                x, y,                  // Position
                width/2, height/2,     // Origin (center)
                width, height,         // Size
                1, 1,                  // Scale (already applied to width/height)
                (float)Math.toDegrees(body.getAngle()) // Rotation
            );
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Clamp velocity each frame
        if (state == BirdState.LAUNCHED) {
            Vector2 velocity = body.getLinearVelocity();
            velocity = clampVelocity(velocity);
            body.setLinearVelocity(velocity);
        }

        Vector2 bodyPosition = body.getPosition();
        setPosition(bodyPosition.x * GamePhysicsConstants.PPM, bodyPosition.y * GamePhysicsConstants.PPM);
    }
}
