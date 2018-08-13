package game;

import geom.Rectangle;
import geom.Shape;
import math.CFrame;
import math.Mat2;
import math.RotMat2;
import physics.Physical;
import physics.PhysicalProperties;
import physics.World;
import util.Color;

public class MultiMassWorld implements WorldBuilder {
	
	@Override
	public void build(World w) {
		PhysicalProperties basicProperties = new PhysicalProperties(10.0, 0.05, 0.0, Color.DEFAULT_BRICK_COLOR);
		Shape floorShape = new Rectangle(basicProperties, new CFrame(0.0, 0.0, 0.0), 20.0, 0.2);
		Physical floor = new Physical(floorShape);
		floor.anchor();
		w.addObject(floor);
		
		Shape heavyCube = new Rectangle(new PhysicalProperties(1000.0, 0.05, 0.0, Color.DARK_GREY.alpha(0.6)), new CFrame(0.0, 0.5, RotMat2.rotTransform(Math.PI/4)), 0.1, 0.1);
		Shape veryLightArm = new Rectangle(basicProperties, new CFrame(-0.3, 0.5, Mat2.IDENTITY), 0.2, 0.03);
		Physical testObj = new Physical(heavyCube, veryLightArm);
		w.addObject(testObj);
	}

}
