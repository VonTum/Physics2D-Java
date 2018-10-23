package physics2D.tests;

import org.junit.After;
import org.junit.Before;

import physics2D.Debug;

public abstract class GUITestSuite {
	private static final boolean SCREEN_ENABLED = "true".equalsIgnoreCase(System.getProperty("debugEnabled"));
	@Before
	public void setupDebugScreen(){
		if(SCREEN_ENABLED)
			Debug.setupDebugScreen();
	}
	
	@After
	public void destroyDebugScreen(){
		if(SCREEN_ENABLED){
			Debug.endTick();
			if(Debug.getDrawCount() > 0)
				Debug.halt();
			else
				Debug.destroyDebugScreen();
		}
	}
}
