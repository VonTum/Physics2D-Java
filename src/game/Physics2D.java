package game;


import game.gui.Screen;
import game.input.InputHandler;
import game.input.StandardInputHandler;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import math.CFrame;
import math.Vec2;
import physics.PhysicalProperties;
import physics.World;
import util.Color;

public class Physics2D {
	
	public static double SIMULATION_SPEED = 1.0;
	public static final int SIMULATION_REPEATS_PER_TICK = 130;
	public static boolean SIMULATION_PAUSED = false;
	
	public static final double deltaT = 0.01 / SIMULATION_REPEATS_PER_TICK;
	
	public static void main(String[] args) throws IOException {
		World w = new World(new Vec2(0.0, -2.0));
		InputHandler handler = new StandardInputHandler(w);
		Debug.setWorld(w);
		Screen.init(handler);
		
		new ConstrainedWorld().build(w);
		
		w.addObject(ObjectLibrary.createHammer(new CFrame(3.0, 0.0), new PhysicalProperties(1000, 0.05, 0.0, Color.DARK_GREY.alpha(0.6)), new PhysicalProperties(10.0, 0.05, 0.0, Color.DEFAULT_BRICK_COLOR)));
		// w.addObject(ObjectLibrary.createFloor(new CFrame(0.0, 0.0), new PhysicalProperties(10.0)));
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		
		executor.scheduleAtFixedRate(() -> {
			if(!SIMULATION_PAUSED){
				for(int i = 0; i < (int) SIMULATION_REPEATS_PER_TICK * SIMULATION_SPEED; i++){
					Debug.startTick();
					w.tick(deltaT);
					Debug.endTick();
				}
			}
		}, 0, 10, TimeUnit.MILLISECONDS);
		
		Screen.setWorld(w);
		while(!Screen.shouldClose()){
			Screen.refresh();
		}
		
		executor.shutdownNow();
	}
}
