package game.input;

import java.util.ArrayList;
import java.util.List;

import game.ObjectLibrary;
import game.Physics2D;
import game.gui.Screen;
import game.gui.Screen.InProgressPolygon;
import game.util.Color;

import org.lwjgl.glfw.GLFW;

import physics2D.Debug;
import physics2D.geom.CompositePolygon;
import physics2D.geom.Convex;
import physics2D.geom.Polygon;
import physics2D.geom.SimpleConvexPolygon;
import physics2D.geom.Triangle;
import physics2D.math.CFrame;
import physics2D.math.Vec2;
import physics2D.physics.Part;
import physics2D.physics.Physical;
import physics2D.physics.World;

public class StandardInputHandler implements InputHandler {
	
	private World world;
	
	private boolean[] mouseDown = new boolean[6];
	
	private Vec2 lastMousePos = Vec2.ZERO;
	
	public StandardInputHandler(World w) {
		this.world = w;
	}
	
	@Override
	public void mouseDown(Vec2 screenPos, int button, int modifiers) {
		mouseDown[button] = true;
		if(button == GLFW.GLFW_MOUSE_BUTTON_1){
			switch(mode){
			case STANDARD:
				Vec2 worldPos = Screen.mouseToWorldCoords(screenPos);
				Part selectedPart = world.getPartAt(worldPos);
				if(selectedPart != null){
					onClickPart(worldPos, selectedPart);
					System.out.println("Grabbing block: " + selectedPart);
					world.grabBlock(worldPos);
				}
				break;
			case DRAWING:
				int size = drawingPolygon.poly.size();
				drawingPolygon.poly.set(size-1, Screen.mouseToWorldCoords(screenPos));
				drawingPolygon.poly.add(Screen.mouseToWorldCoords(screenPos));
				break;
			}
		}
	}
	
	public void onClickPart(Vec2 worldPos, Part clickedPart){
		//ConvexPolygon p = (ConvexPolygon) clickedPart.shape;
		//ConvexPolygon cp = p.transformToCFrame(new CFrame(p.getCenterOfMass().neg()));
		//System.out.println(String.format("%.20f", cp.getNewI()));
		
		//System.out.println(p.getCenterOfMass() + "<=>" + p.getNewCOM());
	}

	@Override
	public void mouseUp(Vec2 screenPos, int button, int modifiers) {
		mouseDown[button] = false;
		if(button == GLFW.GLFW_MOUSE_BUTTON_1){
			world.dropBlock();
		}
	}
	
	@Override
	public void mouseMove(Vec2 newScreenPos) {
		
		if(mode == InputMode.STANDARD && mouseDown[0])
			world.dragBlock(Screen.mouseToWorldCoords(newScreenPos));
		
		if(mode == InputMode.DRAWING){
			int size = drawingPolygon.poly.size();
			drawingPolygon.poly.set(size-1, Screen.mouseToWorldCoords(newScreenPos));
			
		}
		
		if(mouseDown[1])
			Screen.camera.move(Screen.mouseToWorldCoords(lastMousePos).subtract(Screen.mouseToWorldCoords(newScreenPos)));
		
		lastMousePos = newScreenPos;
	}
	
	private enum InputMode{
		STANDARD,
		DRAWING
	}
	
	public InputMode mode = InputMode.STANDARD;
	private InProgressPolygon drawingPolygon = null;
	
	@Override
	public void keyDown(int key, int scancode, int modifiers) {
		keyDownOrRepeat(key, scancode, modifiers);
		switch(key){
		case GLFW.GLFW_KEY_P:
			Physics2D.SIMULATION_PAUSED = !Physics2D.SIMULATION_PAUSED;
			System.out.println("Simulation "+((Physics2D.SIMULATION_PAUSED)?"paused":"unpaused"));
			break;
		case GLFW.GLFW_KEY_N:
			switch(mode){
			case STANDARD:
				setMode(InputMode.DRAWING);
				break;
			case DRAWING:
				setMode(InputMode.STANDARD);
				break;
			}
		case GLFW.GLFW_KEY_V:
			Screen.DRAW_VERTEX_CORNERS = true;
			break;
		}
	}
	
	@Override
	public void keyUp(int key, int scancode, int modifiers) {
		switch(key){
		case GLFW.GLFW_KEY_V:
			Screen.DRAW_VERTEX_CORNERS = false;
			break;
		}
	}

	@Override
	public void keyRepeat(int key, int scancode, int modifiers) {
		keyDownOrRepeat(key, scancode, modifiers);
	}

