package physics.tests;
import static org.junit.Assert.*;
import geom.Box;
import math.CFrame;
import math.Mat2;
import math.Vec2;

import org.junit.Test;

import physics.PhysicalProperties;
import util.Color;
import static physics.tests.util.TestUtil.*;

public class PhysicsTest {
	
	private static final double deltaT = 0.01;
	
	private PhysicalProperties properties = new PhysicalProperties(10.0, 0.05, 0.0, Color.DEFAULT_BRICK_COLOR);
	
	private Box testBox = new Box(CFrame.IDENTITY, 0.3, 0.1, properties);
	
	@Test
	public void testAcceleration(){
		// Box testBox = new Box(new CFrame(0.0, 0.0), 0.3, 0.1, properties);
		
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
		// Box testBox = new Box(new CFrame(0.0, 0.0), 0.3, 0.1, properties);
		testBox.velocity = new Vec2(0.0, -5.0);
		
		Vec2 gravity = Vec2.UNITNEGY;
		
		double lastEnergy = testBox.getEnergy(gravity);
		
		for(int i = 0; i < 10000; i++){
			testBox.applyForceAtCenterOfMass(gravity.mul(testBox.getMass()));
			testBox.update(deltaT);
			assertTrue("energy has increased randomly " + lastEnergy + " => " + testBox.getEnergy(gravity) + " at tick " + i + " with delta " + (testBox.getEnergy(gravity) - lastEnergy), lastEnergy+1E-8 >= testBox.getEnergy(gravity));
			lastEnergy = testBox.getEnergy(gravity);
		}
	}
	
	@Test
	public void testInertiaThroughCenter(){
		// Box testBox = new Box(new CFrame(0.0, 0.0), 0.6, 0.6, properties);
		
		assertEquals(testBox.getMass(), testBox.getPointInertia(Vec2.ZERO, Vec2.UNITX), DELTA);
		assertEquals(testBox.getMass(), testBox.getPointInertia(Vec2.ZERO, Vec2.UNITY), DELTA);
		assertEquals(testBox.getMass(), testBox.getPointInertia(Vec2.ZERO, Vec2.UNITNEGX), DELTA);
		assertEquals(testBox.getMass(), testBox.getPointInertia(Vec2.ZERO, Vec2.UNITNEGY), DELTA);
		
		Vec2 offset = new Vec2(0.3, 0.7);
		
		assertEquals(testBox.getMass(), testBox.getPointInertia(offset, offset.neg().normalize()), DELTA);
	}
	
	@Test
	public void testImpulse(){
		// Box testBox = new Box(new CFrame(0.0, 0.0), 0.6, 0.6, properties);
		
		Vec2 point = new Vec2(0.15, -0.05);
		
		Vec2 accel = new Vec2(0.0, 0.7);
		
		
		
		double inertia = testBox.getPointInertia(point, accel.normalize());
		
		
		
		Vec2 impulse = accel.mul(inertia);
		testBox.applyImpulse(impulse, point);
		
		System.out.println("Accel: " + accel + "\nInertia: " + inertia + "\nImpulse: " + impulse + "\nSpeed: " + testBox.getSpeedOfPoint(point));
		System.out.println(testBox);
		
		assertEquals(accel.y, testBox.getSpeedOfPoint(point).y, DELTA);
	}
	
	@Test
	public void testGetAccellerationOfPoint(){
		Vec2 relativePos = new Vec2(0.15, 0.05);
		
		double deltaT = 0.00001;
		
		testBox.velocity = new Vec2(2.0, 0.7);
		testBox.angularVelocity = 3.3;
		
		Vec2 lastVelocity = testBox.getSpeedOfPoint(relativePos);
		
		for(int i = 0; i < 100000; i++){
			Vec2 globalPos = testBox.cframe.localToGlobal(relativePos);
			testBox.applyForce(new Vec2(-0.3, 0.7), globalPos);
			Vec2 acceleration = testBox.getAccelerationOfPoint(globalPos);
			Vec2 curVelocity = testBox.getSpeedOfPoint(globalPos);
			// very high tolerance for error on this one, discretization errors are to be expected
			assertVecEquals(curVelocity.subtract(lastVelocity), acceleration.mul(deltaT), 1E-4); 
			lastVelocity = curVelocity;
			testBox.update(deltaT);
		}
	}
	
	@Test
	public void testGetPointInertialMatrix() {
		Vec2 relativePos = new Vec2(0.15, 0.05);
		
		Vec2 force = new Vec2(0.7, 0.5);
		
		Mat2 inertialMat = testBox.getPointInertialMatrix(relativePos);
		
		testBox.applyForce(force, relativePos);
		
		assertVecEquals(testBox.getAccelerationOfPoint(relativePos), inertialMat.mul(force));
	}
}
