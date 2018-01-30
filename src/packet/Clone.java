package packet;

/**
 * интерфейс для клонирования объектов
 * @author Ilya Sokolov
 */
public interface Clone {
	/**
	 * создаёт копию объекта
	 * @return копия объекта
	 */
	<T> T copy();
}
