package physics2D.math;

public class Range {
	public final double min, max;
	public Range(double min, double max){
		assert min <= max;
		this.min = min;
		this.max = max;
	}
	
	public boolean isDisjunct(Range other){
		return other.max < min || other.min > max;
	}
	
	public Range intersect(Range other){
		return new Range(Math.max(min, other.min), Math.min(max, other.max));
	}
	
	public boolean contains(Range other){
		return other.min > min && other.max < max;
	}
	
	public double getCenter(){
		return (min+max)/2;
	}
	
	public double getWidth(){
		return max-min;
	}
}
