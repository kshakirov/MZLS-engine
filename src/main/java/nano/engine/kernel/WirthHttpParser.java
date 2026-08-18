package nano.engine.kernel;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;



import java.nio.ByteBuffer;

public class WirthHttpParser{
    public enum STATUS {

	REQ_METHOD(1),
	REQ_URI(2),
	REQ_VERSION(3),
	HEADER_START(4),
	HEADER_NAME(5),
	HEADER_VALUE(6),
	FINISHED   (7),
	CHECK_NEXT_LINE(9),
	CHECK_CARRIAGE(10),
	REQ_URI_START(8),
	ERROR(11);
	STATUS(int value) {this.value=value;};
	private final int value;
	public int value() {return value;}
    }
    public enum METHODS{
	GET("GET"),
	HEAD("HEAD"),
	POST("POST"),
	PUT("GET");
	METHODS(String value){this.value=value;}
	private final String value;
	public String value() {return value;}
	public byte[] bValue() {return value.getBytes();}
    }
    
    public int[]  parse(byte[] payload){
	//	var payload = stream.toByteArray();
	var status = STATUS.REQ_METHOD;
	var console = System.console();
	var methodStart=0;
	var methodEnd=0;
	var uriStart=0;
	var uriEnd =0;
	var versionStart =0;
	var versionEnd=0;
	var nextOffsetIdx =0;
	var ddos = true;

	
	var offsets = new int[32];// 127 method, uri,headers should be enough 
	for (int i=0; i < payload.length; i++){
	    switch(status){
	    case REQ_METHOD: {
		switch(payload[i])  {
		case 0x20: {
		    status = STATUS.REQ_URI;
		    offsets[2] = i + 1;
		    break;
		}
		default: {
		    offsets[1] = i;
		    break;
		}
		}
		break;
			
	    }
	    case REQ_URI :{
		switch(payload[i]){
		case 0x20 :{
		    status = STATUS.REQ_VERSION;
		    offsets[4] = i + 1;
		    break;

		}
		default: {
		    offsets[3]= i;
		    break;

		}
		    
		}
		break;
	    }
	    case REQ_VERSION :{
		switch(payload[i]){
		case 0x0D :{
		    status = STATUS.CHECK_NEXT_LINE;
		    nextOffsetIdx = 6;
		    break;
		}
		default: {
		    offsets[5]= i;
		    break;

		}

		}
		break;
	    }
	    case CHECK_NEXT_LINE: {
		switch(payload[i]){
		case 0x0A :{
		    status = STATUS.HEADER_NAME;
		    offsets[nextOffsetIdx] = i + 1;
		    break;
		}
		default: {
		    status = STATUS.ERROR;
		    break;

		}
		}
		break;
	       
	    }



	    case HEADER_NAME: {
		switch(payload[i]){
		case 0x0D :{
		    status = STATUS.FINISHED;
		    offsets[nextOffsetIdx] = i + 1;
		    break;
		}
		default: {
		    //for the time being
		    status = STATUS.FINISHED;
		    break;
		}

		}
		    break;	       
	    }	
		

	    case STATUS.FINISHED: {
		console.printf("STATUS.FINISH: reading \n");
		break;
		//return offsets;
	    }
	    }
	

	       
	}
	    
	return offsets;
	
    }
    private boolean isGetOrPut(byte[] payload){
	//we know exactly the possible length of method
	if(fullMatch(METHODS.GET.bValue(),payload, 0)){
	    return true;
	}
	if(fullMatch(METHODS.PUT.bValue(),payload, 0)){
	    return true;
	}

		
	return false;
    }

    private boolean fullMatch(byte[] template, byte[] candidate, int candiadateOffset){
	var length = template.length;
	for (int i =0;i< length;i++){
	    if(template[i]!=candidate[candiadateOffset + i]){
		return false;
	    }
	}
	return true;
    }
    

    private String sliceByteArray(int start, int end, byte[] payload){
	ByteBuffer sliceBuffer = ByteBuffer.wrap(payload, start, end).slice();
	return sliceBuffer.toString();
    }

}
