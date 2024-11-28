package io.github.AngryBird_2023124_2023371.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.physics.box2d.*;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public abstract class BaseBlock extends Actor implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Mark non-serializable LibGDX components as transient
    protected transient TextureRegion blockTexture;
    protected transient Body body;
    protected Vector2 position; // Keep serializable
    protected float health;     // Keep serializable
    protected transient World world;
    private static final float BLOCK_SCALE = 0.1f; // Define BLOCK_SCALE

    public Vector2 getPosition() {
        return position;
    }

    public TextureRegion getBlockTexture() {
        return blockTexture;
    }

    public void setBlockTexture(TextureRegion blockTexture) {
        this.blockTexture = blockTexture;
    }

    public Body getBody() {
        return body;
    }

    public World getWorld(){
        return world;
    }
    public float getHealth() {
        return health;
    }
    public void setHealth(float health) {
        this.health = health;
    }

    public BaseBlock(TextureRegion texture, Vector2 position, World world) {
        Gdx.app.log("BaseBlock", "Position in BaseBlock constructor: " + position);  // Log position
        this.world = world;
        this.health = 100;
        this.blockTexture = texture;
        this.position = position;

        if (this.blockTexture == null) {
            System.out.println("Error: blockTexture is null. Using a fallback texture.");
            // Handle fallback texture if necessary
        }

        // Set initial scale
        setScale(BLOCK_SCALE);

        // Set size based on texture dimensions
        setSize(texture.getRegionWidth(), texture.getRegionHeight());

        // Create physics body after setting size
        createPhysicsBody(world);
    }

    // Add method to reinitialize transient fields after deserialization
    public void initializeTransients(TextureRegion texture, World world) {
        this.blockTexture = texture;
        this.world = world;
        createPhysicsBody(world);
    }
    
    // Add readObject method for custom deserialization if needed
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
    
    // Add writeObject method for custom serialization if needed
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    public void createPhysicsBody(World world) {
        // Log dimensions for debugging
        Gdx.app.log("BaseBlock", "Creating physics body with dimensions: " +
                    "width=" + (getWidth() * getScaleX()) +
                    ", height=" + (getHeight() * getScaleY()));

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        // Center the body at the position
        bodyDef.position.set(
            (position.x + (getWidth() * getScaleX() / 2)) / GamePhysicsConstants.PPM,
            (position.y + (getHeight() * getScaleY() / 2)) / GamePhysicsConstants.PPM
        );

        body = world.createBody(bodyDef);

        // Create scaled rectangle shape
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            (getWidth() * getScaleX()) / 2 / GamePhysicsConstants.PPM,
            (getHeight() * getScaleY()) / 2 / GamePhysicsConstants.PPM
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 5.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void setPosition(Vector2 position) {
        this.position = position;
        body.setTransform(position.x / GamePhysicsConstants.PPM, position.y / GamePhysicsConstants.PPM, 0);
    }
    public void reduceHealth(float damage) {
        // Implement health reduction logic, e.g., decrease health when hit
        this.health -= damage;
        if (this.health <= 0) {
            // Destroy the block when health is 0 or less
            world.destroyBody(body);  // Destroy the Box2D body
            remove();  // Remove the block from the stage (LibGDX actor)
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (blockTexture != null && body != null) {
            // Get the body's position and convert to screen coordinates
            Vector2 pos = body.getPosition();
            float width = getWidth() * getScaleX();
            float height = getHeight() * getScaleY();

            // Calculate screen position (centered)
            float x = (pos.x * GamePhysicsConstants.PPM) - width/2;
            float y = (pos.y * GamePhysicsConstants.PPM) - height/2;

            batch.draw(
                blockTexture,
                x, y,                  // Position
                width/2, height/2,     // Origin (center)
                width, height,         // Size
                1, 1,                  // Scale (already applied to width/height)
                (float)Math.toDegrees(body.getAngle()) // Rotation
            );
        }
    }

    public boolean isDestroyed(){
        return health<=0;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Vector2 bodyPosition = body.getPosition();
        setPosition(bodyPosition.x * GamePhysicsConstants.PPM, bodyPosition.y * GamePhysicsConstants.PPM);
    }



}
