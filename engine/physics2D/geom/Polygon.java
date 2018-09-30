package physics2D.geom;

import game.util.Color;

import java.util.Arrays;
import java.util.stream.Stream;

import physics2D.Debug;
import physics2D.math.BoundingBox;
import physics2D.math.CFrame;
import physics2D.math.NormalizedVec2;
import physics2D.math.OrientedPoint;
import physics2D.math.Range;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;
import physics2D.math.Vertex2;
import physics2D.physics.DepthWithDirection;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import static java.lang.Math.min;
import static java.lang.Math.max;


public abstract class Polygon extends Shape {
	
	public final Vertex2[] vertexes;
	
	public Polygon(Vec2[] polygon) {
		this(Vertex2.convertToVertexes(polygon));
	}
	
	public Polygon(Vertex2[] vertexes) {
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
		//return intersectsUsingVertexContains(other);
		return intersectsUsingSAT(other);
	}
	
	private boolean intersectsUsingVertexContains(Shape other){
		for(Vertex2 vertex:getVertexes())
			if(other.containsPoint(vertex.position))
				return true;
		return false;
	}
	
	private boolean intersectsUsingSweptShapes(Shape other){
		CollisionOutline outline = getCollisionOutline(other);
		return outline.asPolygon().containsPoint(Vec2.ZERO);
	}
	
