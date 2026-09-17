package nano.engine.kernel;



public class HttpBodyParserAutomaton {

    public enum  State {
	PARSE_HEADERS,
	EXPECT_CHUNK_SIZE,
	READ_CHUNK_DATA,
	SUCCESS,
	ERROR,
	EXPECT_CHUNK_CR,
	EXPECT_CHUNK_LF,
	READ_CHUNK_CR,
	READ_CHUNK_LF;
    }

    public enum NetworkInput{
	CHUNK_SIZE_GREATER_ZERO ,
	CHUNK_SIZE_ZERO,
	DATA_ARRIVED,
	MALFORMED,
	READING_FIXED_DATA,
	HEADERS_PARSED_EMPTY,
	HEADERS_PARSED_CONTENT_LENGTH,
	HEADERS_PARSED_CHUNKED,
	CHUNK_DATA_FLOW,
	CHUNK_DATA_EMPTY,
	CRLF_VALID,
	CR_AFTER_SIZE_VALID,
	CR_AFTER_DATA_VALID,
	CR_AFTER_ZERO_VALID,
	LF_AFTER_SIZE_VALID,
	LF_AFTER_DATA_VALID,
	LF_AFTER_ZERO_VALID,
	TIMEOUT;
    }
    private State currentState;
    private NetworkInput currentInput;
    private int currentValue;
    //    private byte[] fragment;
    private int bufferPtr;
    private byte[] arena;
    private int arenaPtr;
    private final int[] registers = new int[3];

    public HttpBodyParserAutomaton(State state, NetworkInput input, int value, byte[] buf, int bufPtr, byte[] arena){
	currentState = State.PARSE_HEADERS;
	currentInput = input;
	currentValue = value;
	//	fragment = buf;
	bufferPtr = bufPtr;
	this.arena = arena;
    }
    
    private void nextState(){
	switch(currentState){
	case State.PARSE_HEADERS:{
	    if (currentInput == NetworkInput.HEADERS_PARSED_EMPTY)
		{
		    currentState = State.SUCCESS;
		    return;
		}
	    else if (currentInput == NetworkInput.HEADERS_PARSED_CONTENT_LENGTH){

		currentState = State.READ_CHUNK_DATA;
		currentInput = NetworkInput.READING_FIXED_DATA;
		return;
		 
	    }
	    break;
	}
	case State.READ_CHUNK_DATA: {
	    if (currentInput == NetworkInput.READING_FIXED_DATA && currentValue > 0){

		currentState =  State.READ_CHUNK_DATA;
		return;
	    }
	    else if (currentInput == NetworkInput.READING_FIXED_DATA && currentValue ==0){

		currentState =  State.SUCCESS;
		return;
	    }
	    break;
	}
        default: {
	    
	}
	}
    }
    public State getStatus(){
	return this.currentState;
    }
    public void resetBufferPointer(){
	this.bufferPtr=0;
    }
    public void resetArenaPointer(){
	this.arenaPtr=0;
    }
    public int getArenPtr(){
	return this.arenaPtr;
    }
			   
    public byte[] runEngine(byte[]fragment){
	//	this.fragment= fragment;

	int counter = 0;
	while(counter >= 0) {
	    switch(currentState){
 	    case State.SUCCESS:{
		return arena;
	    }
	    case State.ERROR:{
		return arena;
	    }
	    case State.READ_CHUNK_DATA :{
		//System.out.println("runEnginge: state is READ CHUNK arena ptr "+ arenaPtr + " buffer ptr " + bufferPtr );
		var regs = readChunkFixedLength(currentValue, fragment, bufferPtr, arena ,arenaPtr);
		currentState = State.READ_CHUNK_DATA;
		currentInput = NetworkInput.READING_FIXED_DATA;
		bufferPtr = regs[1];
		arena[arenaPtr] = fragment[arenaPtr];
		
		arenaPtr = regs[2];

		//System.out.println("runEnginge: state is READ CHUNK, bufferPtr " + bufferPtr + " current value " +  currentValue + " bufferLen " + fragment.length + "  " + regs[0] + " arena ptr "+ arenaPtr);
		if(regs[0] > 0){
		    currentValue =  regs[0];
		    return arena;
		}else{
		    currentValue = 0;
		}
	    }
	    default: {
		//return State.ERROR;
		//System.out.println("runEnginge: Nothing yet found state is " + currentState + " input is " + currentInput);
	    }
	      
	    }
	    nextState();
	}
	return arena;
    }
    private int[] readChunkFixedLength(int value, byte[] buf , int bufPtr, byte[] a, int aPtr){
	if(bufPtr <= buf.length){
	    while (bufPtr < buf.length && value >  0){
		if(bufPtr == -1)
		    bufPtr = 0;
		a[aPtr] = buf[bufPtr];
		bufPtr += 1;
		aPtr +=1;
		value -=1;
	
	    }
	    registers[0] = value;
	    registers[1] = bufPtr;
	    registers[2] = aPtr;
	    return registers;


	}else {
	    registers[0] = value;
	    registers[1] = bufPtr;
	    registers[2] = aPtr;
	    return registers;

	    
	}
    }

}
