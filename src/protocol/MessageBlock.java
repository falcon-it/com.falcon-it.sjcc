package protocol;

/**
 * пакет с обязательными полями<br/>
 * по обязательным полям можно распознать подсистему
 * @author Ilya Sokolov
 */
public interface MessageBlock {
	/**
	 * @return идетификатор прокола протокла
	 */
	int protocol();
	/**
	 * @return версия протокола
	 */
	int version();
	/**
	 * @return заголовок пакета <b>обязателен</b> - зависит от протокола
	 */
	Head head();
}
