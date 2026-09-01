package nano.engine.kernel;


import java.nio.charset.StandardCharsets;

import nano.engine.kernel.WirthHttpParser;
import nano.engine.kernel.WirthHttpParser.STATUS;
import nano.engine.kernel.WirthHttpParser.WirthParsedData;
public  class TestWirthHttpParser{
    private static byte[] standardGet = (
					 "GET /index.html HTTP/1.0\r\n" +
					 "Host: localhost\r\n" +
					 "Connection: close\r\n" +
					 "\r\n"
					 ).getBytes(java.nio.charset.StandardCharsets.UTF_8);


    private static byte[] shortGet = (
				      "GET /tell HTTP/1.0\r\n" +
				      "\r\n"
				      ).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static byte[] heavyGet = (
				      "GET /api/v1/users/profile?id=42 HTTP/1.1\r\n" +
				      "Host: 127.0.0.1\r\n" +
				      "User-Agent: wrk/4.2.0\r\n" +
				      "Accept: */*\r\n" +
				      "X-Real-IP: 192.168.1.100\r\n" +
				      "Connection: keep-alive\r\n" +
				      "\r\n"
				      ).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    public static void dumpOffsets(byte[] payload, int[] offsets) {
	for (int j = 0; j < 6; j+=2) {
	    int start = offsets[j];
	    int end = offsets[j + 1];
	    int length = end - start; 
	    System.out.write(payload, start, length);
	    System.out.println("");
	
	}

    
    }
    public static void dumpHeader(byte[] payload, int[] offsets, int start, int end){
	for(int j= start;j <= end /4 + 4;j+=4){
	    int startN = offsets[j];
	    int endN = offsets[j + 1];
	    int startV = offsets[j+2];
	    int endV = offsets[j + 3];

	    int lengthN = endN - startN;
	    int lengthV = endV - startV;
	    
	    System.out.write(payload, startN, lengthN);
	    System.out.write(payload, startV, lengthV);

	    System.out.println("");
	}
    }



    public static void main(String[] args){

	var parser = new WirthHttpParser();
	var data = new WirthHttpParser.WirthParsedData(STATUS.REQ_METHOD,0,0, new int[64]);
	var wholeResult = parser.parse(standardGet,data);
	var streamedResult  = new WirthHttpParser.WirthParsedData(STATUS.REQ_METHOD,0,0, new int[64]);
	var payload = standardGet;
	for (int i =0; i< payload.length ; i++){
	    streamedResult =  parser.parse(new byte[]{payload[i]}, streamedResult);
	    System.out.printf("consumedBytes %d nextOffsetIds %d Status %s\n", streamedResult.consumedBytes(), streamedResult.nextOffsetIdx(), streamedResult.status());
	}
	assert(wholeResult.offsetsTable() != streamedResult.offsetsTable());
	assert(wholeResult.consumedBytes() == streamedResult.consumedBytes());
	assert(wholeResult.nextOffsetIdx() == streamedResult.nextOffsetIdx());
	assert(wholeResult.status() == streamedResult.status());
	assert(wholeResult.offsetsTable().length == streamedResult.offsetsTable().length);
	for(int i=0;i <wholeResult.offsetsTable().length; i++){
	    assert(wholeResult.offsetsTable()[i] == streamedResult.offsetsTable()[i]);
	}
    
    }
     
}
