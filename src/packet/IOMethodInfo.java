package packet;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

/**
 * аннотация, описывающая методы диспетчегов ввода вывода
 * @author Ilya Sokolov
 */
@Target(METHOD)
public @interface IOMethodInfo {
	/**
	 * тип методы
	 */
	enum MethodType {
		read,
		write
	}
	/**
	 * @return тип метода
	 */
	MethodType type();
	/**
	 * @return true метод общий - 
	 * не работает на прямую с объектами ввода/вывода
	 */
	boolean universal();
}
