package physics2D.tests;
import static org.junit.Assert.*;

import org.junit.Test;

import physics2D.math.CFrame;
import physics2D.math.Mat2;
import physics2D.math.NormalizedVec2;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;
import physics2D.math.WorldVec2;
import static physics2D.tests.util.TestUtil.*;

public class MathTests {
	
	public static final double DELTA = 1E-15;
	
	Mat2 testMat = new Mat2(1.0, 2.0, -0.3, 0.0);
	Mat2 invOfTestMat = new Mat2(0.0, -3.3333333333333333333333, 0.5, 1.6666666666666666666666);
	Vec2 testVec = new Vec2(1.7, -0.4);
	
	Mat2 testRotMat = Mat2.rotTransform(0.3);
	
	CFrame identityCFrame = new CFrame();
	CFrame justPosition = new CFrame(new Vec2(0.7, -0.3), Mat2.IDENTITY);
	CFrame justRotation = new CFrame(Vec2.ZERO, new RotMat2(0.3));
	CFrame fullCFrame = new CFrame(new Vec2(-0.3, 0.4), new RotMat2(-0.4));
	
	
	
	@Test
	public void testEquality() {
		assertMatEquals(testMat, testMat);
		assertMatNotEquals(testMat.inv(), testMat);
	}
	
	@Test
	public void testInverse(){
		assertMatEquals(invOfTestMat, testMat.inv());
	}
	
	@Test
	public void testMatTimesInv(){
		assertMatEquals(Mat2.IDENTITY, testMat.inv().mul(testMat));
		assertMatEquals(Mat2.IDENTITY, testMat.mul(testMat.inv()));
	}
	
	@Test
	public void testIdentity(){
		assertMatEquals(testMat, testMat.mul(Mat2.IDENTITY));
		assertMatEquals(testMat, Mat2.IDENTITY.mul(testMat));
	}
	
	@Test
	public void testZero(){
		assertMatEquals(Mat2.ZERO, testMat.subtract(testMat));
		assertMatEquals(Mat2.ZERO, testMat.mul(Mat2.ZERO));
		assertMatEquals(Mat2.ZERO, Mat2.ZERO.mul(testMat));
		assertMatEquals(Mat2.ZERO, testMat.mul(0));
		assertVecEquals(Vec2.ZERO, testMat.mul(Vec2.ZERO));
		assertVecEquals(Vec2.ZERO, Mat2.ZERO.mul(testVec));
	}
	
	@Test
	public void testRotationMat(){
		assertMatEquals(new Mat2(Math.cos(0.5), -Math.sin(0.5), Math.sin(0.5), Math.cos(0.5)), Mat2.rotTransform(0.5));
	}
	
	@Test
	public void testRotAngleCorrect(){
		assertEquals(0.3, Mat2.rotTransform(0.3).getAngle(), DELTA);
		assertEquals(-0.7, Mat2.rotTransform(-0.7).getAngle(), DELTA);
		assertEquals(0.0, Mat2.rotTransform(0.0).getAngle(), DELTA);
	}
	
	@Test
	public void testVecEquality(){
		assertVecEquals(testVec, testVec);
	}
	
	@Test
	public void testVecOperations(){
		assertVecEquals(new Vec2(0.4, -0.7), new Vec2(0.7, -0.3).add(new Vec2(-0.3, -0.4)));
		assertVecEquals(new Vec2(0.1, 0.7), new Vec2(0.7, -0.3).subtract(new Vec2(0.6, -1.0)));
		assertVecEquals(new Vec2(0.2, 0.4), new Vec2(0.1, 0.2).mul(2));
	}
	
	@Test
	public void testIdentityCFrame(){
		assertVecEquals(testVec, identityCFrame.globalToLocal(testVec));
		assertVecEquals(testVec, identityCFrame.localToGlobal(testVec));
	}
	
	@Test
	public void testCFrameMove(){
		assertVecEquals(testVec.add(justPosition.position), justPosition.localToGlobal(testVec));
		assertVecEquals(testVec.subtract(justPosition.position), justPosition.globalToLocal(testVec));
	}
	
