package io.github.AngryBird_2023124_2023371.Screens;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class BombBird extends BaseBird {
    private static final float BOMB_BIRD_SCALE = 0.05f; // Reduced from 0.4f to 0.2f (50% smaller)
    private static final float BOMB_BIRD_RADIUS_MODIFIER = 0.5f; // 50% of base radius
    private static final float EXPLOSION_SCALE = 0.4f; // Scale during explosion
    private static final float EXPLOSION_DURATION = 0.02f; // Duration in seconds
    private boolean isExploding = false;
    private float explosionTimer = 0;
    private float originalRadius;

    public BombBird(TextureAtlas atlas, Vector2 position, World world) {
        super(atlas.findRegion("BombBird"), position, world);
        setScale(BOMB_BIRD_SCALE);
    }

    // Add method to scale physics body
    private void scalePhysicsBody(float scale) {
        if (body == null) return;

        // Remove old fixture
        if (body.getFixtureList().size > 0) {
            body.destroyFixture(body.getFixtureList().get(0));
        }

        // Create new fixture with scaled radius
        CircleShape shape = new CircleShape();
        float scaledRadius = (originalRadius * scale) / PPM;
        shape.setRadius(scaledRadius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.2f;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    @Override
    protected void createPhysicsBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
            (position.x + getWidth() / 2) / PPM,
            (position.y + getHeight() / 2) / PPM
        );

        body = world.createBody(bodyDef);

        // Store original radius
        originalRadius = (Math.min(getWidth(), getHeight()) * PHYSICS_RADIUS_SCALE) / 4 * BOMB_BIRD_RADIUS_MODIFIER;

        // Create initial fixture
        CircleShape shape = new CircleShape();
        shape.setRadius(originalRadius / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.2f;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    // Add method to trigger explosion
    public void explode() {
        if (!isExploding && getState() == BirdState.LAUNCHED) {
            isExploding = true;
            setScale(EXPLOSION_SCALE);
            scalePhysicsBody(EXPLOSION_SCALE / BOMB_BIRD_SCALE); // Scale relative to initial scale
        }
    }

    // Add method to update explosion state
    public boolean updateExplosion(float delta) {
        if (isExploding) {
            explosionTimer += delta;
            if (explosionTimer >= EXPLOSION_DURATION) {
                return true; // Mark for removal
            }
        }
        return false;
    }
}
