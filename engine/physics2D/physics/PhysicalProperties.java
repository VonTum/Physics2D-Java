package physics2D.physics;

import game.util.Color;

public class PhysicalProperties {
	
	public final double density;
	private final double friction, stickyness;
	public final Color color;
	
	public PhysicalProperties(double density, double friction, double stickyness, Color color){
		this.density = density;
		this.friction = friction;
		this.stickyness = stickyness;
		this.color = color;
	}
	
	public PhysicalProperties(double density){
		this(density, 0.05, 0.0, Color.DEFAULT_BRICK_COLOR);
	}
	
	public double getFrictionWith(PhysicalProperties other){
		return this.friction*other.friction;
	}
	
	public double getStickynessWith(PhysicalProperties other){
		return this.stickyness*other.stickyness;
	}
	
	public PhysicalProperties withDensity(double density){return new PhysicalProperties(density, friction, stickyness, color);}
	public PhysicalProperties withFriction(double friction){return new PhysicalProperties(density, friction, stickyness, color);}
	public PhysicalProperties withStickyness(double stickyness){return new PhysicalProperties(density, friction, stickyness, color);}
	public PhysicalProperties withColor(Color color){return new PhysicalProperties(density, friction, stickyness, color);}
}
