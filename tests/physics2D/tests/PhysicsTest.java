package physics2D.tests;
import static org.junit.Assert.*;
import game.util.Color;

import org.junit.Test;

import physics2D.geom.Rectangle;
import physics2D.math.CFrame;
import physics2D.math.Mat2;
import physics2D.math.NormalizedVec2;
import physics2D.math.Vec2;
import physics2D.physics.Box;
import physics2D.physics.Physical;
import physics2D.physics.PhysicalProperties;
import static physics2D.tests.util.TestUtil.*;

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
	
	@Test
	public void testReferencePointInvariance(){
		double D = 1E-12;
		
		Rectangle r1 = new Rectangle(0.3, 0.1);
		Rectangle r2 = new Rectangle(0.1, 0.1);
		
		CFrame c1 = new CFrame(0.0, 0.0);
		CFrame c2 = new CFrame(0.3, 0.0);
		
		for(double x = -1.0; x < 1.0; x += 0.05){
			for(double y = -1.0; y < 1.0; y += 0.05){
				for(double r = 0.0; r < Math.PI*2; r += 0.1){
					CFrame curFrame = new CFrame(x, y, r);
					
					Physical p1 = new Physical(new CFrame(0.0, 0.0));
					p1.addPart(r1, c1, properties);
					p1.addPart(r2, c2, properties);
					
					Physical p2 = new Physical(curFrame);
					p2.addPart(r1, curFrame.globalToLocal(c1), properties);
					p2.addPart(r2, curFrame.globalToLocal(c2), properties);
					
					for(int i = 0; i < p1.parts.size(); i++)
						assertCFrameEquals(p1.parts.get(i).getGlobalCFrame(), p2.parts.get(i).getGlobalCFrame());
					
					Vec2 force = new Vec2(0.3, 0.7);
					Vec2 attachF = new Vec2(-0.15, 0.05);
					Vec2 impulse = new Vec2(-0.7, 0.5);
					Vec2 attachI = new Vec2(0.15, -0.00);
					
					Vec2 point = new Vec2(0.2, 0.1);
					
					Vec2 gravity = new Vec2(0.0, -2.0);
					
					NormalizedVec2 direction = new NormalizedVec2(0.3);
					
					for(int i = 0; i < 10; i++){
						p1.update(deltaT);
						p2.update(deltaT);
						
						p1.applyForce(force, attachF);
						p1.applyImpulse(impulse, attachI);
						
						p2.applyForce(force, attachF);
						p2.applyImpulse(impulse, attachI);
					}
					
					assertVecEquals(p1.getCenterOfMass(), p2.getCenterOfMass(), D);
					assertBoundingBoxEquals(p1.getBoundingBox(), p2.getBoundingBox(), D);
					assertVecEquals(p1.getSpeedOfPoint(point), p2.getSpeedOfPoint(point), D);
					assertVecEquals(p1.getAccelerationOfPoint(point), p1.getAccelerationOfPoint(point), D);
					assertEquals(p1.getEnergy(gravity), p2.getEnergy(gravity), D);
					assertEquals(p1.getPotentialEnergy(gravity), p2.getPotentialEnergy(gravity), D);
					assertEquals(p1.getKineticEnergy(), p2.getKineticEnergy(), D);
					assertEquals(p1.getRotAccelertation(), p2.getRotAccelertation(), D);
					assertEquals(p1.getPointInertia(point, direction), p2.getPointInertia(point, direction), D);
					assertMatEquals(p1.getPointInertialMatrix(point), p2.getPointInertialMatrix(point), D);
				}
			}
		}
	}
}
