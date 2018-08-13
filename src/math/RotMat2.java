package math;

import static java.lang.Math.sin;
import static java.lang.Math.cos;


public class RotMat2 extends Mat2 {
	public RotMat2(double theta) {
		super(cos(theta), -sin(theta), sin(theta), cos(theta));
	}
	
	private RotMat2(double a, double b, double c, double d){
		super(a, b, c, d);
	}
	
	public NormalizedVec2 mul(NormalizedVec2 v){
		return new NormalizedVec2(a*v.x+b*v.y, c*v.x+d*v.y);
	}
	
	public RotMat2 mul(RotMat2 o){
		/*
		 * a b * A B
		 * c d   C D
		 * = a*A+b*C a*B+b*D
		 *   c*A+d*C c*B+d*D
		 */
		
		return new RotMat2(	a*o.a+b*o.c, a*o.b+b*o.d, 
							c*o.a+d*o.c, c*o.b+d*o.d);
	}
	
	public double getAngle(){
		return Math.atan2(c, a);
	}
	
	public static RotMat2 fromNormalizedVecAsXAxe(NormalizedVec2 vec){
		return new RotMat2(vec.x, -vec.y, vec.y, vec.x);
	}
	
	public static RotMat2 fromNormalizedVecAsYAxe(NormalizedVec2 vec){
		return new RotMat2(vec.y, vec.x, -vec.x, vec.y);
	}
	
	@Override
	public RotMat2 inv() {
		return new RotMat2(d, -b, -c, a);
	}
	
	@Override
	public double det() {
		return 1.0;
	}
	
	public NormalizedVec2 getOrientation(){
		return new NormalizedVec2(a, b);
	}
}
