package nano.engine.kernel;

import nano.engine.kernel.HttpRequestParser;
import nano.engine.kernel.HttpRequestParser.ParserState;

import nano.engine.kernel.HttpBodyParserAutomaton;
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

	final byte[] POSTREQUEST_CONTENT = ("{\"a\":1}").getBytes();
	final int POSTREQUEST_SIZE = POSTREQUEST_CONTENT.length;
	System.out.println(POSTREQUEST_SIZE);
	var parser = new HttpRequestParser();

	var payload = postRequest;
	var wholeParser = new HttpRequestParser();
	var wholeArena = wholeParser.getArena();
	var wholeResult = wholeParser.parse(payload);
	var arena = new byte[POSTREQUEST_SIZE];
	var byte_counter =0;
	for (int i =0; i< payload.length ; i++){
	    var streamedResult = parser.parse(new byte[]{payload[i]});
	    var a = parser.getArena();//we know in this case for sure all
	    if(a != null &&  a[0] != (byte) 0x00 ){
		System.out.println(a[0]);
		System.out.println(POSTREQUEST_CONTENT[byte_counter]);
		System.out.println(byte_counter);
		//assert(a[0] == POSTREQUEST_CONTENT[byte_counter]);
		byte_counter += 1;

	    }
	    if(streamedResult == ParserState.FINISH){

		assert byte_counter == POSTREQUEST_CONTENT.length;
		//assert Arrays.equals(wholeArena, POSTREQUEST_CONTENT);
		break;
	    }
	}
	

    }
}
