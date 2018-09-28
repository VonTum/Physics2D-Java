package physics2D.math;

import game.gui.Describable;


public final class CFrame implements Describable {
	
	public static final CFrame IDENTITY = new CFrame(0.0, 0.0, 0.0);
	public final Vec2 position;
	public final RotMat2 rotation;
	public CFrame(Vec2 position, RotMat2 rotation){
		this.position = position;
		this.rotation = rotation;
	}
	public CFrame(Vec2 position, double angle){
		this.position = position;
		this.rotation = new RotMat2(angle);
	}
	public CFrame(double x, double y, RotMat2 rotation){this(new Vec2(x, y), rotation);}
	public CFrame(double x, double y, double angle){this(new Vec2(x, y), angle);}
	public CFrame(double x, double y){this(new Vec2(x, y), Mat2.IDENTITY);}
	public CFrame(){this(Vec2.ZERO, Mat2.IDENTITY);}
	
	public Vec2 globalToLocal(Vec2 vec){
		return rotation.inv().mul(vec.subtract(position));
	}
	
	public Vec2 globalToLocalRotation(Vec2 vec) {
		return rotation.inv().mul(vec);
	}
	
	public Vertex2 globalToLocal(Vertex2 vertex){
		return new Vertex2(rotation.inv().mul(vertex.position.subtract(position)), rotation.inv().mul(vertex.orientation), rotation.inv().mul(vertex.normalVec), vertex.edgeLength, vertex.concave);
	}
	
	public Vec2 localToGlobal(Vec2 vec){
		return rotation.mul(vec).add(position);
	}
	
	public Vec2 localToGlobalRotation(Vec2 vec) {
		return rotation.mul(vec);
	}
	
	public Vertex2 localToGlobal(Vertex2 vertex){
		return new Vertex2(rotation.mul(vertex.position).add(position), rotation.mul(vertex.orientation), rotation.mul(vertex.normalVec), vertex.edgeLength, vertex.concave);
	}
	
	public CFrame localToGlobal(CFrame cframe){
		return new CFrame(localToGlobal(cframe.position), rotation.mul(cframe.rotation));
	}
	
	public CFrame globalToLocal(CFrame cframe){
		return new CFrame(globalToLocal(cframe.position), rotation.inv().mul(cframe.rotation));
	}
	
	public Vec2[] globalToLocalArray(Vec2... vecs){
		int size = vecs.length;
		Vec2[] newVecs = new Vec2[size];
		
		for(int i = 0; i < size; i++)
			newVecs[i] = globalToLocal(vecs[i]);
		
		return newVecs;
	}
	
	public Vec2[] localToGlobalArray(Vec2... vecs){
		int size = vecs.length;
		Vec2[] newVecs = new Vec2[size];
		
		for(int i = 0; i < size; i++)
			newVecs[i] = localToGlobal(vecs[i]);
		
		return newVecs;
	}
	
	public CFrame add(Vec2 offset){return new CFrame(position.add(offset), rotation);}
	public CFrame rotated(RotMat2 rotation){return new CFrame(position, this.rotation.mul(rotation));}
	public CFrame rotated(double angle){return rotated(new RotMat2(angle));}
	/*public void move(Vec2 offset){position = position.add(offset);}
	public void subtract(Vec2 offset){position = position.subtract(offset);}
	public void rotate(RotMat2 rotMat){rotation = rotation.mul(rotMat);}
	public void rotate(double angle){rotation = rotation.mul(new RotMat2(angle));}*/
	// public CFrame copy(){return new CFrame(position, rotation);}
	
	
	@Override
	public String toString(){
		return position + ": " + rotation.getAngle() + "°";
	}
	@Override
	public String describe() {
		return String.format("{\npos: %s\nangle: %.9fdeg\n}", position.toString().replace("\n", "\n  "), rotation.getAngle()*180/Math.PI);
	}
}
