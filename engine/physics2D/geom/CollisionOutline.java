package physics2D.geom;

import physics2D.math.Vec2;
import physics2D.math.Vertex2;

public class CollisionOutline {
	public final Vec2[] outline, refPoints;
	public CollisionOutline(Vec2[] outline, Vec2[] refPoints){
		this.outline = outline;
		this.refPoints = refPoints;
	}
	
	public AbstractPolygon asPolygon(){
		return new DummyPolygon(outline);
	}
	
	/**
	 * Tries to approximate the collision point, but it seems to be a bad one
	 * @param otherShapeLocation
	 * @return
	 */
	public Vec2 getCollisionPoint(Vec2 otherShapeLocation){
		Vec2 r = otherShapeLocation;
		
		AbstractPolygon colShape = new DummyPolygon(outline);
		
		if(!colShape.containsPoint(r))
			return null;
		
		double totalWeight = 0;
		Vec2 weightedAverage = Vec2.ZERO;
		
		for(int i = 0; i < outline.length; i++){
			Vec2 point = outline[i];
			Vec2 ref = refPoints[i];
			double distance = r.subtract(point).lengthSquared();
			double weight = 1/distance;
			weightedAverage = weightedAverage.add(ref.mul(weight));
			totalWeight += weight;
		}
		
		return weightedAverage.div(totalWeight);
	}
	
	private static final class DummyPolygon extends AbstractPolygon {
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
