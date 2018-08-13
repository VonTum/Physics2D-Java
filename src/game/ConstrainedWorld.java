package game;

import math.CFrame;
import geom.Box;
import geom.Shape;
import geom.Rectangle;
import physics.Constraint;
import physics.Physical;
import physics.PhysicalProperties;
import physics.PinConstraint;
import physics.World;
import util.Color;

public class ConstrainedWorld implements WorldBuilder {

	@Override
	public void build(World world) {
		PhysicalProperties properties = new PhysicalProperties(10.0, 0.05, 0.0, new Color(0.8, 0.8, 0.6, 0.6));
		
		Shape floorShape = new Rectangle(properties, new CFrame(0.0, -0.1, 0.0), 20.0, 0.2);
		Physical floor = new Physical(floorShape);
		world.addObject(floor);
		
		floor.anchor();
		
		Box b1 = new Box(new CFrame(0.0, 0.5, 0.0), 0.3, 0.1, properties);
		Box b2 = new Box(new CFrame(0.4, 0.5, 0.0), 0.3, 0.3, properties);
		
		Constraint pin = new PinConstraint(b1, b2, new CFrame(0.2, 0.0, 0.0), new CFrame(-0.2, 0.0, 0.0), 200);
		
		world.addObject(b1);
		world.addObject(b2);
		world.addConstraint(pin);
	}
	
}
