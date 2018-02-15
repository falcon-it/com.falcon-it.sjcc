package utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * глубокое копирование объекта
 * @author Ilya Sokolov
 */
public final class DeepCopy {
	/**
	 * интерфейс для клонирования объектов
	 */
	public interface Clone extends Cloneable {
		Clone clone() throws CloneNotSupportedException;
	}
	
	private DeepCopy () { }
	
	public static Object copy(Object src) throws CloneNotSupportedException {
		try {
			if(src instanceof Clone) {
				return ((Clone)src).clone();
			}
			else {
				Class<?> c = src.getClass();
				if(c.isPrimitive()) {
					return src;
				}
				else {
					try {
						Constructor<?> cc = c.getConstructor(c);
							return cc.newInstance(src);
					} catch (NoSuchMethodException e) { }
					
					try {
						Method cm = c.getMethod("clone");
						Class<?> rt = cm.getReturnType();
						if(Modifier.isPublic(cm.getModifiers()) && 
								!Modifier.isStatic(cm.getModifiers()) && 
								!rt.equals(void.class) && 
								!rt.equals(Void.class)) {
							return cm.invoke(src);
						}
					} catch (NoSuchMethodException e) { }
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | CloneNotSupportedException e) {
		}
		
		throw new CloneNotSupportedException();
	}
}
