package game;

import math.CFrame;
import physics.PhysicalProperties;
import physics.World;
import util.Color;

public class MultiMassWorld implements WorldBuilder {
	
	@Override
	public void build(World w) {
		PhysicalProperties basicProperties = new PhysicalProperties(10.0, 0.05, 0.0, Color.DEFAULT_BRICK_COLOR);
		w.addObject(ObjectLibrary.createFloor(new CFrame(0.0, 0.0, 0.0), basicProperties));
		w.addObject(ObjectLibrary.createHammer(new CFrame(0.0, 0.0, 0.0), new PhysicalProperties(1000.0, 0.05, 0.0, Color.DARK_GREY.alpha(0.6)), basicProperties));
	}

}
