package game;

import math.Mat2;
import math.RotMat2;
import math.Vec2;
import physics.Box;
import physics.PhysicalProperties;
import physics.World;
import util.Color;

public class BasicWorld implements WorldBuilder {

	@Override
	public void build(World w) {
		PhysicalProperties basicProperties = new PhysicalProperties(10.0, 0.05, 0.0, new Color(0.7, 0.7, 0.0, 0.8));
		
		Box floorBox = new Box(new Vec2(0.0, 0.0), Mat2.IDENTITY, 50.0, 0.3, basicProperties);
		Box longBox = new Box(new Vec2(0.5, 0.5), new RotMat2(1.5), 0.3, 0.1, basicProperties);
		Box longBox2 = new Box(new Vec2(-0.5, 0.5), new RotMat2(1.5), 0.3, 0.1, basicProperties);
		Box fatBox = new Box(new Vec2(0.0, 0.5), new RotMat2(-Math.PI/2), 0.5, 0.3, basicProperties);
		Box littleSquare = new Box(new Vec2(0.15, 0.9), new RotMat2(0.1), 0.1, 0.1, basicProperties);
		Box hugeLongBox = new Box(new Vec2(0.25, 1.1), new RotMat2(-0.3), 1.0, 0.2, basicProperties);
		Box massiveBlock = new Box(new Vec2(12.0, 12.0), Mat2.IDENTITY, 20.0, 20.0, basicProperties);
		
		w.addObject(floorBox);
		w.addObject(hugeLongBox);
		w.addObject(fatBox);
		w.addObject(longBox);
		w.addObject(longBox2);
		w.addObject(littleSquare);
		w.addObject(massiveBlock);
		
		floorBox.anchor();
		
		fatBox.angularVelocity = 0.7;
	}

}
