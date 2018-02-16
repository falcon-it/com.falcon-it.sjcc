package utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * глубокое копирование объекта
 * @author Ilya Sokolov
 */
@SuppressWarnings("unchecked")
public final class DeepCopy {
	/**
	 * интерфейс для клонирования объектов
	 */
	public interface Clone extends Cloneable {
		Clone clone() throws CloneNotSupportedException;
	}
	
	/**
	 * интерфейс для копирования простых типов<br />
	 * внутренний
	 * @param <T> тип копируемого объекта
	 */
	private interface CopySimpleType <T> {
		T copy(T p);
	}
	
	/**
	 * мап с простыми типами
	 */
	private static final HashMap<Integer, CopySimpleType<Object>> copiers = new HashMap<>();
	
	static {
		CopySimpleType<?> s = (Byte p) -> { return (byte)p; };
		copiers.put(byte.class.getName().hashCode(), (CopySimpleType<Object>)s);
		copiers.put(Byte.class.getName().hashCode(), (CopySimpleType<Object>)s);
		s = (Boolean p) -> { return (boolean)p; };
		copiers.put(boolean.class.getName().hashCode(), (CopySimpleType<Object>)s);
		copiers.put(Boolean.class.getName().hashCode(), (CopySimpleType<Object>)s);
		s = (Character p) -> { return (char)p; };
		copiers.put(char.class.getName().hashCode(), (CopySimpleType<Object>)s);
		copiers.put(Character.class.getName().hashCode(), (CopySimpleType<Object>)s);
		s = (Float p) -> { return (float)p; };
		copiers.put(float.class.getName().hashCode(), (CopySimpleType<Object>)s);
		copiers.put(Float.class.getName().hashCode(), (CopySimpleType<Object>)s);
		s = (Double p) -> { return (double)p; };
		copiers.put(double.class.getName().hashCode(), (CopySimpleType<Object>)s);
		copiers.put(Double.class.getName().hashCode(), (CopySimpleType<Object>)s);
		s = (Short p) -> { return (short)p; };
		copiers.put(short.class.getName().hashCode(), (CopySimpleType<Object>)s);
		copiers.put(Short.class.getName().hashCode(), (CopySimpleType<Object>)s);
		s = (Integer p) -> { return (int)p; };
		copiers.put(int.class.getName().hashCode(), (CopySimpleType<Object>)s);
		copiers.put(Integer.class.getName().hashCode(), (CopySimpleType<Object>)s);
		s = (Long p) -> { return (long)p; };
		copiers.put(long.class.getName().hashCode(), (CopySimpleType<Object>)s);
		copiers.put(Long.class.getName().hashCode(), (CopySimpleType<Object>)s);
		s = (String p) -> { return new String(p); };
		copiers.put(String.class.getName().hashCode(), (CopySimpleType<Object>)s);
	}
	
	private DeepCopy () { }
	
	/**
	 * функция глубокого копирования
	 * @param src исходный объект
	 * @return полная копия
	 * @throws CloneNotSupportedException
	 */
	public static Object copy(Object src) throws CloneNotSupportedException {
		try {
			if(src instanceof Clone) {
				return ((Clone)src).clone();
			}
			else {
				Class<?> c = src.getClass();
				int nameHash = c.getName().hashCode();
				if(copiers.containsKey(nameHash)) {
					return copiers.get(nameHash).copy(src);
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
