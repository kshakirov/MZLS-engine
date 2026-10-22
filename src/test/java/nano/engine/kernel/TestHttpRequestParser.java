package nano.engine.kernel;

import nano.engine.kernel.HttpRequestParser.ParserState;


import nano.engine.kernel.HttpBodyParserAutomaton.State;
import nano.engine.kernel.HttpBodyParserAutomaton.Event;




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
				      "User-Agent: 850" +
				      "\r\n"
				      ).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static byte[] postRequest = ("POST /api/v1/data HTTP/1.1\r\n" +
					 "Host: localhost\r\n" +
					 "Content-Type: application/json\r\n" +
					 "Content-Length: 7\r\n" +
					 "User-Agent: 850\r\n" +
					 "\r\n" +
					 "{\"a\":1}").getBytes();


    // Первый фрагмент: заголовки и начало чанка тела (размер в HEX, затем данные)
    private static byte[] chunk1 = ("POST /api/v1/stream HTTP/1.1\r\n" +
		     "Host: localhost\r\n" +
		     "Transfer-Encoding: chunked\r\n" +
		     "\r\n" +
		     "2\r\n" +
		     "{" +
		     "\"").getBytes(java.nio.charset.StandardCharsets.UTF_8);

    private static byte[] chunkError = ("POST /api/v1/stream HTTP/1.1\r\n" +
		     "Host: localhost\r\n" +
		     "Transfer-Encoding: chunked\r\n" +
		     "\r\n" +
		     "4\r\n{\"a\":1}\r\n0\r\n\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);


    // Второй фрагмент: оставшаяся часть первого чанка и терминатор (0\r\n\r\n)
    private static byte[] chunk2 = ("\r\n5\r\na").getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static byte[] chunk3 = ("\":1}\r\n" +
				    "0\r\n" +
		     "\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);

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
	var chunkParser = new HttpRequestParser();

	var chunkParserState = chunkParser.parse(chunk1);
	assert(chunkParserState == ParserState.NEEDS_MORE_DATA); 
 	chunkParserState = chunkParser.parse(chunk2);
	assert(chunkParserState == ParserState.NEEDS_MORE_DATA); 
	chunkParserState = chunkParser.parse(chunk3);
	assert(chunkParserState == ParserState.FINISH);
	assert(chunkParser.getArenaPtr() == POSTREQUEST_SIZE);
	int z = 0;
	var chunkedArena = chunkParser.getArena();
	for(byte b : POSTREQUEST_CONTENT){
	    assert(chunkedArena[z] == b);
	    z += 1;
	}
	var errorParser = new HttpRequestParser();
	var errorParserState = errorParser.parse(chunkError);
	assert(errorParserState == ParserState.ERROR); 

	
    }
}
