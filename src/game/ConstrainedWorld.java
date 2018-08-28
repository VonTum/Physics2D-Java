package game;

import math.CFrame;
import geom.Box;
import physics.CoilPinConstraint;
import physics.Constraint;
import physics.PhysicalProperties;
import physics.PinConstraint;
import physics.World;
import util.Color;

public class ConstrainedWorld implements WorldBuilder {

	@Override
	public void build(World world) {
		PhysicalProperties properties = new PhysicalProperties(10.0, 0.05, 0.0, new Color(0.8, 0.8, 0.6, 0.6));
		
		Box b1 = new Box(new CFrame(-0.4, 0.5, 0.0), 0.3, 0.1, properties);
		Box b2 = new Box(new CFrame(0.0, 0.5, 0.0), 0.3, 0.3, properties);
		Box hangingBox = new Box(new CFrame(0.4, 0.8, 0.0), 0.1, 0.2, properties);
		Box hanging2 = new Box(new CFrame(0.6, 0.6, 0.0), 0.1, 0.1, properties);
		
		Constraint springPin = new CoilPinConstraint(b1, b2, new CFrame(0.2, 0.0, Math.PI/3), new CFrame(-0.2, 0.0, 0.0), 1.0);
		
		Constraint pin = new PinConstraint(b2, hangingBox, new CFrame(0.2, 0.5, 0.0), new CFrame(0.0, 0.15, 0.0));
		
		Constraint pin2 = new PinConstraint(hangingBox, hanging2, new CFrame(0.2, 0.0), new CFrame(0.0, 0.2));
		
		world.addObject(b1);
		world.addObject(b2);
		world.addObject(hangingBox);
		world.addObject(hanging2);
		world.addConstraint(springPin);
		world.addConstraint(pin);
		world.addConstraint(pin2);
	}
	
}
