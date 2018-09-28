package physics2D.math;

import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.abs;


public class NormalizedVec2 extends Vec2 {
	
	public NormalizedVec2(double x, double y) {
		super(x, y);
		assert abs(x*x+y*y - 1.0) < 1E-15;
	}
	
	public NormalizedVec2(double theta){
		super(cos(theta), sin(theta));
	}
	
	@Override
	public double length() {
		return 1.0;
	}

	@Override
	public double lengthSquared() {
		return 1.0;
	}

	@Override
	public NormalizedVec2 normalize() {
		return this;
	}
	
	@Override
	public boolean isLongerThan(double length) {
		return 1.0 > length;
	}
	
	@Override
	public boolean isShorterThan(double length) {
		return 1.0 < length;
	}

	@Override
	public double pointToLineDistance(Vec2 point) {
		return Math.abs(point.cross(this));
	}

	@Override
	public double pointToLineDistanceSquared(Vec2 point) {
		double crossProd = point.cross(this);
		return crossProd*crossProd;
	}
	
	@Override
	public NormalizedVec2 rotate90Clockwise(){
		return new NormalizedVec2(y, -x);
	}
	
	@Override
	public NormalizedVec2 rotate90CounterClockwise(){
		return new NormalizedVec2(-y, x);
	}
}
