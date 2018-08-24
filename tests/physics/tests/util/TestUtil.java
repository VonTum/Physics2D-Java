package physics.tests.util;
import static org.junit.Assert.fail;
import math.Mat2;
import math.Vec2;


public class TestUtil {
	
	public static final double DELTA = 1E-15;
	
	private static boolean eq(double a, double b, double delta){
		return Math.abs(a-b) <= delta;
	}
	
	
	public static void assertMatEquals(Mat2 expected, Mat2 given){assertMatEquals(expected, given, DELTA);}

	public static void assertMatEquals(Mat2 expected, Mat2 given, double delta){
		if(	!eq(expected.a, given.a, delta) || 
			!eq(expected.b, given.b, delta) ||
			!eq(expected.c, given.c, delta) ||
			!eq(expected.d, given.d, delta))
			fail("expected: " + expected + " but was: " + given);
	}

	public static void assertVecEquals(Vec2 expected, Vec2 given){assertVecEquals(expected, given, DELTA);}

	public static void assertVecEquals(Vec2 expected, Vec2 given, double delta){
		if( !eq(expected.x, given.x, delta) ||
			!eq(expected.y, given.y, delta))
			fail("expected: " + expected + " but was: " + given + " delta="+expected.subtract(given));
	}

	public static void assertMatNotEquals(Mat2 expected, Mat2 given){assertMatNotEquals(expected, given, DELTA);}

	public static void assertMatNotEquals(Mat2 expected, Mat2 given, double delta){
		if(	eq(expected.a, given.a, delta) || 
			eq(expected.b, given.b, delta) ||
			eq(expected.c, given.c, delta) ||
			eq(expected.d, given.d, delta))
			fail("expected: " + expected + " but was: " + given);
	}

	public static void assertOrthogonal(Vec2 v1, Vec2 v2){assertOrthogonal(v1, v2, DELTA);}

	public static void assertOrthogonal(Vec2 v1, Vec2 v2, double deltaDot){
		if(!eq(0.0, v1.normalize().dot(v2.normalize()), deltaDot))
			fail("Vectors " + v1 + " and " + v2 + " are not orthogonal! Angle is " + v1.angleBetween(v2)*180/Math.PI + "°");
	}

	public static void assertInLine(Vec2 v1, Vec2 v2){assertInLine(v1, v2, DELTA);}

	public static void assertInLine(Vec2 v1, Vec2 v2, double deltaCross){
		if(!eq(0.0, v1.normalize().cross(v2.normalize()), deltaCross))
			fail("Vectors " + v1 + " and " + v2 + " are not in line! Angle is " + v1.angleBetween(v2)*180/Math.PI + "°");
	}
	
}
