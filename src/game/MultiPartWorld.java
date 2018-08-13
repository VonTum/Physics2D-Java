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
		PhysicalProperties basicProperties = new PhysicalProperties(10.0, 0.05, 0.0, new Color(0.8, 0.8, 0.6, 0.6));
		
		Shape s = new Rectangle(basicProperties, new CFrame(0.4, 0.8, 0.1), 0.3, 0.2);
		Shape s2 = new Rectangle(basicProperties, new CFrame(0.2, 0.6, 0.7), 0.1, 0.2);
		Shape s3 = new Rectangle(basicProperties, new CFrame(0.5, 0.6, 0.3), 0.3, 0.05);
		Physical physical = new Physical(s, s2, s3);
		w.addObject(physical);
		
		Shape blockShape = new Rectangle(basicProperties, new CFrame(-0.4, 1.2, 0.1), 0.3, 0.2);
		Physical block = new Physical(blockShape);
		w.addObject(block);
		
		Shape floorShape = new Rectangle(basicProperties, new CFrame(0.0, 0.0, 0.0), 20.0, 0.2);
		Physical floor = new Physical(floorShape);
		w.addObject(floor);
		
		RotMat2 rotMat = new RotMat2(0.7);
		Shape bowlLeft = new Rectangle(basicProperties, new CFrame(rotMat.inv().mul(new Vec2(0, -0.4)), -0.7), 0.2, 0.05);
		Shape bowlBottom = new Rectangle(basicProperties, new CFrame(new Vec2(0, -0.4), 0.0), 0.2, 0.05);
		Shape bowlRight = new Rectangle(basicProperties, new CFrame(rotMat.mul(new Vec2(0, -0.4)), 0.7), 0.2, 0.05);
		
		Physical bowl = new Physical(bowlBottom, bowlLeft, bowlRight);
		bowl.cframe.move(new Vec2(-0.2, 1.0));
		w.addObject(bowl);
		
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
		
		floor.anchor();
		
		physical.angularVelocity = -1.0;
		physical.velocity = new Vec2(0.0, 0.0);
	}
	
}
