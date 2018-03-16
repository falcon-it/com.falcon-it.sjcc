package protocol;

/**
 * интефейс очереди сообщений
 * @author Ilya Sokolov
 */
public interface Queue {
	/**
	 * поставить сообщение в очередь на отправку
	 * @param message собщение
	 */
	<T> void send(T packet);
	
	/**
	 * интерфейс слушателя сообщений очереди
	 */
	interface ReceiveListener {
		<T> void receive(T packet);
	}
	
	/**
	 * зарегистрировать слушателя сообщений очереди
	 * @param e новый слушатель
	 */
	void addReceiveListener(ReceiveListener e);
	
	/**
	 * отменить регистрацию слушателя
	 * @param e слушатель
	 */
	void removeReceiveListener(ReceiveListener e);
	
	/**
	 * удалить всех слушателей
	 */
	void clearReceiveListeners();
}
