package physics2D.geom;

import java.util.ArrayList;
import java.util.List;

import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.NormalizedVec2;
import physics2D.math.OrientedPoint;
import physics2D.math.Range;
import physics2D.math.Vec2;
import physics2D.math.Vertex2;
import physics2D.physics.DepthWithDirection;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import static java.lang.Math.min;
import static java.lang.Math.max;


public abstract class AbstractPolygon implements Shape {
	
	public final Vertex2[] vertexes;
	
	public AbstractPolygon(Vec2[] polygon) {
		this(Vertex2.convertToVertexes(polygon));
	}
	
	public AbstractPolygon(Vertex2[] vertexes) {
		this.vertexes = vertexes;
	}
	
	@Override
	public Vec2[] getDrawingVertexes(){
		Vertex2[] verts = getVertexes();
		Vec2[] drawingVertexes = new Vec2[verts.length];
		for(int i = 0; i < vertexes.length; i++)
			drawingVertexes[i] = verts[i].position;
		
		return drawingVertexes;
	}
	
	public Vertex2[] getVertexes(){
		return vertexes;
	}
	
	public Vec2[] getCorners(){
		Vec2[] poly = new Vec2[vertexes.length];
		for(int i = 0; i < vertexes.length; i++)
			poly[i] = vertexes[i].position;
		return poly;
	}
	
	@Override
	public boolean intersects(Shape other) {
		return intersectsUsingVertexContains(other);
		//return intersectsUsingSAT(other);
	}
	
	private boolean intersectsUsingVertexContains(Shape other){
		for(Vertex2 vertex:getVertexes())
			if(other.containsPoint(vertex.position))
				return true;
		return false;
	}
	
	/**
	 * Gets all normal vectors of all sides, and ranks them based on their 'score'
	 * 
	 * This score is calculated as follows: 
	 * 		score = -normalVec . point.orientation / depth
	 * 
	 * @param point
	 * @return the vector to the nearest surface
	 */
	@Override
	public DepthWithDirection getNormalVecAndDepthToSurface(Vec2 position, NormalizedVec2 orientation){
		Vertex2[] vertexes = getVertexes();
		
		double bestDepth = Double.NaN;
		NormalizedVec2 bestNormalVec = null;
		double bestScore = 0;
		
		for(int i = 0; i < vertexes.length; i++){
			// Vec2 normalizedSide = vertexes[(i+1) % vertexes.length].position.subtract(vertexes[i].position).normalize();
			NormalizedVec2 normalVec = vertexes[i].normalVec;
			double depth = normalVec.rotate90CounterClockwise().pointToLineDistance(position.subtract(vertexes[i].position));
			double score = -normalVec.dot(orientation) / depth;
			if(score > bestScore){
				bestScore = score;
				bestDepth = depth;
				bestNormalVec = normalVec;
			}
		}
		
		return new DepthWithDirection(bestNormalVec, bestDepth);
	}
	
	@Override
	public BoundingBox getBoundingBox(){
		Vertex2[] vertexes = getVertexes();
		
		double xmin=vertexes[0].position.x, ymin=vertexes[0].position.y;
		double xmax=xmin, ymax=ymin;
		
		for(int i = 1; i < vertexes.length; i++){
			xmin = min(xmin, vertexes[i].position.x);
			ymin = min(ymin, vertexes[i].position.y);
			xmax = max(xmax, vertexes[i].position.x);
			ymax = max(ymax, vertexes[i].position.y);
		}
		
		return new BoundingBox(xmin, ymin, xmax, ymax);
	}
	
	@Override
	public List<OrientedPoint> getIntersectionPoints(Shape other) {
		List<OrientedPoint> points = new ArrayList<OrientedPoint>();
		for(Vertex2 vert:getVertexes()){
			if(other.containsPoint(vert.position))
				points.add(new OrientedPoint(vert.position, vert.orientation));
		}
		return points;
	}
	
	/*public Vec2 getIntersectionPoint(Shape other){
		List<? extends ConvexPolygon> thisDecomp = convexDecomposition();
		List<? extends Convex> otherDecomp = other.convexDecomposition();
		for(ConvexPolygon p:thisDecomp){
			for(Convex o:otherDecomp){
				if(!p.getBoundingBox().intersects(o.getBoundingBox())) continue;
				
				Vec2[][] allDirs = {p.getSATDirections(), o.getSATDirections()};
				
				Vec2 bestVec = null;
				double bestWidth = Double.POSITIVE_INFINITY;
				
				for(Vec2[] directions:allDirs){
					for(Vec2 direction:directions){
						Range b1 = getBoundsAlongDirection(direction);
						Range b2 = other.getBoundsAlongDirection(direction);
						
						Vec2 axis = direction.rotate90CounterClockwise();
						
						Debug.logVector(axis.mul(b1.min).add(direction.mul(0.003)), axis.mul(b1.getWidth()), Color.GREEN);
						Debug.logVector(axis.mul(b2.min).add(direction.mul(-0.003)), axis.mul(b2.getWidth()), Color.ORANGE);
						
						if(b1.isDisjunct(b2))
							return null;
						
						Range intersect = b1.intersect(b2);
						
						
						
						Debug.logVector(axis.mul(intersect.min), axis.mul(intersect.getWidth()), Color.RED);
						
						//throw new NotImplementedException();
					}
				}
				
			}
		}
		
		return null;
	}*/
	
