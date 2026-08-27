package nano.engine.kernel;



public class HttpRequestParser {
    public enum Phase {
	FINISHED,
	HEADER,
	BODY
	
    }
    public record ParsedData(int[] offsetTable, byte[] arena){}
    private WirthHttpParser wirthHttpParser;
    private int[] offsetTable;
    private byte[] buffer;
    private byte[] arena;
    private WirthHttpParser.STATUS headerStatus;
    private ParsedData parsedData;
    //    private 
    public HttpRequestParser(){
	this.wirthHttpParser = new WirthHttpParser();

    };

    public ParsedData  parse(byte[] fragment){
	ParsedData parsedData = new ParsedData(this.offsetTable, this.arena);
	return parsedData;
    }
}
