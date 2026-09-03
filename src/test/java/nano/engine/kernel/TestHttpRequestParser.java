package nano.engine.kernel;
import nano.engine.kernel.HttpRequestParser;
import nano.engine.kernel.HttpRequestParser.ParserState;;

public class TestHttpRequestParser{
    private static byte[] standardGet = (
					 "GET /index.html HTTP/1.0\r\n" +
					 "Host: localhost\r\n" +
					 "Connection: close\r\n" +
					 "\r\n"
					 ).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    public static void main(String[] args){
	
	var parser = new HttpRequestParser();
	//	var parsedData = parser.parse(standardGet);
	var payload = standardGet;
	for (int i =0; i< payload.length ; i++){
	    var streamedResult = parser.parse(new byte[]{payload[i]});
	    System.out.printf("Status %s\n", streamedResult);
	    if(streamedResult == ParserState.FINISH){
		break;
	    }
	}
	System.out.println(parser);

    }
}
