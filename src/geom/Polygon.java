package geom;

import java.util.Arrays;
import java.util.stream.Stream;

import physics.DepthWithDirection;
import physics.PhysicalProperties;
import math.BoundingBox;
import math.CFrame;
import math.NormalizedVec2;
import math.OrientedPoint;
import math.Vec2;
import math.Vertex2;
import static java.lang.Math.min;
import static java.lang.Math.max;


public abstract class Polygon extends Shape {
	
	public final Vertex2[] vertexes;
	
	public Polygon(PhysicalProperties properties, CFrame cframe, Vec2[] polygon) {
		this(properties, cframe, Vertex2.convertToVertexes(polygon));
	}
	
	public Polygon(PhysicalProperties properties, CFrame cframe, Vertex2[] vertexes) {
		super(properties, cframe);
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
		CFrame cframe = getCFrame();
		
		Vertex2[] newVertexes = new Vertex2[vertexes.length];
		
		for(int i = 0; i < vertexes.length; i++)
			newVertexes[i] = cframe.localToGlobal(vertexes[i]);
		
		
		return newVertexes;
	}
	
	@Override
	public boolean intersects(Shape other) {
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
	public Stream<? extends OrientedPoint> getIntersectionPoints(Shape other) {
		return Arrays.stream(getVertexes()).filter((vertex) -> {
			return other.containsPoint(vertex.position);
		});
	}
}
