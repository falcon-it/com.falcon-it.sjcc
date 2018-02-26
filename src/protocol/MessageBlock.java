package protocol;

public interface MessageBlock {
	Head head();
	Body body();
	MessageBlock tail();
}
