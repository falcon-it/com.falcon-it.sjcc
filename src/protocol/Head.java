package protocol;

/**
 * заголовок пакета
 * @author Ilya Sokolov
 */
public interface Head {
	/**
	 * @return тело пакета - может быть пустым<br /> 
	 * (или содержать слелующую часть сообщения)
	 */
	Body body();
}
