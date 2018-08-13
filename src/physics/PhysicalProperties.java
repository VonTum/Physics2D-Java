package physics;

import util.Color;

public class PhysicalProperties {
	
	public final double density, friction, stickyness;
	public final Color color;
	
	public PhysicalProperties(double density, double friction, double stickyness, Color color){
		this.density = density;
		this.friction = friction;
		this.stickyness = stickyness;
		this.color = color;
	}
}
