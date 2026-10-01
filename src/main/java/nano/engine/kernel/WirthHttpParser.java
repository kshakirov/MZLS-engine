package nano.engine.kernel;


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
    public enum BodyType {
	FIXED_CONTENT("Content-Length".getBytes()),
	CHUNK_CONTENT("Transfer-Encoding".getBytes()),
	NONE("".getBytes());
	BodyType(byte[] value){this.value = value;}
	private final byte[] value;
	public byte[] bValue(){return this.value;}
    }

    private STATUS status;
    private  int nextOffsetIdx;
    private int consumedBytes;
    private int[] offsets;
    private BodyType bodyType;
    private int fixed_content_match;
    private int fixed_content_value;
    private int chunk_content_match;
    private int content_length;
    private final int FIXED_CONTENT_LENGTH=14;
    private final int  CHUNK_CONTENT_LENGTH= 17;
    public WirthHttpParser (){
	this.status = STATUS.REQ_METHOD;
	this.nextOffsetIdx =0;
	this.consumedBytes =0;
	this.offsets = new int[64];
	this.bodyType = BodyType.NONE;
	this.fixed_content_match=0;
	this.chunk_content_match=0;
	this.fixed_content_value=0;
	this.content_length = 0;


    }
  
    

    public STATUS parse(byte payload){
	var index = 0;
	
	    switch(status){
	    case REQ_METHOD: {
		switch(payload)  {
		case 0x20: {
		    status = STATUS.REQ_URI;
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
		switch(payload){
		case 0x20 :{
		    status = STATUS.REQ_VERSION;
		    break;

		}
		default: {

		    break;

		}
		    
		}
		break;
	    }
	    case REQ_VERSION :{
		switch(payload){
		case 0x0D :{
		    status = STATUS.CHECK_NEXT_LINE;
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
		switch(payload){
		case 0x0A :{
		    status = STATUS.HEADER_NAME;
		    nextOffsetIdx += 1;//convention each header part increments offset for itself
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
		switch(payload){
		case 0x0D :{
		    status = STATUS.CHECK_CRLF;
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
	
		    if(payload == BodyType.FIXED_CONTENT.bValue()[fixed_content_match]){
			fixed_content_match += 1;
			if(fixed_content_match == FIXED_CONTENT_LENGTH){
			    this.bodyType = BodyType.FIXED_CONTENT;
			}
		    }else{
			fixed_content_match = 0;
			//TODO: make failed field in case "XXXXXCONTENT-LENGTHXXXX"
		    }

		    if(payload == BodyType.CHUNK_CONTENT.bValue()[chunk_content_match]){

			chunk_content_match += 1;
			if(chunk_content_match == CHUNK_CONTENT_LENGTH){
			    this.bodyType = BodyType.CHUNK_CONTENT;
			}
		    }else{
			chunk_content_match = 0;
			//TODO: make failed field in case "XXXXXCONTENT-LENGTHXXXX"
		    }

		    break;
		}

		}
		    break;	       
	    }

	    case CHECK_CRLF: {

		switch (payload){

		    
		case 0x0A:{

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
		switch(payload){
		case 0x0D :{

		    status = STATUS.CHECK_NEXT_LINE;
		    nextOffsetIdx += 1;

		    break;
		}
		default: {
		    //for the time being
		    if(this.bodyType==BodyType.FIXED_CONTENT  && payload > 47 && payload <58){
			fixed_content_match = 0;
			content_length = content_length * 10 + (payload - '0');
		    }
		    if(this.bodyType==BodyType.CHUNK_CONTENT){
			fixed_content_match = 0;
		    }

				    
		    break;
		}

		}
		    break;	       
	    }	
		

	    case STATUS.FINISHED: {

		consumedBytes += index + 1;
		//	System.out.println("STATUS.FINISH: reading consumedBytes " + consumedBytes);
		return status;
	    }

	    case ERROR:{
		//		console.printf("STATUS.ERROR: reading \n");
		consumedBytes += index;
		return status;
	    }
	    }
	

	       
	
	return status;
	
    }

    public int getContentLength(){
	return this.content_length;
    }
    public BodyType getBodyType(){
	return this.bodyType;
    }
   
}
