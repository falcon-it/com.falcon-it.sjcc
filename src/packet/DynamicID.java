package packet;

/**
 * итерфейс для типов с динамической структурой
 * реализация Cloneable нужна для копирования динамической 
 * структуры объекта
 * @author Ilya Sokolov
 */
public interface DynamicID extends Cloneable {
	/**
	 * @return расчитанный динамический id типа
	 */
	int calculateDynamicID();
}