	public void setMode(InputMode newMode){
		switch(newMode){
		case STANDARD:
			Polygon poly = finishShapeDrawing();
			if(poly != null){
				Vec2 com = poly.getCenterOfMass();
				Physical p = new Physical(new CFrame(com));
				p.addPart(poly.translate(com.neg()), CFrame.IDENTITY, ObjectLibrary.BASIC);
				world.addObject(p);
			}
			break;
		case DRAWING:
			beginShapeDrawing();
			break;
		}
		mode = newMode;
		System.out.println(newMode);
	}
	
	public void beginShapeDrawing(){
		if(mode != InputMode.STANDARD) return;
		mode = InputMode.DRAWING;
		drawingPolygon = Screen.addPolygon(new ArrayList<Vec2>(), Color.YELLOW.fuzzier());
		Vec2 mousePos = Screen.mouseToWorldCoords(Screen.getMousePos());
		drawingPolygon.poly.add(mousePos);
	}
	
	public Polygon finishShapeDrawing(){
		if(mode != InputMode.DRAWING) return null;
		mode = InputMode.STANDARD;
		Screen.removeDrawable(drawingPolygon);
		drawingPolygon.poly.remove(drawingPolygon.poly.size()-1);
		Vec2[] polygon = drawingPolygon.poly.toArray(new Vec2[drawingPolygon.poly.size()]);
		drawingPolygon = null;
		if(polygon.length >= 3){
			if(!Polygon.isPositivelyOriented(polygon)) polygon = Polygon.reverse(polygon);
			if(Polygon.isValid(polygon)) return new CompositePolygon(polygon);
			else return null;
		}else
			return null;
	}
	
	private void keyDownOrRepeat(int key, int scancode, int modifiers) {
		switch(key){
		case GLFW.GLFW_KEY_KP_ADD:
			Physics2D.SIMULATION_SPEED *= 1.1;
			System.out.println("Simulation speed: " + Physics2D.SIMULATION_SPEED);
			break;
		case GLFW.GLFW_KEY_KP_SUBTRACT:
			Physics2D.SIMULATION_SPEED /= 1.1;
			System.out.println("Simulation speed: " + Physics2D.SIMULATION_SPEED);
			break;
		case GLFW.GLFW_KEY_O:
			if(Physics2D.SIMULATION_PAUSED) Physics2D.runTick(world);
			System.out.println("Proceeded one step");
			break;
		case GLFW.GLFW_KEY_G:
			System.out.println("Garbagecollecting!");
			System.gc();
			break;
		case GLFW.GLFW_KEY_C:
			System.out.println("Convex decomposition");
			Part p = world.getPartAt(Screen.getMouseWorldPos());
			if(p == null) break;
			List<? extends Convex> convexes = p.getGlobalShape().convexDecomposition();
			
			for(Convex c:convexes)
				Debug.logShape(c, Color.GREEN.fuzzier(), Color.DARK_GREEN);
			Screen.addDrawings();
			break;
		case GLFW.GLFW_KEY_T:
			System.out.println("Triangulate");
			Part prt = world.getPartAt(Screen.getMouseWorldPos());
			if(prt == null) break;
			Triangle[] triangles = ((Polygon) prt.getGlobalShape()).divideIntoTriangles();
			
			for(Triangle t:triangles)
				Debug.logShape(new SimpleConvexPolygon(t.getCorners()), Color.random().fuzzier(), Color.TRANSPARENT);
			Screen.addDrawings();
			break;
		case GLFW.GLFW_KEY_A:
			System.out.println("Anchoring");
			Part ap = world.getPartAt(Screen.getMouseWorldPos());
			if(ap == null) break;
			
			if(ap.parent.isAnchored())
				ap.parent.unAnchor();
			else
				ap.parent.anchor();
			
			break;
		case GLFW.GLFW_KEY_D:
			world.removeObject(world.getPartAt(Screen.getMouseWorldPos()));
			break;
		case GLFW.GLFW_KEY_F1:
			Debug.MARK_FORCES = !Debug.MARK_FORCES;
			break;
		case GLFW.GLFW_KEY_F2:
			Debug.MARK_VECTORS = !Debug.MARK_VECTORS;
			break;
		case GLFW.GLFW_KEY_F3:
			Debug.MARK_POINTS = !Debug.MARK_POINTS;
			break;
		case GLFW.GLFW_KEY_F4:
			Debug.MARK_SHAPES = !Debug.MARK_SHAPES;
			break;
		case GLFW.GLFW_KEY_F5:
			Screen.DRAW_CENTER_OF_MASS = !Screen.DRAW_CENTER_OF_MASS;
			break;
		}
		
		// System.out.println(GLFW.glfwGetKeyName(key, scancode));
	}
	
	@Override
	public void scroll(double xscroll, double yscroll) {
		Screen.camera.zoom(Math.pow(1.3, yscroll), Screen.mouseToScreenCoords(Screen.getMousePos()));
	}
}
