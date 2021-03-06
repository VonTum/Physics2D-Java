package physics2D.tests;

import game.util.Color;

import org.junit.Test;

import physics2D.Debug;
import physics2D.geom.CompositePolygon;
import physics2D.geom.Polygon;
import physics2D.geom.Triangle;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;

public class TriangulationTests extends GUITestSuite{
	static final Vec2[] polygon = {
		new Vec2(1.0, 1.0),
		new Vec2(-3.0, 1.5),
		new Vec2(-2.0, -0.3),
		new Vec2(2.0, 0.3),
		new Vec2(2.4, 0.7),
		new Vec2(3.0, -1.0),
		new Vec2(3.5, -1.0),
		new Vec2(2.0, 2.0),
		new Vec2(3.5, 1.0),
		new Vec2(2.0, 2.5),
	};
	
	static final Vec2[] halfcircleThing = new Vec2[16];
	static{
		int L = halfcircleThing.length;
		RotMat2 r = new RotMat2(2*Math.PI/(L-2));
		Vec2 cur = new Vec2(2.0, 0.0);
		for(int i = 0; i < L/2-1; i++){
			halfcircleThing[i] = cur;
			cur = r.mul(cur);
		}
		halfcircleThing[L/2-1] = cur;
		cur = cur.div(1.2);
		for(int i = L/2; i < L-1; i++){
			halfcircleThing[i] = cur;
			cur = r.inv().mul(cur);
		}
		halfcircleThing[L-1] = cur;
	}
	
	
	@Test
	public void testTriangulate() {
		//Debug.logPolygon(Color.GREEN, new Vec2[]{new Vec2(3.0, 2.0),new Vec2(4.0, 3.0),new Vec2(3.5, 3.0)});
		Debug.logPolygon(Color.TRANSPARENT, polygon);
		
		Triangle[] division = new CompositePolygon(Polygon.shifted(polygon, 1)).divideIntoTriangles();
		
		for(Triangle t:division){
			Debug.logPolygon(Color.random().fuzzier(), Color.TRANSPARENT, t.getCorners());
			System.out.println(t);
		}
	}
	
	@Test
	public void testTriangulateBad() {
		//Debug.logPolygon(Color.GREEN, new Vec2[]{new Vec2(3.0, 2.0),new Vec2(4.0, 3.0),new Vec2(3.5, 3.0)});
		Debug.logPolygon(Color.TRANSPARENT, polygon);
		
		Triangle[] division = new CompositePolygon(Polygon.shifted(polygon, 2)).divideIntoTriangles();
		
		for(Triangle t:division){
			Debug.logPolygon(Color.random().fuzzier(), Color.TRANSPARENT, t.getCorners());
			System.out.println(t);
		}
	}
	
	@Test
	public void testSimpleBad(){
		Vec2[] simpleBad = new Vec2[]{
			new Vec2(0.0, 0.4),
			new Vec2(0.5, -0.8),
			new Vec2(0.5, 0.8),
			new Vec2(-0.5, 0.8),
			new Vec2(-0.5, -0.8),
			
		};
		
		Debug.logPolygon(Color.TRANSPARENT, simpleBad);
		
		Triangle[] division = new CompositePolygon(simpleBad).divideIntoTriangles();
		
		for(Triangle t:division){
			Debug.logPolygon(Color.random().fuzzier(), Color.TRANSPARENT, t.getCorners());
			System.out.println(t);
		}
	}
	
	@Test
	public void testConvexDecompose(){
		Debug.logPolygon(Color.TRANSPARENT, polygon);
		
		Polygon.convexDecomposition(polygon);
		Polygon.convexDecomposition(halfcircleThing);
	}
}
