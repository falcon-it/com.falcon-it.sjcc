package packet;

/**
 * интерфейс для чтения/записи структурированных типов
 * @author Ilya Sokolov
 */
public interface StructureSerialize {
	/**
	 * прочитать данные
	 * @param ioCTX объект для чтения
	 */
	void read(IOContext ioCTX);
	/**
	 * записать данные
	 * @param ioCTX объект для записи
	 */
	void write(IOContext ioCTX);
}
