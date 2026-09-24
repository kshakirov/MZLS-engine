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
    public HttpRequestParser(WirthHttpParser wirthHttpParser){
	this.wirthHttpParser = wirthHttpParser;

	//	offsetTable = new int[64];
	arena = new byte[1024];

	consumedBytes =0;
	contentLength =0;
	headerStatus = STATUS.REQ_METHOD;
	phase = Phase.HEADER;
	this.automaton = new HttpBodyParserAutomaton(State.PARSE_HEADERS,
						     NetworkInput.HEADERS_PARSED_CONTENT_LENGTH,
						     contentLength,
						     null,
						     consumedBytes,
						     arena
														 
						     );
	
    };

    public long getCotentLength(){
	return this.contentLength;
    };


    public ParserState  parse(byte[] fragment){
	//somewhere to accumulate the whole body


	if(phase == Phase.HEADER){
	    for (int i =0; i < fragment.length; i++){
		headerStatus = wirthHttpParser.parse(fragment[i]);
		
		if(headerStatus == STATUS.FINISHED){

		    break;
		}else if(headerStatus== STATUS.ERROR){
		    return ParserState.ERROR;
		}
	    }
	    
	    if(wirthHttpParser.getBodyType()== BodyType.FIXED_CONTENT){
		contentLength = wirthHttpParser.getContentLength();
		byte[] arena = new byte[contentLength];
		System.out.println("content length " + contentLength);
		
		phase = Phase.BODY;
	    }

	}
	    

	if(phase == Phase.BODY){
	    System.out.println("Here we area");
	    
	    for(int i=0;i <fragment.length; i++){
		this.arena = automaton.runEngine(fragment[i]);
		this.arenaPtr = automaton.getArenPtr();
		var state = automaton.getStatus();
		if(state!= State.SUCCESS && state!= State.ERROR){
		
		    //		return ParserState.NEEDS_MORE_DATA;
		    System.out.println(state);
		
		}
	    }
	}

    
	
	return ParserState.FINISH;
    }

    public byte[] getArena(){
	return this.arena;
    }
    public int getArenaPtr(){
	return this.arenaPtr;
    }
}
