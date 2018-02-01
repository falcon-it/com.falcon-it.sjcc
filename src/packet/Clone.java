package packet;

/**
 * интерфейс для клонирования объектов
 * @author Ilya Sokolov
 */
public interface Clone extends Cloneable {
	Clone clone() throws CloneNotSupportedException;
}
