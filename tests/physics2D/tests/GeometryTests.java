package physics2D.tests;
import static org.junit.Assert.*;
import game.util.Color;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import physics2D.Debug;
import physics2D.geom.*;
import physics2D.math.CFrame;
import physics2D.math.NormalizedVec2;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;
import physics2D.math.Vertex2;
import static physics2D.tests.util.TestUtil.*;

public class GeometryTests {
	
	static final Vec2[] polygon = {
			new Vec2(1.0, 1.0),
			new Vec2(-3.0, 1.5),
			new Vec2(-2.0, -0.3),
			new Vec2(2.0, 0.3),
			new Vec2(2.5, 0.7),
			new Vec2(3.0, -1.0),
			new Vec2(3.5, -1.0),
			new Vec2(3.0, 1.0)
	};
	
	static final Vec2[] convexPolygon = {
		new Vec2(1.0, 1.0),
		new Vec2(0.5, 1.1),
		new Vec2(-0.3, 0.5),
		new Vec2(-0.1, -0.5),
		new Vec2(0.4, -0.6),
		new Vec2(0.7, 0.0)
	};
	
	static final Vec2[] spheroid = new Vec2[11];
	
	static {
		for(int i = 0; i < 11; i++)
			spheroid[i] = new RotMat2(i * 2 * Math.PI / 11).mul(Vec2.UNITX);
	}
	
	@Before
	public void setupDebugScreen(){
		if(Boolean.getBoolean("debugEnabled"))
			Debug.setupDebugScreen();
	}
	
	@After
	public void destroyDebugScreen(){
		if(Boolean.getBoolean("debugEnabled")){
			Debug.endTick();
			if(Debug.getDrawCount() > 0)
				Debug.halt();
			else
				Debug.destroyDebugScreen();
		}
	}
	
	@Test
	public void testPolygonContainsPoint() {
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
	public void testGetCollisionOutline(){
		Polygon p1 = new Rectangle(0.3, 0.1).transformToCFrame(new CFrame(-0.2, 0.3, 0.0));
		
		
		Polygon p2 = new RegularPolygon(3, new Vec2(0.2, 0.0)).transformToCFrame(new CFrame(0.0, 0.1, 0.0));
		
		// Debug.logShape(p1, util.Color.DEFAULT_BRICK_COLOR);
		// Debug.logShape(p2, util.Color.BLUE.alpha(util.Color.DEFAULT_BRICK_COLOR.a));
		
		CollisionOutline outline = p1.getCollisionOutline(p2);
		//CollisionOutline outline2 = p2.getCollisionOutline(p1);
		
		Debug.logPoint(outline.getCollisionPoint(p2.getCenterOfMass()), Color.PURPLE);
		//Debug.logPoint(outline2.getCollisionPoint(p1.getCenterOfMass()), Color.BLUE);
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
	
	@Test
	public void testIntersection(){
		Rectangle r1 = new Rectangle(0.3, 0.1);
		Rectangle r2 = new Rectangle(0.2, 0.2);
		ConvexPolygon b2 = new Triangle(0.3, new Vec2(0.2, 0.1));
		
		Debug.logShape(r1, Color.BLUE);
		
		ConvexPolygon[] collidingPolygons = {r2, r2.transformToCFrame(new CFrame(0.25, 0.0)), b2, b2.transformToCFrame(new CFrame(0.2, 0.07)), b2.transformToCFrame(new CFrame(-0.1, 0.25, 1.5))};
		ConvexPolygon[] disjunctPolygons = {r2.transformToCFrame(new CFrame(0.28, 0.1)), r2.transformToCFrame(new CFrame(-0.28, 0.1)), b2.transformToCFrame(new CFrame(0.03, 0.3, 1.5))};
		
		for(ConvexPolygon p:collidingPolygons){
			Debug.logShape(p, r1.intersects(p) ? Color.GREEN.fuzzier() : Color.YELLOW.fuzzier());
		}
		
		for(ConvexPolygon p:disjunctPolygons){
			Debug.logShape(p, r1.intersects(p) ? Color.RED.fuzzier() : Color.ORANGE.fuzzier());
		}
	}
	
	@Test
	public void testIntersect(){
		Vec2[] poly1 = new Triangle(0.8, new Vec2(0.2, 0.4)).getCorners();
		Vec2[] poly2 = new RegularPolygon(12, new Vec2(0.2, 0)).transformToCFrame(new CFrame(-0.2, 0.0)).getCorners();
		Vec2[] poly3 = new DummyPolygon(convexPolygon).scale(0.2).transformToCFrame(new CFrame(-0.17, -0.03)).getCorners();
		
		Debug.logPolygon(Color.GREEN.fuzzier(), poly1);
		Debug.logPolygon(Color.BLUE.fuzzier(), poly2);
		Vec2[] intersect1 = ConvexPolygon.intersection(poly1, poly2);
		Debug.logPolygon(Color.CYAN.fuzzier(), intersect1);
		Debug.logPolygon(Color.ORANGE.fuzzier(0.5), poly3);
		Vec2[] intersect2 = ConvexPolygon.intersection(intersect1, poly3);
		Debug.logPolygon(Color.RED, intersect2);
		
	}
	
	@Test
	public void testGetIntersectionPoint(){
		Rectangle r1 = new Rectangle(0.3, 0.1);
		ConvexPolygon b2 = new Triangle(0.3, new Vec2(0.2, 0.1)).transformToCFrame(new CFrame(0.2, 0.2, 0.7));
		
		Debug.logShape(r1, Color.BLUE);
		Debug.logShape(b2, Color.YELLOW);
		
		r1.getIntersectionPoint(b2);
	}
	
	@Test
	public void testSlicing(){
		Vec2[] startPoly = new Rectangle(0.3, 0.2).transformToCFrame(new CFrame(0.0, 0.0, 0.2)).getCorners();
		
		Vec2[] slices = new Vec2[]{
				new Vec2(0.1, 0.07), new Vec2(-0.3, 0.4),
				new Vec2(0.02, -0.3), new Vec2(0.3, 1.2),
				new Vec2(0.3, -0.3), new Vec2(1.0, 1.0),
				new Vec2(0.3, 0.3), new Vec2(1.0, 1.0),
				new Vec2(-0.2, -0.3), new Vec2(-1.0, -2.0)
		};
		
		Debug.logPolygon(Color.BLUE, startPoly);
		
		Vec2[] curPoly = startPoly;
		
		for(int i = 0; i < slices.length; i+=2){
			Debug.logVector(slices[i], slices[i+1], Color.BLACK);
			curPoly = ConvexPolygon.leftSlice(curPoly, slices[i], slices[i+1]);
			Debug.logPolygon(new Color(0, 1.0/(i+1), 0, 0.6), curPoly);
		}
		
		Debug.logPolygon(Color.YELLOW.fuzzier(), curPoly);
	}
	
	private static final class DummyPolygon extends Polygon {
		public DummyPolygon(Vec2... polygon) {
			super(polygon);
		}
		
		public DummyPolygon(Vertex2... vertexes) {
			super(vertexes);
		}
		
		@Override public double getArea() {return 0;}
		@Override public double getInertialArea() {return 0;}
		@Override public Vec2 getCenterOfMass() {return Vec2.ZERO;}
		@Override public Shape leftSlice(Vec2 origin, Vec2 direction) {return null;}
	}
}
