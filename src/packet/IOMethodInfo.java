package packet;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * аннотация, описывающая методы диспетчегов ввода вывода
 * @author Ilya Sokolov
 */
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
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
	/**
	 * список типов
	 * если пустой массив, то тип взять из определения метода
	 * @return массив классов типов
	 */
	Class<?>[] classes() default {};
}
