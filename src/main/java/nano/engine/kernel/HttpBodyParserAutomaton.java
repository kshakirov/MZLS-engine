package nano.engine.kernel;



public class HttpBodyParserAutomaton {

    public enum  State {

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
	currentState = state;
	currentInput = input;
	currentValue = value;
	//	fragment = buf;
	arenaPtr = 0;
	this.arena = arena;
    }
    
    private void nextState(){
	switch(currentState){

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
    
			   
    public byte[] runEngine(byte fragment){
	int counter = 0;
	//System.out.println(fragment);
	switch(currentState){
	case State.SUCCESS:{
	    return arena;
	}
	case State.ERROR:{
	    return arena;
	}
	case State.READ_CHUNK_DATA :{
	    currentValue -= 1;
	    currentState = State.READ_CHUNK_DATA;
	    currentInput = NetworkInput.READING_FIXED_DATA;
	    arena[arenaPtr] = fragment;
	    arenaPtr += 1;

	    //System.out.println("CV " + this.currentValue);
	    
	   
	}
	default: {
	    //return State.ERROR;
	    //System.out.println("runEnginge: Nothing yet found state is " + currentState + " input is " + currentInput);
	    break;
	}
	      
	}
	nextState();

	return arena;
    }
   
}
