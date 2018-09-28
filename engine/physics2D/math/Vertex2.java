package physics2D.math;



public class Vertex2 extends OrientedPoint {
	/**
	 * normal vector of the side between this and the next vertex
	 */
	public final NormalizedVec2 normalVec;
	public final double edgeLength;
	public final boolean concave;
	
	public Vertex2(Vec2 position, NormalizedVec2 orientation, NormalizedVec2 normalVec, double edgeLength, boolean concave){
		super(position, orientation);
		this.normalVec = normalVec;
		this.edgeLength = edgeLength;
		this.concave = concave;
	}
	
	/**
	 * Constructs an OrientedPosition out of the given position, and the previous and next positions
	 * 
	 * The orientation will be the bisector of the two adjacent vertexes, normalized and facing outward
	 * 
	 * @param position
	 * @param previous
	 * @param next
	 */
	public static Vertex2 fromBisector(Vec2 previous, Vec2 position, Vec2 next){
		Vec2 deltaForward = next.subtract(position);
		Vec2 deltaBackward = previous.subtract(position);
		
		Vec2 bisector = Vec2.bisect(deltaForward, deltaBackward);
		
		boolean isConcave = false;
		if(deltaForward.cross(deltaBackward) > 0){
			bisector = bisector.neg();
			isConcave = true;
		}
		
		return new Vertex2(position, bisector.normalize(), deltaForward.rotate90Clockwise().normalize(), deltaForward.length(), isConcave);
	}
	
	public static Vertex2[] convertToVertexes(Vec2... polygon){
		int size = polygon.length;
		Vertex2[] vertexes = new Vertex2[size];
		
		vertexes[0] = fromBisector(polygon[size-1], polygon[0], polygon[1]);
		for(int i = 1; i < size - 1; i++)
			vertexes[i] = fromBisector(polygon[i-1], polygon[i], polygon[i+1]);
		
		vertexes[size-1] = fromBisector(polygon[size-2], polygon[size-1], polygon[0]);
		
		return vertexes;
	}
}