	@Override
	public boolean containsPoint(Vec2 point){
		boolean withinShape = false;
		
		for(int i = 0; i < vertexes.length; i++){
			Vec2 previous = vertexes[(vertexes.length + i - 1)%vertexes.length].position.subtract(point);
			Vec2 current = vertexes[i].position.subtract(point);
			Vec2 next = vertexes[(i+1)%vertexes.length].position.subtract(point);
			
			// from here on it is assumed 'point' lies at 0, 0 all coordinates are relative to this
			
			boolean goingUp = current.y <= 0 && next.y > 0;
			boolean goingDown = current.y >= 0 && next.y < 0;
			boolean intersectingCorner = current.y == 0;
			
			if(goingUp || goingDown) // check that line y=0 through point crosses this edge
				if(next.x*Math.abs(current.y) + current.x*Math.abs(next.y) >= 0) // check that edge lies to the right of point
					if(!intersectingCorner || previous.y * next.y <= 0)
						withinShape = !withinShape;
			
		}
		
		return withinShape;
	}
	
	@Override
	public AbstractPolygon transformToCFrame(CFrame frame){
		Vertex2[] newVertexes = new Vertex2[vertexes.length];
		
		for(int i = 0; i < vertexes.length; i++)
			newVertexes[i] = frame.localToGlobal(vertexes[i]);
		
		return new TransformedPolygon(newVertexes, getArea(), getInertialArea(), frame.localToGlobal(getCenterOfMass()));
	}
	
	@Override
	public AbstractPolygon scale(double factor){
		Vertex2[] newVertexes = new Vertex2[vertexes.length];
		
		for(int i = 0; i < vertexes.length; i++){
			Vertex2 v = vertexes[i];
			newVertexes[i] = new Vertex2(v.position.mul(factor), v.orientation, v.normalVec, v.edgeLength*factor, v.concave);
		}
		
		return new TransformedPolygon(newVertexes, getArea()*Math.pow(factor, 2), getInertialArea()*Math.pow(factor, 4), getCenterOfMass().mul(factor));
	}
	
	/**
	 * Applies <i>consumer</i> over all vertexes, giving the current, next and previous vertexes. 
	 * 
	 * @param consumer vertex consumer
	 */
	public void forEachVertex(VertexConsumer consumer){
		Vertex2 previous = vertexes[vertexes.length - 2];
		Vertex2 current = vertexes[vertexes.length - 1];
		
		for(int i = 0; i < vertexes.length; i++){
			Vertex2 next = vertexes[i];
			
			consumer.accept(previous, current, next);
			
			previous = current;
			current = next;
		}
	}
	
	@FunctionalInterface
	public static interface VertexConsumer {
		public void accept(Vertex2 previous, Vertex2 current, Vertex2 next);
	}
	
	@Override
	public Shape union(Shape other){
		if(other instanceof AbstractPolygon){
			return union((AbstractPolygon) other);
		}else{
			throw new NotImplementedException();
		}
	}
	
	public AbstractPolygon union(AbstractPolygon other){
		throw new NotImplementedException();
	}
	
	@Override
	public Range getBoundsAlongDirection(Vec2 direction){
		double value = direction.cross(vertexes[0].position);
		double min = value;
		double max = value;
		
		for(int i = 1; i < vertexes.length; i++){
			value = direction.cross(vertexes[i].position);
			min = Math.min(min, value);
			max = Math.max(max, value);
		}
		
		return new Range(min, max);
	}
	
	
	
	@Override
	public List<? extends ConvexPolygon> convexDecomposition(){
		ArrayList<Vec2[]> d = Polygon.convexDecomposition(getCorners());
		ArrayList<ConvexPolygon> convexDecomp = new ArrayList<ConvexPolygon>(d.size());
		for(int i = 0; i < d.size(); i++)
			convexDecomp.set(i, new ConvexPolygon(d.get(i)));
		
		return convexDecomp;
	}
	
	private static class TransformedPolygon extends AbstractPolygon {
		
		private final double area;
		private final double inertialArea;
		private final Vec2 centerOfMass;
		
		public TransformedPolygon(Vertex2[] polygon, double area, double inertialArea, Vec2 centerOfMass) {
			super(polygon);
			this.area = area;
			this.inertialArea = inertialArea;
			this.centerOfMass = centerOfMass;
		}

		@Override
		public double getArea() {
			return area;
		}

		@Override
		public double getInertialArea() {
			return inertialArea;
		}

		@Override
		public Vec2 getCenterOfMass() {
			return centerOfMass;
		}
	}
}
