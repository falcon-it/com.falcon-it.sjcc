package protocol;

public interface Module <InT, OutT> {
	InT read(OutT rd);
	OutT write(InT wr);
}
