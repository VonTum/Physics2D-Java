package game;

import geom.Box;
import math.CFrame;
import physics.PhysicalProperties;
import physics.World;
import util.Color;

public class FluidWorld implements WorldBuilder {
	
	private final int pyramidSize;
	
	public FluidWorld(int pyramidSize) {
		this.pyramidSize = pyramidSize;
	}
	
	@Override
	public void build(World w) {
		PhysicalProperties properties = new PhysicalProperties(10.0).withColor(Color.RED.alpha(0.6));
		
		
		Box leftSide = new Box(new CFrame(0.0, 1.0), 0.05, 2.0, properties);
		leftSide.anchor();
		Box rightSide = new Box(new CFrame(1.0, 1.0), 0.05, 2.0, properties);
		rightSide.anchor();
		Box topSide = new Box(new CFrame(0.5, 1.5), 2.0, 0.05, properties);
		topSide.anchor();
		w.addObject(leftSide);
		w.addObject(rightSide);
		w.addObject(topSide);
		
		for(int y = 0; y < pyramidSize; y++){
			for(int x = 0; x < pyramidSize; x++){
				w.addObject(new Box(new CFrame(x*0.0300001 + 0.075 + 0.00001*(y%2), 0.03*y+0.15), 0.025, 0.025, properties));
			}
		}
	}

}
