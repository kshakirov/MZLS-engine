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
	HEADER_NAME(5),
	HEADER_VALUE(6),
	FINISHED   (7),
	CHECK_NEXT_LINE(9),
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
	var nextOffsetIdx =0;


	
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
		    nextOffsetIdx = 5;
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
		    console.printf("STATUS.NEXTL LINE : HEADER NAME STARTED i[%d] \n", i);
		    status = STATUS.HEADER_NAME;
		    nextOffsetIdx += 1;//convention each header part increments offset for itself
		    offsets[nextOffsetIdx] = i + 1;
		    //nextOffsetIdx += 1;
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

		    break;
		}
		case 0x3A:{
		    status = STATUS.HEADER_VALUE;
		    nextOffsetIdx += 1;
		    offsets[nextOffsetIdx] = i;//the end exclusive of Header name
		    nextOffsetIdx += 1;
		    offsets[nextOffsetIdx] = i + 1;//the start of header value inclusive

		    break;
		}
		default: {
		    //for the time being
		    

		    break;
		}

		}
		    break;	       
	    }

	    case HEADER_VALUE: {
		switch(payload[i]){
		case 0x0D :{
		    console.printf("STATUS.HEADER VALUE : HEADER VALUE FINISHED i[%d] \n", i);
		    status = STATUS.CHECK_NEXT_LINE;
		    nextOffsetIdx += 1;
		    offsets[nextOffsetIdx] = i; //the end of header value exclusive
		    break;
		}
		default: {
		    //for the time being

		    
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
