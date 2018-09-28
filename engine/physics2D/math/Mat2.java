package physics2D.math;


public class Mat2 {
	public static final Mat2 ZERO = new Mat2(0, 0, 0, 0);
	public static final RotMat2 IDENTITY = new RotMat2(0);
	//public static final RotMat2 CLOCKWISE90 = new RotMat2(-Math.PI/2);
	// public static final RotMat2 COUNTERCLOCKWISE90 = new RotMat2(Math.PI/2);
	
	public final double a, b, c, d;
	
	// a b
	// c d
	
	public Mat2(double a, double b, double c, double d){
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	
	public static RotMat2 rotTransform(double angle){
		/*double cosa = cos(angle);
		double sina = sin(angle);
		return new RotMat2(	cosa, -sina, 
							sina, cosa);*/
		return new RotMat2(angle);
	}
	
	public Mat2 mul(double f){
		return new Mat2(a*f, b*f, c*f, d*f);
	}
	
	public Mat2 mul(Mat2 o){
		/*
		 * a b * A B
		 * c d   C D
		 * = a*A+b*C a*B+b*D
		 *   c*A+d*C c*B+d*D
		 */
		
		return new Mat2(a*o.a+b*o.c, a*o.b+b*o.d, 
						c*o.a+d*o.c, c*o.b+d*o.d);
	}
	
	public double det(){
		return a*d-b*c;
	}
	
	public Mat2 add(Mat2 other){
		return new Mat2(this.a+other.a, this.b+other.b, this.c+other.c, this.d+other.d);
	}
	
	public Mat2 subtract(Mat2 other){
		return new Mat2(this.a-other.a, this.b-other.b, this.c-other.c, this.d-other.d);
	}
	
	public Mat2 inv(){
		return new Mat2(d, -b, -c, a).mul(1/det());
	}
	
	public Vec2 mul(Vec2 v){
		return new Vec2(a*v.x+b*v.y, c*v.x+d*v.y);
	}
	
	@Override
	public String toString(){
		return "["+a+","+b+","+c+","+d+"]";
	}
}
