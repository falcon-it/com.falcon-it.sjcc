package protocol;

/**
 * тело сообщения
 * @author Ilya Sokolov
 */
public interface Body {
	/**
	 * @return следующее сообщение<br />
	 * может быть выстроена цепочка из произвольного количества подсистем
	 */
	MessageBlock tail();
}
