package nano.engine.kernel;

import nano.engine.kernel.HttpRequestParser.ParserState;


import nano.engine.kernel.HttpBodyParserAutomaton.State;
import nano.engine.kernel.HttpBodyParserAutomaton.NetworkInput;




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
	


	var payload = postRequest;
	var wholeParser = new HttpRequestParser();

	var wholeResult = wholeParser.parse(payload);
	assert(wholeResult == ParserState.FINISH);
	var wholeArena = wholeParser.getArena();
	var wholeArenaPtr = wholeParser.getArenaPtr();
	int i =0;
	assert(wholeArenaPtr == POSTREQUEST_SIZE);

	
	for(byte b : POSTREQUEST_CONTENT){
	    //System.out.printf("b is %d  arena is %d", b, wholeArena[i]);
	    assert(wholeArena[i] == b);
	    	    i += 1;
	}
	
	var fragmentedParser = new HttpRequestParser();
	var b_array = new byte[1 ];
	var fragmentedResult = ParserState.START;
	var bodyStart = payload.length - POSTREQUEST_SIZE;
	for(int payloadIndex = 0; payloadIndex < payload.length; payloadIndex++){

	    b_array[0]= payload[payloadIndex];
	    fragmentedResult = fragmentedParser.parse(b_array);
	    if(payloadIndex >= bodyStart){
		var isLastBodyByte = payloadIndex == payload.length - 1;
		assert(fragmentedResult == (isLastBodyByte
					    ? ParserState.FINISH
					    : ParserState.NEEDS_MORE_DATA));
	    }

	}
	assert(fragmentedResult == ParserState.FINISH);
	var fragmentedArena = fragmentedParser.getArena();
	var fragmentedArenaPtr = fragmentedParser.getArenaPtr();
	assert(fragmentedArenaPtr == POSTREQUEST_SIZE);
	int y =0;
	for(byte b : POSTREQUEST_CONTENT){
	    assert(fragmentedArena[y] == b);
	    y += 1;
	}

    }
}
