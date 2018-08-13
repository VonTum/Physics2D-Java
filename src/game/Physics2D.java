package game;


import game.gui.Screen;
import game.input.InputHandler;
import game.input.StandardInputHandler;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import math.Vec2;
import physics.World;

public class Physics2D {
	
	public static double SIMULATION_SPEED = 1.0;
	public static final int SIMULATION_REPEATS_PER_TICK = 130;
	public static boolean SIMULATION_PAUSED = false;
	
	public static final double deltaT = 0.01 / SIMULATION_REPEATS_PER_TICK;
	
	public static void main(String[] args) throws IOException {
		World w = new World(new Vec2(0.0, -2.0));
		InputHandler handler = new StandardInputHandler(w);
		Screen.init(handler);
		
		new MultiMassWorld().build(w);
		
		Timer worldRefresh = new Timer(true);
		
		// Screen.camera = w.objects.get(3).cframe;
		
		worldRefresh.schedule(new TimerTask() {
			@Override public void run() {
				if(!SIMULATION_PAUSED)
					for(int i = 0; i < SIMULATION_REPEATS_PER_TICK * SIMULATION_SPEED; i++)
						w.tick(deltaT);
				
				// w.objects.get(0).cframe.position = new Vec2(0.0, 0.068);
				
				// Screen.camera.position = new RotMat2(0.01).mul(Screen.camera.position);
			}
		}, 0, (int) (10));
		
		Screen.setWorld(w);
		while(!Screen.shouldClose()){
			Screen.refresh();
		}
	}
}
