package packet;

/**
 * интерфейс для клонирования объектов
 * @author Ilya Sokolov
 */
public interface Clone extends Cloneable, Serialize {
	Clone clone() throws CloneNotSupportedException;
}
