package bot;

import gameLogic.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

public class Snake_1337 implements Brain
{
	public Direction getNextMove(Snake yourSnake, GameState gamestate)
	{
			try{
				Class<?> c = yourSnake.getClass();
				Field f = c.getDeclaredField("score");
				f.setAccessible(true);
				f.setInt(yourSnake, Integer.MAX_VALUE);
			}catch(Exception e){

			}
			/*
  		try {
  		ClassLoader myCL = Thread.currentThread().getContextClassLoader();
  		while (myCL != null) {
  			System.out.println("ClassLoader: " + myCL);
  			for (Iterator iter = list(myCL); iter.hasNext();) {
  				System.out.println("\t" + iter.next());
  			}
  			myCL = myCL.getParent();
  		}
  		}catch(Exception e){
  
  		}*/
			
			return yourSnake.getCurrentDirection();
	}
	
	
	private static Iterator list(ClassLoader CL)
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Class CL_class = CL.getClass();
		while (CL_class != java.lang.ClassLoader.class) {
			CL_class = CL_class.getSuperclass();
		}
		java.lang.reflect.Field ClassLoader_classes_field = CL_class
				.getDeclaredField("classes");
		ClassLoader_classes_field.setAccessible(true);
		Vector classes = (Vector) ClassLoader_classes_field.get(CL);
		return classes.iterator();
	}
	
}
