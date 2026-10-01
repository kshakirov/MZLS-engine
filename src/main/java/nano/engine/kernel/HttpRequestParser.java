package nano.engine.kernel;
import nano.engine.kernel.WirthHttpParser.STATUS;

import nano.engine.kernel.WirthHttpParser.BodyType;
import java.util.Arrays;


import nano.engine.kernel.HttpBodyParserAutomaton.State;
import nano.engine.kernel.HttpBodyParserAutomaton.NetworkInput;
import nano.engine.kernel.HttpBodyParserAutomaton;

public class HttpRequestParser {
    public enum Phase {
	FINISHED,
	HEADER,
	BODY
	
    }
    public enum ParserState {
	START,
	FINISH,
	ERROR,
	NEEDS_MORE_DATA

    }


    private WirthHttpParser wirthHttpParser;
    private byte[] arena;
    private int arenaPtr;
    private int consumedBytes; //index
    private int contentLength;
    private WirthHttpParser.STATUS headerStatus;
    private HttpBodyParserAutomaton automaton;
    private Phase phase;

    
    //    private 
    public HttpRequestParser(WirthHttpParser wirthHttpParser,HttpBodyParserAutomaton automaton){
	this.wirthHttpParser = wirthHttpParser;
	this.automaton = automaton;

	arena = new byte[1024];

	consumedBytes =0;
	contentLength =0;
	headerStatus = STATUS.REQ_METHOD;
	phase = Phase.HEADER;
	
    };

    public long getCotentLength(){
	return this.contentLength;
    };


    public ParserState  parse(byte[] fragment){
	//somewhere to accumulate the whole body
	var resultState = ParserState.START;
	int i = 0;
	if(phase == Phase.HEADER){
	    for (; i < fragment.length; i++){
		headerStatus = wirthHttpParser.parse(fragment[i]);
		
		if(headerStatus == STATUS.FINISHED){

		    break;
		}else if(headerStatus== STATUS.ERROR){
		    return ParserState.ERROR;
		}
	    }
	    if(headerStatus == STATUS.FINISHED){
	    
		if(wirthHttpParser.getBodyType()== BodyType.FIXED_CONTENT){
		    contentLength = wirthHttpParser.getContentLength();
		    byte[] arena = new byte[contentLength];
		    System.out.println("content length " + contentLength);
		    phase = Phase.BODY;
		}else if(wirthHttpParser.getBodyType()== BodyType.CHUNK_CONTENT){
		    System.out.println("");
		}
	    }else{
		return ParserState.NEEDS_MORE_DATA;
	    }
	    
	}


	if(phase == Phase.BODY){
	   	    
	    for(;i < fragment.length; i++){
		if(fragment[i]==10){
		    continue;
		}
		this.arena = automaton.runEngine(fragment[i]);
		this.arenaPtr = automaton.getArenPtr();
		var state = automaton.getStatus();
		if(state!= State.SUCCESS && state!= State.ERROR){
		    
		    System.out.println(state + " cv " + automaton.getArenPtr() );
		    resultState = ParserState.NEEDS_MORE_DATA;
		 
		}else{
		    resultState = ParserState.FINISH;
		}
		System.out.println("Final automaton state is " + state);
		
	    }
	    
	    
	    
	}

    
	
	return resultState;
    }

    public byte[] getArena(){
	return this.arena;
    }
    public int getArenaPtr(){
	return this.arenaPtr;
    }
}
