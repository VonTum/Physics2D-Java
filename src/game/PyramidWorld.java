package game;

import geom.Box;
import math.CFrame;
import physics.PhysicalProperties;
import physics.World;
import util.Color;

public class PyramidWorld implements WorldBuilder {
	
	private final int pyramidSize;
	
	public PyramidWorld(int pyramidSize) {
		this.pyramidSize = pyramidSize;
	}
	
	@Override
	public void build(World w) {
		PhysicalProperties properties = new PhysicalProperties(10.0).withColor(Color.RED.alpha(0.6));
		w.addObject(ObjectLibrary.createFloor(new CFrame(0.0, 0.0), properties.withColor(Color.DEFAULT_BRICK_COLOR)));
		
		for(int y = 0; y < pyramidSize; y++){
			for(int x = 0; x < pyramidSize-y; x++){
				w.addObject(new Box(new CFrame(x*0.100001+0.05*y, 0.1*y+0.15), 0.1, 0.05, properties));
			}
		}
	}

}
