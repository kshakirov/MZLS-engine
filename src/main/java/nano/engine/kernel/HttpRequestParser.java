package nano.engine.kernel;
import nano.engine.kernel.WirthHttpParser.STATUS;
import nano.engine.kernel.WirthHttpParser.WirthParsedData;




public class HttpRequestParser {
    public enum Phase {
	FINISHED,
	HEADER,
	BODY
	
    }
    public enum ParserState {
	START,
	FINISH,
	ERROR,
	NEEDS_MORE_DATA
    }


    private WirthHttpParser wirthHttpParser;
    private int[] offsetTable;
    private byte[] buffer;
    private byte[] arena;
    private int nextOffsetIdx;
    private STATUS status;
    private int consumedBytes; //index
    private WirthHttpParser.STATUS headerStatus;

    private WirthParsedData wirthParsedData;
    //    private 
    public HttpRequestParser(){
	this.wirthHttpParser = new WirthHttpParser();
	nextOffsetIdx=0;
	offsetTable = new int[64];
	arena = new byte[1028];
	status =STATUS.REQ_METHOD;
	consumedBytes =0;
	headerStatus = STATUS.REQ_METHOD;
	this.wirthParsedData = new WirthParsedData(headerStatus, nextOffsetIdx, consumedBytes, offsetTable);

	

    };

    public ParserState  parse(byte[] fragment){
	//somewhere to accumulate the whole body


	
	headerStatus = wirthHttpParser.parse(fragment);
	if(headerStatus!= STATUS.ERROR && headerStatus != STATUS.FINISHED){

	    return ParserState.NEEDS_MORE_DATA;
	}
	    
	
	
	return ParserState.FINISH;
    }
}
