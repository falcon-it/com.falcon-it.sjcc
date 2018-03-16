package protocol;

/**
 * основной модуль с очередями чтения/записи пакетов<br />
 * клиент регистрирует слушателя для очереди чтения и отсылает пакеты через очередь записи<br />
 * устрой вывод наоборот регистрирует слушателя для очереди записи и пересылает сообщения через очередь чтения
 * @author Ilya Sokolov
 */
public class Main {
	private final class PacketQueue implements Queue {

		@Override
		public <T> void send(T packet) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addReceiveListener(ReceiveListener e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeReceiveListener(ReceiveListener e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void clearReceiveListeners() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
