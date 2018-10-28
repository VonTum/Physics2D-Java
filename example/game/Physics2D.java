package game;


import game.gui.Screen;
import game.input.InputHandler;
import game.input.StandardInputHandler;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import physics2D.Debug;
import physics2D.geom.ConvexPolygon;
import physics2D.math.CFrame;
import physics2D.math.Vec2;
import physics2D.physics.Physical;
import physics2D.physics.World;

public class Physics2D {
	
	public static double SIMULATION_SPEED = 1.0;
	public static final int SIMULATION_REPEATS_PER_TICK = 10;
	public static boolean SIMULATION_PAUSED = true;
	
	public static final double deltaT = 0.01 / SIMULATION_REPEATS_PER_TICK;
	
	public static void main(String[] args) throws IOException {
		World w = new World(new Vec2(0.0, -1.0));
		InputHandler handler = new StandardInputHandler(w);
		Debug.setWorld(w);
		Screen.init(handler);
		
		w.addObject(ObjectLibrary.createFloor(new CFrame(0.0, 0.0), ObjectLibrary.BASIC));
		
		w.addObject(ObjectLibrary.createHammer(new CFrame(0.0, 1.5, Math.PI*3/8)));
		
		ConvexPolygon weirdthing = new ConvexPolygon(new Vec2[]{
			new Vec2(0.1,0.1),
			new Vec2(0.3,0.5),
			new Vec2(0.0,0.7),
			new Vec2(-0.5,0.3),
		});
		
		Physical p = new Physical(CFrame.IDENTITY);
		p.addPart(weirdthing, CFrame.IDENTITY, ObjectLibrary.BASIC.withDensity(1.0));
		
		w.addObject(p);
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		
		executor.scheduleAtFixedRate(() -> {
			if(!SIMULATION_PAUSED){
				try{
					for(int i = 0; i < (int) (SIMULATION_REPEATS_PER_TICK * SIMULATION_SPEED); i++){
						runTick(w);
					}
				}catch(Throwable e){
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
