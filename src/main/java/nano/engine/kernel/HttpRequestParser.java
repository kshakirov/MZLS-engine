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
    public record ParsedData(int[] offsetTable,  STATUS status, int nextOffsetIdx, int consumedBytes, byte[] arena, ParserState parserState){
	public ParsedData withParserState(ParserState newParserState){
	    return new ParsedData(offsetTable,status,nextOffsetIdx,consumedBytes,arena,newParserState);
	}
	
    }
    private WirthHttpParser wirthHttpParser;
    private int[] offsetTable;
    private byte[] buffer;
    private byte[] arena;
    private int nextOffsetIdx;
    private STATUS status;
    private int consumedBytes; //index
    private WirthHttpParser.STATUS headerStatus;
    private ParsedData parsedData;
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
	this.parsedData = new ParsedData(offsetTable,status,nextOffsetIdx, consumedBytes,arena, ParserState.START); // this will contain body data too
	

    };

    public ParsedData  parse(byte[] fragment){
	//somewhere to accumulate the whole body


	
	wirthParsedData = wirthHttpParser.parse(fragment, wirthParsedData);
	System.out.println(wirthParsedData);
	if(wirthParsedData.status()!= STATUS.ERROR || parsedData.status() != STATUS.FINISHED){
	    this.parsedData = parsedData.withParserState(ParserState.NEEDS_MORE_DATA);
	    return parsedData;
	}
	    
	
	
	return parsedData;
    }
}
