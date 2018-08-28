package game;

import math.CFrame;
import math.RotMat2;
import math.Vec2;
import geom.Box;
import geom.Shape;
import geom.Rectangle;
import geom.Triangle;
import physics.Physical;
import physics.PhysicalProperties;
import physics.World;
import util.Color;

public class MultiPartWorld implements WorldBuilder {

	@Override
	public void build(World w) {
		PhysicalProperties basicProperties = new PhysicalProperties(10.0, 0.1, 0.0, new Color(0.8, 0.8, 0.6, 0.6));
		
		Shape s = new Rectangle(basicProperties, new CFrame(0.4, 0.8, 0.1), 0.3, 0.2);
		Shape s2 = new Rectangle(basicProperties, new CFrame(0.2, 0.6, 0.7), 0.1, 0.2);
		Shape s3 = new Rectangle(basicProperties, new CFrame(0.5, 0.6, 0.3), 0.3, 0.05);
		Physical physical = new Physical(s, s2, s3);
		w.addObject(physical);
		
		Shape blockShape = new Rectangle(basicProperties, new CFrame(-0.4, 1.2, 0.1), 0.3, 0.2);
		Physical block = new Physical(blockShape);
		w.addObject(block);
		
		// w.addObject(ObjectLibrary.createFloor(new CFrame(0.0, 0.0, 0.0), basicProperties));
		
		w.addObject(ObjectLibrary.createBowl(new CFrame(-0.2, 1.0, 0.0), basicProperties));
		
		Triangle t = new Triangle(basicProperties, new CFrame(new Vec2(0.9, 1.2), 0), 0.3, new Vec2(0.2, 0.2));
		Physical trianglePhysical = new Physical(t);
		w.addObject(trianglePhysical);
		
		Box box = new Box(new Vec2(0.1, 1.0), RotMat2.IDENTITY, 0.3, 0.2, basicProperties);
		w.addObject(box);
		
		PhysicalProperties circleProperties = new PhysicalProperties(1.0, 0.05, 0.0, Color.RED.darker());
		Rectangle[] squares = new Rectangle[8];
		for(int i = 0; i < squares.length; i++){
			squares[i] = new Rectangle(circleProperties, new CFrame(new Vec2(2.0, 2.0), new RotMat2(i*Math.PI/squares.length/2)), 0.1, 0.1);
		}
		
		Physical circleThing = new Physical(squares);
		w.addObject(circleThing);
		
		physical.angularVelocity = -1.0;
		physical.velocity = new Vec2(0.0, 0.0);
	}
	
}
