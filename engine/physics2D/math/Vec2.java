package physics2D.math;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

public class Vec2 {
	public static final Vec2 ZERO = new Vec2(0, 0);
	public static final NormalizedVec2 UNITX = new NormalizedVec2(1.0, 0.0);
	public static final NormalizedVec2 UNITY = new NormalizedVec2(0.0, 1.0);
	public static final NormalizedVec2 UNITNEGX = new NormalizedVec2(-1.0, 0.0);
	public static final NormalizedVec2 UNITNEGY = new NormalizedVec2(0.0, -1.0);
	
	public final double x, y;
	
	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vec2 add(Vec2 other){
		return add(other.x, other.y);
	}
	
	public Vec2 add(double dx, double dy){
		return new Vec2(x+dx, y+dy);
	}
	
	public Vec2 subtract(Vec2 other){
		return subtract(other.x, other.y);
	}
	
	public Vec2 subtract(double dx, double dy){
		return new Vec2(x-dx, y-dy);
	}
	
	public Vec2 neg(){
		return new Vec2(-x, -y);
	}
	
	public static Vec2 sum(Vec2... vecs){
		double x = 0, y = 0;
		for(Vec2 v:vecs){
			x += v.x;
			y += v.y;
		}
		
		return new Vec2(x, y);
	}
	
	public Vec2 mul(double d){
		return new Vec2(x*d, y*d);
	}
	
	public Vec2 div(double d){
		return new Vec2(x/d, y/d);
	}
	
	public Vec2 mulXY(double xd, double yd){
		return new Vec2(x*xd, y*yd);
	}
	
	public Vec2 divXY(double xd, double yd){
		return new Vec2(x/xd, y/yd);
	}
	
	public double length(){
		return sqrt(lengthSquared());
	}
	
	public double lengthSquared(){
		return x*x+y*y;
	}
	
	public double dot(Vec2 other){
		return this.x*other.x+this.y*other.y;
	}
	
	public double cross(Vec2 other){
		return this.x*other.y-this.y*other.x;
	}
	
	public Vec2 cross(double z){
		return new Vec2(y*z, -x*z);
	}
	
	public double distanceTo(Vec2 other){
		return this.subtract(other).length();
	}
	
	/**
	 * @return the theta of this vector if it were in polar coordinates
	 */
	public double getTheta(){
		return atan2(y, x);
	}
	
	public static Vec2 fromPolar(double r, double theta){
		return new Vec2(r*cos(theta), r*sin(theta));
	}
	
	public Vec2 withLength(double newLength){
		double factor = newLength/length();
		return new Vec2(x*factor, y*factor);
	}
	
	public Vec2 maxLength(double maxLength){
		double lengthSquared = lengthSquared();
		if(maxLength*maxLength < lengthSquared){
			double f = maxLength / length();
			return new Vec2(x*f, y*f);
		}else
			return this;
	}
	
	public Vec2 minLength(double maxLength){
		double lengthSquared = lengthSquared();
		if(maxLength*maxLength > lengthSquared){
			double f = maxLength / length();
			return new Vec2(x*f, y*f);
		}else
			return this;
	}
	
	public NormalizedVec2 normalize(){
		double length = length();
		return new NormalizedVec2(x/length, y/length);
	}
	
	public double angleBetween(Vec2 other){
		return Math.acos(this.normalize().dot(other.normalize()));
	}
	
	public boolean isLongerThan(double length){
		return lengthSquared() > length*length;
	}
	
	public boolean isShorterThan(double length){
		return lengthSquared() < length*length;
	}
	
	public Vec2 rotate90Clockwise(){
		return new Vec2(y, -x);
	}
	
	public Vec2 rotate90CounterClockwise(){
		return new Vec2(-y, x);
	}
	
	/**
	 * returns the distance of the given point to the line that goes through the origin along this vector
	 * @param point
	 * @return the distance
	 */
	public double pointToLineDistance(Vec2 point){
		return Math.abs(point.cross(this))/length();
	}
	
	/**
	 * returns the squared distance of the given point to the line that goes through the origin along this vector
	 * @param point
	 * @return the square of the distance
	 */
	public double pointToLineDistanceSquared(Vec2 point){
		double crossProd = point.cross(this);
		return crossProd*crossProd/lengthSquared();
	}
	
	public static Vec2 bisect(Vec2 a, Vec2 b){
		return a.mul(b.length()).add(b.mul(a.length()));
	}
	
	public static Vec2 getIntersection(Vec2 origin1, Vec2 vec1, Vec2 origin2, Vec2 vec2){
		double a = origin1.subtract(origin2).cross(vec1) / vec2.cross(vec1);
		return origin2.add(vec2.mul(a));
	}
	
	@Override
	public String toString(){
		return String.format("{x: %.9f, y: %.9f}", x, y);
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof Vec2){
			Vec2 oVec = (Vec2) other;
			return this.x == oVec.x && this.y == oVec.y;
		}else
			return false;
	}
	
	@Override
	public int hashCode(){
		long bits = Double.doubleToLongBits(this.x)+Double.doubleToLongBits(this.y);
		return (int) (bits >> 32 + bits);
	}
}
