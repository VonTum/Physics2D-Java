package game;

import geom.Rectangle;
import geom.Shape;
import geom.Triangle;
import math.CFrame;
import math.RotMat2;
import math.Vec2;
import physics.Box;
import physics.Constraint;
import physics.Physical;
import physics.PhysicalProperties;
import physics.PinConstraint;
import physics.World;
import util.Color;

public class ShowcaseWorld implements WorldBuilder {
	
	@Override
	public void build(World w) {
		PhysicalProperties basicProperties = new PhysicalProperties(10.0, 0.1, 0.0, new Color(0.8, 0.8, 0.6, 0.6));
		
		Shape s = new Rectangle(new CFrame(0.4, 0.8, 0.1), 0.3, 0.2);
		Shape s2 = new Rectangle(new CFrame(0.2, 0.6, 0.7), 0.1, 0.2);
		Shape s3 = new Rectangle(new CFrame(0.5, 0.6, 0.3), 0.3, 0.05);
		Physical physical = new Physical(s, s2, s3);
		w.addObject(physical);
		
		Shape blockShape = new Rectangle(new CFrame(-0.4, 1.2, 0.1), 0.3, 0.2);
		Physical block = new Physical(blockShape);
		w.addObject(block);
		
		
		
		w.addObject(ObjectLibrary.createBowl(new CFrame(-0.2, 1.0, 0.0), basicProperties));
		
		Triangle t = new Triangle(new CFrame(new Vec2(0.9, 1.2), 0), 0.3, new Vec2(0.2, 0.2));
		Physical trianglePhysical = new Physical(t);
		w.addObject(trianglePhysical);
		
		Box box = new Box(new Vec2(0.1, 1.0), RotMat2.IDENTITY, 0.3, 0.2, basicProperties);
		w.addObject(box);
		
		PhysicalProperties circleProperties = new PhysicalProperties(1.0, 0.05, 0.0, Color.RED.darker());
		Physical circleThing = new Physical(new CFrame(2.0, 2.0));
		
		int squareCount = 8;
		for(int i = 0; i < squareCount; i++)
			circleThing.addPart(new Rectangle(0.1, 0.1), new CFrame(Vec2.ZERO, new RotMat2(i*Math.PI/squareCount/2)), circleProperties);
		
		
		w.addObject(circleThing);
		
		Box b1 = new Box(new CFrame(-2.0, 0.5), 0.3, 0.1, basicProperties);
		Box b2 = new Box(new CFrame(-1.6, 0.5), 0.3, 0.3, basicProperties);
		Constraint link = new PinConstraint(b1, b2, new CFrame(0.2, 0.0, 0.0), new CFrame(-0.2, 0.0, 0.0));
		
		w.addObject(b1);
		w.addObject(b2);
		w.addConstraint(link);
		
		physical.angularVelocity = -1.0;
		physical.velocity = new Vec2(0.0, 0.0);
	}

}
