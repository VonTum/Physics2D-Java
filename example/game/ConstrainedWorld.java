package game;

import game.util.Color;
import physics2D.math.CFrame;
import physics2D.physics.Box;
import physics2D.physics.CoilPinConstraint;
import physics2D.physics.Constraint;
import physics2D.physics.PhysicalProperties;
import physics2D.physics.PinConstraint;
import physics2D.physics.SpringPinConstraint;
import physics2D.physics.World;

public class ConstrainedWorld implements WorldBuilder {

	@Override
	public void build(World world) {
		PhysicalProperties properties = new PhysicalProperties(10.0, 0.05, 0.0, new Color(0.8, 0.8, 0.6, 0.6));
		
		Box b1 = new Box(new CFrame(-0.4, 0.5, 0.0), 0.3, 0.1, properties);
		Box b2 = new Box(new CFrame(0.0, 0.5, 0.0), 0.3, 0.3, properties.withDensity(10));
		Box hangingBox = new Box(new CFrame(0.4, 0.8, 0.0), 0.1, 0.2, properties);
		Box hanging2 = new Box(new CFrame(0.6, 0.6, 0.0), 0.1, 0.1, properties);
		
		// b2.anchor();
		
		Constraint springPin = new CoilPinConstraint(b1, b2, new CFrame(0.2, 0.0, Math.PI/3), new CFrame(-0.2, 0.0, 0.0), 1.0);
		
		Constraint pin = new PinConstraint(b2, hangingBox, new CFrame(0.2, 0.5, 0.0), new CFrame(0.0, 0.15, 0.0));
		
		Constraint pin2 = new SpringPinConstraint(hangingBox, hanging2, new CFrame(0.2, 0.0), new CFrame(0.0, 0.2), 20);
		
		world.addObject(b1);
		world.addObject(b2);
		world.addObject(hangingBox);
		world.addObject(hanging2);
		world.addConstraint(springPin);
		world.addConstraint(pin);
		world.addConstraint(pin2);
	}
	
}
