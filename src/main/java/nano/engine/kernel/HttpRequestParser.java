package nano.engine.kernel;
import nano.engine.kernel.WirthHttpParser.STATUS;



public class HttpRequestParser {
    public enum Phase {
	FINISHED,
	HEADER,
	BODY
	
    }
    public record ParsedData(int[] offsetTable,  STATUS status, int nextOffsetIdx, int consumedBytes, byte[] arena){}
    private WirthHttpParser wirthHttpParser;
    private int[] offsetTable;
    private byte[] buffer;
    private byte[] arena;
    private int nextOffsetIdx;
    private STATUS status;
    private int consumedBytes; //index
    private WirthHttpParser.STATUS headerStatus;
    private ParsedData parsedData;
    //    private 
    public HttpRequestParser(){
	this.wirthHttpParser = new WirthHttpParser();
	nextOffsetIdx=0;
	offsetTable = new int[64];
	arena = new byte[1028];
	status =STATUS.REQ_METHOD;
	consumedBytes =0;

    };

    public ParsedData  parse(byte[] fragment){
	ParsedData parsedData = new ParsedData(offsetTable,status,nextOffsetIdx, consumedBytes,arena);
	return parsedData;
    }
}
