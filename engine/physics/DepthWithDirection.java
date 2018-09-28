package physics;

import math.NormalizedVec2;
import math.Vec2;

public class DepthWithDirection{
	public final NormalizedVec2 direction;
	public final double depth;
	
	public DepthWithDirection(NormalizedVec2 direction, double depth){
		this.direction = direction;
		this.depth = depth;
	}
	
	public Vec2 getVecToSurface(){
		return direction.mul(depth);
	}
}