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
public @interface IOMethodInformation {
	/**
	 * тип методы
	 */
	enum MethodType { read, write }
	/**
	 * @return тип метода
	 */
	MethodType type();
	/**
	 * список типов
	 * если пустой массив, то тип взять из определения метода
	 * @return массив классов типов
	 */
	Class<?>[] classes() default {};
}
