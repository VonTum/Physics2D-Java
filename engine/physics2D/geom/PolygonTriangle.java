package physics2D.geom;
import physics2D.math.Vec2;

public class PolygonTriangle extends ConvexPolygon {
	
	public PolygonTriangle(double baseWidth, Vec2 top) {
		this(top.subtract(top.div(3)), new Vec2(-baseWidth/2, 0).subtract(top.div(3)), new Vec2(baseWidth/2, 0).subtract(top.div(3)));
	}
	
	public PolygonTriangle(Vec2 v1, Vec2 v2, Vec2 v3){
		super(new Vec2[]{v1, v2, v3});
	}
	
	@Override
	public double getArea() {
		Vec2[] verts = getCorners();
		return verts[1].subtract(verts[0]).cross(verts[2].subtract(verts[0])) / 2;
	}
	
	@Override
	public double getInertialArea() {
		Vec2[] verts = getCorners();
		Vec2 v1 = verts[0];
		Vec2 v2t = verts[1].subtract(v1);
		Vec2 v3t = verts[2].subtract(v1);
		
		return getArea() * (v3t.subtract(v2t).lengthSquared() + v2t.dot(v3t))/18;
	}
	
	@Override
	public Vec2 getCenterOfMass() {
		return Vec2.sum(getCorners()).div(3);
	}
}
