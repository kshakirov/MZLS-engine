package nano.engine.kernel;
import nano.engine.kernel.HttpRequestParser;
import nano.engine.kernel.HttpRequestParser.ParserState;
import java.util.Arrays;


public class TestHttpRequestParser{
    private static byte[] standardGet = (
					 "GET /index.html HTTP/1.0\r\n" +
					 "Host: localhost\r\n" +
					 "Connection: close\r\n" +
					 "\r\n"
					 ).getBytes(java.nio.charset.StandardCharsets.UTF_8);

    private static byte[] heavyGet = (
				      "GET /api/v1/users/profile?id=42 HTTP/1.1\r\n" +
				      "Host: 127.0.0.1\r\n" +
				      "User-Agent: wrk/4.2.0\r\n" +
				      "Accept: */*\r\n" +
				      "X-Real-IP: 192.168.1.100\r\n" +
				      "Connection: keep-alive\r\n" +
				      "Content-Length: 64\r\n" +
				      "\r\n"
				      ).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static byte[] postRequest = ("POST /api/v1/data HTTP/1.1\r\n" +
					 "Host: localhost\r\n" +
					 "Content-Type: application/json\r\n" +
					 "Content-Length: 7\r\n" +
					 "\r\n" +
					 "{\"a\":1}").getBytes();
    public static void main(String[] args){
	final int POSTREQUEST_SIZE = 8;
	final byte[] POSTREQUEST_CONTENT = ("{\"a\":1}").getBytes();
	var parser = new HttpRequestParser();
	//	var parsedData = parser.parse(standardGet);
	var payload = postRequest;
	var arena = new byte[POSTREQUEST_SIZE];
	var byte_counter =0;
	for (int i =0; i< payload.length ; i++){
	    var streamedResult = parser.parse(new byte[]{payload[i]});
	    var a = parser.getArena();//we know in this case for sure all
	    if(a != null &&  a[0] != (byte) 0x00){
		assert(a[byte_counter] == POSTREQUEST_CONTENT[byte_counter]); 
	    }
	    if(streamedResult == ParserState.FINISH){
		break;
	    }
	}
	

    }
}