	@Test
	public void testCFrameRotate(){
		assertVecEquals(justRotation.rotation.mul(testVec), justRotation.localToGlobal(testVec));
		assertVecEquals(justRotation.rotation.inv().mul(testVec), justRotation.globalToLocal(testVec));
	}
	
	@Test
	public void testCFrameBackAndForth(){
		assertVecEquals(testVec, identityCFrame.localToGlobal(identityCFrame.globalToLocal(testVec)));
		assertVecEquals(testVec, justPosition.localToGlobal(justPosition.globalToLocal(testVec)));
		assertVecEquals(testVec, justRotation.localToGlobal(justRotation.globalToLocal(testVec)));
		assertVecEquals(testVec, fullCFrame.localToGlobal(fullCFrame.globalToLocal(testVec)));
	}
	
	/*@Test
	public void testNormalVecsActuallyNormal(){
		Vec2 localVec = new Vec2(0.0, 0.1);
		Vec2 realVec = box.cframe.localToGlobal(localVec);
		Vec2 nrmlVec = box.getNormalVecAndDepthOfNearestEdge(realVec).direction;
		assertOrthogonal(realVec.subtract(box.getCenter()), nrmlVec);
		
		Vec2 alongTopEdge = box.getVertexes()[0].position.subtract(box.getVertexes()[1].position);
		Vec2 pointInUpperQuadrant = box.cframe.localToGlobal(new Vec2(0.0, 0.1));
		Vec2 normalVec = box.getNormalVecAndDepthOfNearestEdge(pointInUpperQuadrant).direction;
		assertInLine(alongTopEdge, normalVec);
	}*/
	
	@Test
	public void testDoesOrthogonalWork/*?*/(){
		assertOrthogonal(new Vec2(0.5, 0.5), new Vec2(0.5, -0.5));
		assertInLine(new Vec2(0.4, 0.8), new Vec2(1.5, 3));
	}
	
	@Test
	public void testPointToLineDist(){
		Vec2 lineVec = new Vec2(2.0, 0.0);
		Vec2 pointVec = new Vec2(-3.0, -3.0);
		
		assertEquals(3.0, lineVec.pointToLineDistance(pointVec), DELTA);
		assertEquals(9.0, lineVec.pointToLineDistanceSquared(pointVec), DELTA);
		assertEquals(3.0, testRotMat.mul(lineVec).pointToLineDistance(testRotMat.mul(pointVec)), DELTA);
		
	}
	
	@Test
	public void testRotMatFromNormalVector(){
		NormalizedVec2 vec = new NormalizedVec2(0.3);
		
		assertMatEquals(new RotMat2(0.3), RotMat2.fromNormalizedVecAsXAxe(vec));
		assertMatEquals(new RotMat2(0.3-Math.PI/2), RotMat2.fromNormalizedVecAsYAxe(vec));
	}
	
	@Test
	public void testWorldVecIntersection(){
		WorldVec2 firstVec = new WorldVec2(new Vec2(-1, 0), new Vec2(1, 1));
		WorldVec2 secondVec = new WorldVec2(new Vec2(0, 0), new Vec2(1, 2));
		WorldVec2 negFirstVec = new WorldVec2(firstVec.origin, firstVec.vector.neg());
		
		assertVecEquals(new Vec2(1, 2), firstVec.intersect(secondVec));
		assertVecEquals(new Vec2(1, 2), secondVec.intersect(firstVec));
		assertVecEquals(new Vec2(1, 2), negFirstVec.intersect(secondVec));
		assertVecEquals(new Vec2(1, 2), secondVec.intersect(negFirstVec));
		
		assertVecEquals(new Vec2(1, Math.tan(0.3)), new WorldVec2(Vec2.ZERO, Vec2.fromPolar(1, 0.3)).intersect(new WorldVec2(Vec2.UNITX, Vec2.UNITY)));
	}
}
