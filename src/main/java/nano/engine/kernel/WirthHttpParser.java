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
	CHECK_CRLF(12),
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
    private STATUS status;
    private  int nextOffsetIdx;
    private int consumedBytes;
    private int[] offsets; 
    public WirthHttpParser (){
	this.status = STATUS.REQ_METHOD;
	this.nextOffsetIdx =0;
	this.consumedBytes =0;
	this.offsets = new int[64];
    }
  
    public record WirthParsedData(STATUS status, int nextOffsetIdx, int consumedBytes, int[] offsetsTable){};
    public STATUS  parse(byte[] payload){
	//	var payload = stream.toByteArray();
	//	var status = wirthParsedData.status();
	//var nextOffsetIdx = wirthParsedData.nextOffsetIdx();
	var index = 0;
	

	
	//var offsets = wirthParsedData.offsetsTable();// 127 method, uri,headers should be enough 
	for (; index < payload.length; index++){
	    switch(status){
	    case REQ_METHOD: {
		switch(payload[index])  {
		case 0x20: {
		    status = STATUS.REQ_URI;
		    offsets[1]= index + consumedBytes; //end of method
		    offsets[2] = index + 1+ consumedBytes; //start of uri
		    break;
		}
		default: {
		    //		    offsets[1] = i;
		    break;
		}
		}
		break;
			
	    }
	    case REQ_URI :{
		switch(payload[index]){
		case 0x20 :{
		    status = STATUS.REQ_VERSION;
		    offsets[3]= index + consumedBytes; //the same logic as above
		    offsets[4] = index + 1 + consumedBytes;
		    break;

		}
		default: {

		    break;

		}
		    
		}
		break;
	    }
	    case REQ_VERSION :{
		switch(payload[index]){
		case 0x0D :{
		    status = STATUS.CHECK_NEXT_LINE;
		    offsets[5]= index + consumedBytes;//end of version
		    nextOffsetIdx = 5;
		    break;
		}
		default: {

		    break;

		}

		}
		break;
	    }
	    case CHECK_NEXT_LINE: {
		switch(payload[index]){
		case 0x0A :{
		    //console.printf("STATUS.NEXTL LINE : HEADER NAME STARTED i[%d] \n", i);
		    status = STATUS.HEADER_NAME;
		    nextOffsetIdx += 1;//convention each header part increments offset for itself
		    offsets[nextOffsetIdx] = index + 1 + consumedBytes;
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
		switch(payload[index]){
		case 0x0D :{
		    status = STATUS.CHECK_CRLF;
		    //		    console.printf("Going to CRLF\n");
		    break;
		}
		case 0x3A:{
		    status = STATUS.HEADER_VALUE;
		    nextOffsetIdx += 1;
		    offsets[nextOffsetIdx] = index + consumedBytes;//the end exclusive of Header name
		    nextOffsetIdx += 1;
		    offsets[nextOffsetIdx] = index + 1 + consumedBytes;//the start of header value inclusive

		    break;
		}
		default: {
		    //for the time being
		    

		    break;
		}

		}
		    break;	       
	    }

	    case CHECK_CRLF: {

		switch (payload[index]){

		    
		case 0x0A:{
		    //		    console.printf("current %d\n", payload[i]);
		    status = STATUS.FINISHED;
		    break;
		}
		default:{
		    status = STATUS.ERROR;
		    break;
		    
		}
		}
		break;
	    }

	    case HEADER_VALUE: {
		switch(payload[index]){
		case 0x0D :{
		    //		    console.printf("STATUS.HEADER VALUE : HEADER VALUE FINISHED i[%d] \n", i);
		    status = STATUS.CHECK_NEXT_LINE;
		    nextOffsetIdx += 1;
		    offsets[nextOffsetIdx] = index + consumedBytes; //the end of header value exclusive
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
		//		console.printf("STATUS.FINISH: reading \n");
		break;
		//return offsets;
	    }

	    case ERROR:{
		//		console.printf("STATUS.ERROR: reading \n");
		break; //только чтобы не подсвечило пока статус как ошибка
	    }
	    }
	

	       
	}
	consumedBytes += index;
	return status;
	
    }
    public int[] getOffsetTable(){
	return this.offsets;
    }
    public int getNextOffsetIdx(){
	return this.nextOffsetIdx;
    }
    public int getConsumedBytes(){
	return this.consumedBytes;
    }
   
}
