package physics.tests;
import static org.junit.Assert.*;

import java.util.Arrays;

import game.Debug;
import game.gui.Screen;
import geom.*;
import math.CFrame;
import math.RotMat2;
import math.Vec2;
import math.Vertex2;

import org.junit.Test;

import static physics.tests.util.TestUtil.*;

public class GeometryTests {

	@Test
	public void testPolygonContainsPoint() {
		Vec2[] polygon = {
				new Vec2(1.0, 1.0),
				new Vec2(-3.0, 1.5),
				new Vec2(-2.0, -0.3),
				new Vec2(2.0, 0.3),
				new Vec2(2.5, 0.7),
				new Vec2(3.0, -1.0),
				new Vec2(3.5, -1.0),
				new Vec2(3.0, 1.0)
		};
		Vertex2[] vertexes = Vertex2.convertToVertexes(polygon);
		
		Polygon p = new DummyPolygon(vertexes);
		
		Vec2[] givenInsidePoints = {new Vec2(0.0, 0.0), new Vec2(-1.0, 0.7), new Vec2(-2.0, 1.0)};
		Vec2[] givenOutsidePoints = {new Vec2(-5.0, 1.0), new Vec2(-5.0, 0.7), new Vec2(-5.0, 1.5)};
		
		Vec2[] insidePoints = Arrays.copyOf(givenInsidePoints, givenInsidePoints.length + vertexes.length);
		Vec2[] outsidePoints = Arrays.copyOf(givenOutsidePoints, givenOutsidePoints.length + vertexes.length);
		
		for(int i = 0; i < vertexes.length; i++){
			insidePoints[givenInsidePoints.length + i] = vertexes[i].position.add(vertexes[i].orientation.mul(-0.1));
			outsidePoints[givenOutsidePoints.length + i] = vertexes[i].position.add(vertexes[i].orientation.mul(0.1));
		}
		
		for(Vec2 point:insidePoints)
			assertTrue("Point " + point + " was shown to be outside while it was actually inside", p.containsPoint(point));
		
		for(Vec2 point:outsidePoints)
			assertFalse("Point " + point + " was shown to be inside while it was actually outside", p.containsPoint(point));
	}
	
	
	@Test
	public void testRegularPolygon(){
		RegularPolygon square = new RegularPolygon(4, new Vec2(1.0, 1.0));
		
		Rectangle rect = new Rectangle(2.0, 2.0);
		
		assertEquals(rect.getArea(), square.getArea(), DELTA);
		assertEquals(rect.getInertialArea(), square.getInertialArea(), DELTA);
		
		
		RegularPolygon triangle = new RegularPolygon(3, new Vec2(1.0, 1.0));
		
		Vec2 d = triangle.vertexes[1].position.subtract(triangle.vertexes[0].position);
		Vec2 center = triangle.vertexes[1].position.add(triangle.vertexes[0].position).div(2);
		
		Triangle tri = new Triangle(d.length(), Vec2.UNITY.mul(triangle.vertexes[2].position.subtract(center).length()));
		
		assertEquals(tri.getArea(), triangle.getArea(), DELTA);
		assertEquals(tri.getInertialArea(), triangle.getInertialArea(), DELTA);
	}
	
	private final class DummyPolygon extends Polygon {
		public DummyPolygon(Vec2... polygon) {
			super(null, new CFrame(0.0, 0.0), polygon);
		}
		
		public DummyPolygon(Vertex2... vertexes) {
			super(null, new CFrame(0.0, 0.0), vertexes);
		}
		
		@Override public double getArea() {return 0;}
		@Override public double getInertialArea() {return 0;}
		@Override public Vec2 getCenterOfMass() {return null;}
	}
}
