package game;

import game.util.Color;
import physics2D.math.CFrame;
import physics2D.physics.PhysicalProperties;
import physics2D.physics.World;

public class MultiMassWorld implements WorldBuilder {
	
	@Override
	public void build(World w) {
		PhysicalProperties basicProperties = new PhysicalProperties(10.0, 0.05, 0.0, Color.DEFAULT_BRICK_COLOR);
		// w.addObject(ObjectLibrary.createFloor(new CFrame(0.0, 0.0, 0.0), basicProperties));
		w.addObject(ObjectLibrary.createHammer(new CFrame(0.0, 0.0, 0.0), new PhysicalProperties(1000.0, 0.05, 0.0, Color.DARK_GREY.alpha(0.6)), basicProperties));
	}

}
