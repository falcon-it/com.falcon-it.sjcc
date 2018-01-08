package packet;

/**
 * интерфейс для чтения/записи изменяемых типов данных
 * @author Ilya Sokolov
 */
public interface DynamicStructureSerialize extends StructureSerialize {
	/**
	 * расчитывает id в записимости от содержимого объекта
	 * @return id типа
	 */
	int calculateDataID();
}
