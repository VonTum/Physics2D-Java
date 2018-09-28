package game;


import game.gui.Screen;
import game.input.InputHandler;
import game.input.StandardInputHandler;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import physics2D.Debug;
import physics2D.math.CFrame;
import physics2D.math.Vec2;
import physics2D.physics.World;

public class Physics2D {
	
	public static double SIMULATION_SPEED = 1.0;
	public static final int SIMULATION_REPEATS_PER_TICK = 10;
	public static boolean SIMULATION_PAUSED = false;
	
	public static final double deltaT = 0.01 / SIMULATION_REPEATS_PER_TICK;
	
	public static void main(String[] args) throws IOException {
		World w = new World(new Vec2(0.0, -1.0));
		InputHandler handler = new StandardInputHandler(w);
		Debug.setWorld(w);
		Screen.init(handler);
		
		// new ConstrainedWorld().build(w);
		
		/*for(Physical p:w.physicals){
			boolean anchored = p.isAnchored();
			p.unAnchor();
			p.move(new Vec2(4.0, 0.0));
			if(anchored) p.anchor();
		}*/
		
		//new FluidWorld(20).build(w);
		
		/*Physical hammer = ObjectLibrary.createHammer(new CFrame(0.0, 0.5, 0.3));
		
		
		
		hammer.angularVelocity = 0.2;
		
		w.addObject(hammer);*/
		
		w.addObject(ObjectLibrary.createFloor(new CFrame(0.0, 0.0), ObjectLibrary.BASIC));
		
		w.addObject(ObjectLibrary.createHammer(new CFrame(0.0, 1.5, Math.PI*3/8)));
		
		/*Physical test = new Physical(new CFrame(0.0, 0.5));
		
		test.addPart(new Rectangle(0.2, 0.1), new CFrame(0.2, 0.0), ObjectLibrary.BASIC);
		
		test.angularVelocity = 0.1;
		
		w.addObject(test);*/
		
		// new FluidWorld(30).build(w);
		
		
		/*Box a = new Box(new CFrame(-0.4, 0.5, 0.3), 0.1, 0.15, ObjectLibrary.BASIC);
		Box b = new Box(new CFrame(0.0, 0.5, 0.0), 0.3, 0.15, ObjectLibrary.BASIC);
		
		SlideConstraint s = new SlideConstraint(a, b, new CFrame(0.3, 0.0, 0.0), new CFrame(-0.2, 0.0, 0.0));
		
		w.addObject(a, b);
		w.addConstraint(s);*/
		
		/*RegularPolygon pol = new RegularPolygon(50, new Vec2(0.0, 0.2));
		Physical p = new Physical(new CFrame(1.0, 0.5));
		p.addPart(pol, CFrame.IDENTITY, ObjectLibrary.BASIC);
		
		w.addObject(p);*/
		
		/*b.applyImpulse(new Vec2(0.0, 0.1), new Vec2(0.15, 0.0));*/
		
		// w.addObject(ObjectLibrary.createHammer(new CFrame(3.0, 0.0), new PhysicalProperties(1000, 0.1, 0.0, Color.DARK_GREY.alpha(0.6)), new PhysicalProperties(10.0, 0.05, 0.0, Color.DEFAULT_BRICK_COLOR)));
		// w.addObject(ObjectLibrary.createFloor(new CFrame(0.0, 0.0), new PhysicalProperties(10.0)));
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		
		executor.scheduleAtFixedRate(() -> {
			if(!SIMULATION_PAUSED){
				try{
					for(int i = 0; i < (int) (SIMULATION_REPEATS_PER_TICK * SIMULATION_SPEED); i++){
						runTick(w);
					}
				}catch(Exception e){
					e.printStackTrace();
					System.exit(1);
				}
			}
		}, 0, 10, TimeUnit.MILLISECONDS);
		
		Screen.setWorld(w);
		while(!Screen.shouldClose() && !executor.isShutdown()){
			Screen.refresh();
		}
		
		executor.shutdownNow();
	}
	
	public static void runTick(World w){
		Debug.startTick();
		w.tick(deltaT);
		Debug.endTick();
		if(Debug.stop){
			System.out.println("Stopped");
			Debug.stop = false;
		}
	}
}
