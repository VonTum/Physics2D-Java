package physics.tests;
import static org.junit.Assert.*;
import game.ObjectLibrary;
import geom.Box;
import math.CFrame;
import math.Vec2;

import org.junit.Before;
import org.junit.Test;

import physics.Physical;
import physics.PhysicalProperties;
import physics.World;
import util.Color;
import static physics.tests.util.TestUtil.*;

public class PhysicsTest {
	
	private static final double MAX_VELOCITY = 5.0;
	private static final double MAX_ANGULAR_VELOCITY = Math.PI*2;
	
	private static final double deltaT = 0.01;
	
	private PhysicalProperties properties = new PhysicalProperties(10.0, 0.05, 0.0, Color.DEFAULT_BRICK_COLOR);
	
	private World basicWorld, noGravityWorld;
	private Physical noGravityBox;
	
	@Before
	public void setup() {
		basicWorld = new World(new Vec2(0.0, -1.0));
		basicWorld.addObject(ObjectLibrary.createFloor(new CFrame(0.0, 0.0), properties));
		basicWorld.addObject(new Box(new CFrame(0.0, 1.0, 0.3), 0.3, 0.1, properties));
		basicWorld.addObject(new Box(new CFrame(0.0, 1.5, 0.3), 0.1, 0.1, properties));
		basicWorld.addObject(new Box(new CFrame(0.0, 0.5, -0.1), 0.1, 0.3, properties));
		
		noGravityWorld = new World(Vec2.ZERO);
		noGravityBox = new Box(new CFrame(0.0, 0.0), 0.3, 0.1, properties);
		noGravityWorld.addObject(noGravityBox);
	}
	
	public void assertNotFlipping(World w) {
		for(Physical p:w.physicals)
			assertNotFlipping(p);
	}
	
	public void assertNotFlipping(Physical p) {
		assertTrue("Object " + p + " moving too fast! " + p.velocity, p.velocity.isShorterThan(MAX_VELOCITY));
		assertTrue("Object " + p + " rotating too fast! " + p.angularVelocity, p.angularVelocity < MAX_ANGULAR_VELOCITY);
	}
	
	@Test
	public void testNotFlipping() {
		for(int i = 0; i < 1000; i++){
			basicWorld.tick(0.01);
			assertNotFlipping(basicWorld);
		}
	}
	
	@Test
	public void testAcceleration(){
		Box testBox = new Box(new CFrame(0.0, 0.0), 0.3, 0.1, properties);
		
		Vec2 localAttachPoint = new Vec2(0.15, 0.0);
		Vec2 desiredAcceleration = Vec2.UNITY.mul(0.5);
		double centerRightInertia = testBox.getPointInertia(localAttachPoint, Vec2.UNITY);
		double centerLeftInertia = testBox.getPointInertia(new Vec2(-0.15, 0.0), Vec2.UNITY);
		
		double upperRightCornerInertia = testBox.getPointInertia(new Vec2(0.15, 0.05), Vec2.UNITY);
		assertEquals(centerRightInertia, upperRightCornerInertia, DELTA);
		assertEquals(centerRightInertia, centerLeftInertia, DELTA);
		
		testBox.applyForce(desiredAcceleration.mul(centerRightInertia), localAttachPoint);
		
		testBox.update(deltaT);
		
		double resultantVelocityY = desiredAcceleration.mul(deltaT).y;
		
		Vec2 newGlobalPoint = testBox.cframe.localToGlobal(localAttachPoint);
		assertEquals(testBox.getSpeedOfPoint(newGlobalPoint).y, resultantVelocityY, 1E-6);
	}
	
	@Test
	public void testPreservationOfEnergy(){
		Box testBox = new Box(new CFrame(0.0, 0.0), 0.3, 0.1, properties);
		testBox.velocity = new Vec2(0.0, -5.0);
		
		Vec2 gravity = Vec2.UNITNEGY;
		
		double lastEnergy = testBox.getEnergy(gravity);
		
		for(int i = 0; i < 10000; i++){
			testBox.applyForce(gravity.mul(testBox.getMass()));
			testBox.update(deltaT);
			assertTrue("energy has increased randomly " + lastEnergy + " => " + testBox.getEnergy(gravity) + " at tick " + i + " with delta " + (testBox.getEnergy(gravity) - lastEnergy), lastEnergy+1E-8 >= testBox.getEnergy(gravity));
			lastEnergy = testBox.getEnergy(gravity);
		}
	}
	
	@Test
	public void testImpulse(){
		Box box = new Box(new CFrame(0.0, 0.0), 0.6, 0.6, properties);
		
		Vec2 point = new Vec2(0.3, -0.3);
		
		Vec2 accel = new Vec2(0.7, 0.3);
		
		double inertia = box.getPointInertia(point, accel.normalize());
		
		Vec2 impulse = accel.mul(inertia);
		box.applyImpulse(impulse, point);
		
		assertEquals(accel.length(), box.getSpeedOfPoint(point).dot(accel.normalize()), DELTA);
	}
}