	private boolean intersectsUsingSAT(Shape other){
		for(NormalizedVec2 direction:getSATDirections())
			if(getBoundsAlongDirection(direction).isDisjunct(other.getBoundsAlongDirection(direction)))
				return false;
		for(NormalizedVec2 direction:other.getSATDirections())
			if(getBoundsAlongDirection(direction).isDisjunct(other.getBoundsAlongDirection(direction)))
				return false;
		return true;
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
	public Stream<? extends OrientedPoint> getIntersectionPoints(Shape other) {
		return Arrays.stream(getVertexes()).filter((vertex) -> {
			return other.containsPoint(vertex.position);
		});
	}
	
	public Vec2 getIntersectionPoint(Shape other){
		NormalizedVec2[][] allDirs = {getSATDirections(), other.getSATDirections()};
		
		NormalizedVec2 bestVec = null;
		double bestWidth = Double.POSITIVE_INFINITY;
		
		for(NormalizedVec2[] directions:allDirs){
			for(NormalizedVec2 direction:directions){
				Range b1 = getBoundsAlongDirection(direction);
				Range b2 = other.getBoundsAlongDirection(direction);
				
				NormalizedVec2 axis = direction.rotate90CounterClockwise();
				
				Debug.logVector(axis.mul(b1.min).add(direction.mul(0.003)), axis.mul(b1.getWidth()), Color.GREEN);
				Debug.logVector(axis.mul(b2.min).add(direction.mul(-0.003)), axis.mul(b2.getWidth()), Color.ORANGE);
				
				if(b1.isDisjunct(b2))
					return null;
				
				Range intersect = b1.intersect(b2);
				
				
				
				Debug.logVector(axis.mul(intersect.min), axis.mul(intersect.getWidth()), Color.RED);
				
				//throw new NotImplementedException();
			}
		}
		
		return null;
	}
	
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
	public Polygon transformToCFrame(CFrame frame){
		Vertex2[] newVertexes = new Vertex2[vertexes.length];
		
		for(int i = 0; i < vertexes.length; i++)
			newVertexes[i] = frame.localToGlobal(vertexes[i]);
		
		return new TransformedPolygon(newVertexes, getArea(), getInertialArea(), frame.localToGlobal(getCenterOfMass()));
	}
	
	@Override
	public Polygon scale(double factor){
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
	public CollisionOutline getCollisionOutline(Shape other) {
		if(!(other instanceof Polygon))
			throw new NotImplementedException();
		
		Debug.logShape(other, Color.GREEN.alpha(0.4));
		Debug.logShape(this, Color.BLUE.alpha(0.4));
		
		
		Polygon o = (Polygon) other;
		
		Vertex2 ref = vertexes[0];
		
		NormalizedVec2 edgeRightNormal = vertexes[vertexes.length-1].normalVec;
		NormalizedVec2 edgeLeftNormal = ref.normalVec;
		
		// Debug.logVector(ref.position, edgeRightNormal);
		// Debug.logVector(ref.position, edgeLeftNormal);
		
		Debug.logPoint(o.getCenterOfMass());
		
		Vertex2 best = null;
		int bestI = -1;
		
		Vertex2 previous = o.vertexes[o.vertexes.length - 1];
		
		for(int i = 0; i < o.vertexes.length; i++){
			Vertex2 current = o.vertexes[i];
			
			NormalizedVec2 rightNormal = previous.normalVec;
			NormalizedVec2 leftNormal = current.normalVec;
			
			//Debug.logVector(current.position, rightNormal, Color.GREEN);
			//Debug.logVector(current.position, leftNormal, Color.GREEN);
			
			if(edgeLeftNormal.cross(rightNormal) > 0 && edgeRightNormal.cross(leftNormal) < 0){
				// Debug.logPoint(current.position, Color.RED);
				best = current;
				bestI = i;
			}
			
			previous = current;
		}
		
		// Debug.logPoint(best.position, Color.YELLOW);
		
		Vec2 delta = other.getCenterOfMass().add(ref.position.subtract(best.position));
		
		Debug.logShape(other.transformToCFrame(new CFrame(delta.subtract(o.getCenterOfMass()), RotMat2.IDENTITY)), Color.YELLOW.alpha(0.1), Color.YELLOW.alpha(0.6));
		
		Vec2[] outline = new Vec2[vertexes.length+o.vertexes.length];
		Vec2[] referencePoints = new Vec2[vertexes.length+o.vertexes.length];
		
		outline[0] = delta;
		referencePoints[0] = ref.position;
		
		
		//Debug.logVector(ref.position, best.normalVec, Color.RED);
		//Debug.logVector(ref.position, ref.normalVec, Color.BLUE);
		
		int thisI = 0;
		int otherI = bestI;
		
		Vertex2 thisCur = ref;
		Vertex2 otherCur = best;
		
		
		
		for(int i = 1; i < vertexes.length+o.vertexes.length; i++){
			Vec2 d;
			if(otherCur.normalVec.cross(thisCur.normalVec) > 0){
				// slide over thisCur
				thisI = (thisI+1) % vertexes.length;
				Vertex2 next = vertexes[thisI];
				d = next.position.subtract(thisCur.position);
				thisCur = next;
			}else{
				// slide over otherCur
				otherI = (otherI+1) % o.vertexes.length;
				Vertex2 next = o.vertexes[otherI];
				d = otherCur.position.subtract(next.position);
				otherCur = next;
			}
			delta = delta.add(d);
			outline[i] = delta;
			referencePoints[i] = thisCur.position;
			
			
			
			//Debug.logVector(thisCur.position, otherCur.normalVec, Color.RED);
			//Debug.logVector(thisCur.position, thisCur.normalVec, Color.BLUE);
			
			//Debug.logShape(other.transformToCFrame(new CFrame(delta.subtract(o.getCenterOfMass()).subtract(d.mul(1.0/4)), RotMat2.IDENTITY)), Color.YELLOW.alpha(0.1), Color.YELLOW.alpha(0.6));
			//Debug.logShape(other.transformToCFrame(new CFrame(delta.subtract(o.getCenterOfMass()).subtract(d.mul(2.0/4)), RotMat2.IDENTITY)), Color.YELLOW.alpha(0.1), Color.YELLOW.alpha(0.6));
			//Debug.logShape(other.transformToCFrame(new CFrame(delta.subtract(o.getCenterOfMass()).subtract(d.mul(3.0/4)), RotMat2.IDENTITY)), Color.YELLOW.alpha(0.1), Color.YELLOW.alpha(0.6));
			Debug.logShape(other.transformToCFrame(new CFrame(delta.subtract(o.getCenterOfMass()), RotMat2.IDENTITY)), Color.YELLOW.alpha(0.1), Color.YELLOW.alpha(0.6));
		}
		
		Debug.logPolygon(Color.TRANSPARENT, outline);
		
		for(int i = 0; i < referencePoints.length; i++)
			Debug.logPoint(referencePoints[i], Color.GREEN);
		
		return new CollisionOutline(outline, referencePoints);
	}
	
	@Override
	public Shape union(Shape other){
		if(other instanceof Polygon){
			return union((Polygon) other);
		}else{
			throw new NotImplementedException();
		}
	}
	
	public Polygon union(Polygon other){
		throw new NotImplementedException();
	}
	
	@Override
	public Range getBoundsAlongDirection(NormalizedVec2 direction){
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
	public NormalizedVec2[] getSATDirections(){
		NormalizedVec2[] directions = new NormalizedVec2[vertexes.length];
		for(int i = 0; i < vertexes.length; i++)
			directions[i] = vertexes[i].normalVec.rotate90CounterClockwise();
		
		return directions;
	}
	
	private static class TransformedPolygon extends Polygon {
		
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

		@Override
		public Shape leftSlice(Vec2 origin, Vec2 direction) {
			throw new NotImplementedException();
		}
	}
}
