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
    private long content_length;
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
  
    

    public STATUS  parse(byte[] payload){
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

		    if(payload[index] == BodyType.FIXED_CONTENT.bValue()[fixed_content_match]){

			System.out.println("header name " + payload[index] + " match is " + fixed_content_match);
			fixed_content_match += 1;
			if(fixed_content_match == 14){
			    this.bodyType = BodyType.FIXED_CONTENT;
			}
		    }else{
			fixed_content_match = 0;
		    }

		    if(payload[index] == BodyType.CHUNK_CONTENT.bValue()[chunk_content_match]){

			System.out.println("header name " + payload[index] + " match is " + chunk_content_match);
			chunk_content_match += 1;
			if(chunk_content_match == 17){
			    this.bodyType = BodyType.CHUNK_CONTENT;
			}
		    }else{
			chunk_content_match = 0;
		    }

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
		    if(this.bodyType==BodyType.FIXED_CONTENT){
			content_length = content_length * 10 + (payload[index] - '0');
		    }
		    
		    break;
		}

		}
		    break;	       
	    }	

		

	    case STATUS.FINISHED: {
		//		console.printf("STATUS.FINISH: reading \n");
		return status;
	    }

	    case ERROR:{
		//		console.printf("STATUS.ERROR: reading \n");

		return status;
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
    };
    public long getContentLength(){
	return this.content_length;
    }
    public BodyType getBodyType(){
	return this.bodyType;
    }
   
}
