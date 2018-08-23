import static org.junit.Assert.*;

import java.util.Arrays;

import game.Debug;
import game.gui.Screen;
import geom.Polygon;
import math.CFrame;
import math.Vec2;
import math.Vertex2;

import org.junit.Test;

import util.Color;


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
		
		// DEBUG
		Debug.setupDebugScreen();
		Screen.markPolygon(polygon, Color.TRANSPARENT);
		
		for(Vec2 point:insidePoints)
			Screen.markPoint(point, p.containsPoint(point)?Color.GREEN:Color.RED);
		
		for(Vec2 point:outsidePoints)
			Screen.markPoint(point, p.containsPoint(point)?Color.GREEN:Color.RED);
		
		Screen.commitDrawings();
		
		Debug.halt();
		// /DEBUG
		
		for(Vec2 point:insidePoints)
			assertTrue("Point " + point + " was shown to be outside while it was actually inside", p.containsPoint(point));
		
		for(Vec2 point:outsidePoints)
			assertFalse("Point " + point + " was shown to be inside while it was actually outside", p.containsPoint(point));
	}
	
	private final class DummyPolygon extends Polygon {
		public DummyPolygon(Vec2... polygon) {
			super(null, new CFrame(0.0, 0.0), polygon);
		}
		
		public DummyPolygon(Vertex2... vertexes) {
			super(null, new CFrame(0.0, 0.0), vertexes);
		}
		
		@Override public double getArea() {return 0;}
		@Override public double getInertia() {return 0;}
		@Override public Vec2 getCenterOfMass() {return null;}
	}
}
