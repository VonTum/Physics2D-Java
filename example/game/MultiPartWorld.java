package game;

import game.util.Color;
import physics2D.geom.ConvexPolygon;
import physics2D.geom.Rectangle;
import physics2D.geom.Shape;
import physics2D.geom.Triangle;
import physics2D.math.CFrame;
import physics2D.math.RotMat2;
import physics2D.math.Vec2;
import physics2D.physics.Box;
import physics2D.physics.Physical;
import physics2D.physics.PhysicalProperties;
import physics2D.physics.World;

public class MultiPartWorld implements WorldBuilder {

	@Override
	public void build(World w) {
		PhysicalProperties basicProperties = new PhysicalProperties(10.0, 0.1, 0.0, new Color(0.8, 0.8, 0.6, 0.6));
		
		Shape s = new Rectangle(0.3, 0.2).transformToCFrame(new CFrame(0.4, 0.8, 0.1));
		Shape s2 = new Rectangle(0.1, 0.2).transformToCFrame(new CFrame(0.2, 0.6, 0.7));
		Shape s3 = new Rectangle(0.3, 0.05).transformToCFrame(new CFrame(0.5, 0.6, 0.3));
		Physical physical = new Physical(basicProperties, s, s2, s3);
		w.addObject(physical);
		
		Shape blockShape = new Rectangle(0.3, 0.2).transformToCFrame(new CFrame(-0.4, 1.2, 0.1));
		Physical block = new Physical(basicProperties, blockShape);
		w.addObject(block);
		
		// w.addObject(ObjectLibrary.createFloor(new CFrame(0.0, 0.0, 0.0), basicProperties));
		
		w.addObject(ObjectLibrary.createBowl(new CFrame(-0.2, 1.0, 0.0), basicProperties));
		
		ConvexPolygon t = new Triangle(new Vec2(-0.15, 0.0), new Vec2(0.15, 0.0), new Vec2(0.2, 0.2)).transformToCFrame(new CFrame(new Vec2(0.9, 1.2), 0));
		Physical trianglePhysical = new Physical(basicProperties, t);
		w.addObject(trianglePhysical);
		
		Box box = new Box(new Vec2(0.1, 1.0), RotMat2.IDENTITY, 0.3, 0.2, basicProperties);
		w.addObject(box);
		
		PhysicalProperties circleProperties = new PhysicalProperties(1.0, 0.05, 0.0, Color.RED.darker());
		Rectangle[] squares = new Rectangle[8];
		for(int i = 0; i < squares.length; i++){
			squares[i] = new Rectangle(new CFrame(new Vec2(2.0, 2.0), new RotMat2(i*Math.PI/squares.length/2)), 0.1, 0.1);
		}
		
		Physical circleThing = new Physical(circleProperties, squares);
		w.addObject(circleThing);
		
		physical.angularVelocity = -1.0;
		physical.velocity = new Vec2(0.0, 0.0);
	}
	
}
