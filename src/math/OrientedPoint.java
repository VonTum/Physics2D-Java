package math;

public class OrientedPoint {
	public final Vec2 position;
	public final NormalizedVec2 orientation;
	
	public OrientedPoint(Vec2 position, NormalizedVec2 orientation){
		this.position = position;
		this.orientation = orientation;
	}
	
	@Override
	public String toString(){
		return "OrientedPoint(" + position + ", " + orientation + ")";
	}
}
