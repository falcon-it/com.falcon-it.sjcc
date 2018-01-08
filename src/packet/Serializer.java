package packet;

/**
 * основной интефейс сериализации
 * @author Ilya Sokolov
 */
public final class Serializer {
	/**
	 * рассчитать id класса типа
	 * @param c экземпляр класса типа
	 * @return id типа
	 */
	public static int calculateTypeIDByClass(Class<?> c) {
		return c.getName().hashCode();
	}
	
	/**
	 * рассчитать id экземпляра типа
	 * @param instance ссылки на экземпляр данных
	 * @return id экзмпляра
	 */
	public static <InstanceType> int calculateTypeIDByInstance(InstanceType instance) {
		if(instance instanceof DynamicStructureSerialize) {
			return ((DynamicStructureSerialize)instance).calculateDataID();
		}
		
		return Serializer.calculateTypeIDByClass(instance.getClass());
	}
}
