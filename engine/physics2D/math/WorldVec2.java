package physics2D.math;

public class WorldVec2 {
	public final Vec2 origin, vector;
	public WorldVec2(Vec2 origin, Vec2 vector){
		this.origin = origin;
		this.vector = vector;
	}
	
	public WorldVec2(double xo, double yo, double dx, double dy){
		this(new Vec2(xo, yo), new Vec2(dx, dy));
	}
	
	public Vec2 getEnd(){
		return origin.add(vector);
	}
	
	public Vec2 intersect(WorldVec2 other){
		Vec2 u = this.origin;
		Vec2 v = other.origin;
		Vec2 du = this.vector;
		Vec2 dv = other.vector;
		
		double a = v.subtract(u).cross(dv) / du.cross(dv);
		return u.add(du.mul(a));
	}
}
