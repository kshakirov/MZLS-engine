package nano.engine.kernel;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.nio.ByteBuffer;

public class WirthHttpParser{
    public enum STATUS {
	REQ_START(0),
	REQ_METHOD(1),
	REQ_URI(2),
	REQ_VERSION(3),
	HEADER_START(4),
	HEADER_NAME(5),
	HEADER_VALUE(6),
	FINISHED   (7),
	CHECK_NEXT_LINE(9),
	REQ_URI_START(8);
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
	var status = STATUS.REQ_START;
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
	    case STATUS.REQ_START: {
		status = STATUS.REQ_METHOD;
		methodStart = i;
	    };
		break;
	    case STATUS.REQ_METHOD: {
		if(i==0 && i !=0x20){
		}
		if(i==3){
		    var method = isGetOrPut(payload);
		    if(method){
			console.printf("STATUS.REQ_METHOD FOUND\n");
			offsets[0] = methodStart; //first offset for method
			methodEnd= i - 1;
			offsets[1] = methodEnd;
			ddos =false;
			status = STATUS.REQ_URI;
		    }else if(i==4) {
			
		    }else if(i==5){

		    }
		    else if(i==6){
			
		    }else if(i==7){

		    }else if(i> 7){

		    }
		}
		    
	    }
	
	    case STATUS.REQ_URI_START: {
		if(payload[i]==0x20){
		    console.printf("STATUS.REQ_URI_START FOUND\n");
		    status = STATUS.REQ_URI;
		    uriStart = i + 1;
		    offsets[2] = uriStart;
		}
		break;
		
	    }
	    case STATUS.REQ_URI: {
		if(payload[i]!=0x20){
		    uriEnd = i;
		    offsets[3]= uriEnd;
		    
		}else{
		    console.printf("STATUS.REQ_URI FOUND\n");
		    status = STATUS.REQ_VERSION;
		    versionStart = i + 1;
		    offsets[4] = versionStart;
		}
		break;
	    }
		
	    case STATUS.REQ_VERSION: {
		if(payload[i]!=0x0D){
		   
		    versionEnd = i;
		}else if(payload[i]==0x0D){
		     console.printf("STATUS.REQ_VERSOIN FOUND\n");
		     offsets[5]=versionEnd;
		     status = STATUS.CHECK_NEXT_LINE;
		};
		break;
		
	    }
		
	    case STATUS.CHECK_NEXT_LINE: {
		if(payload[i]==0x0A){
		    console.printf("STATUS.CHECK_NEXT_LINE: newline  found\n");
		    status = STATUS.CHECK_NEXT_LINE;
		}else if(payload[i]==0x0D){
		    console.printf("STATUS.CHECK_NEXT_LINE: carriage return found, this means the edn of the request, quitting ...\n");
		    status = STATUS.FINISHED;
		}else{
		    console.printf("STATUS.CHECK_NEXT_LINE: neither carriage return no newline found, this means there are headers \n");
		    status = STATUS.HEADER_START;
		    
		}
		break;
	    }
	    case STATUS.HEADER_START: {
		if(payload[i]==0x0A){
		    console.printf("STATUS.HEADER_START FOUND\n");
		    status = STATUS.HEADER_NAME;
		    nextOffsetIdx = 6;
		    offsets[nextOffsetIdx]= i;
		    


		}
		break;
		
	    }
	    case STATUS.HEADER_NAME: {
		if(payload[i]!=0x3A){

		    //status = STATUS.HEADER_NAME;

		}else{
		    console.printf("STATUS.HEADER_NAME FOUND  \n");


		    status= STATUS.HEADER_VALUE;
		   
		}   

		break;
		
	    }
	    case STATUS.HEADER_VALUE: {
		if(payload[i]!=0x0A) {

		}else{
		    console.printf("STATUS.HEADER_VALUE FOUND\n");

		    status = STATUS.HEADER_NAME;
		}
		break;
		
	    }
	    case STATUS.FINISHED: {
		console.printf("STATUS.FINISH: returning\n");
		//break;
		return offsets;
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
