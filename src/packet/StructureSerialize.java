package packet;

import packet.IOMethodType.MethodType;

/**
 * интерфейс для чтения/записи структурированных типов
 * @author Ilya Sokolov
 */
public interface StructureSerialize {
	/**
	 * прочитать данные
	 * @param ioCTX объект для чтения
	 */
	@IOMethodType(type=MethodType.read, general=true)
	void read(IOContext ioCTX);
	/**
	 * записать данные
	 * @param ioCTX объект для записи
	 */
	@IOMethodType(type=MethodType.write, general=true)
	void write(IOContext ioCTX);
}
