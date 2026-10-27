package nano.engine.kernel;


import java.io.IOException;

import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nano.engine.kernel.HttpRequestParser.ParserState;
import nano.engine.kernel.HttpBodyParserAutomaton.*;;

public class TestSocketServer {

    private Path path;


      
    public TestSocketServer(String path){

	this.path = Path.of(path);
	try{
	    Files.delete(this.path);

	}catch(IOException exception){
	    
	}

    }
    public void run()throws IOException {
	ServerSocketChannel serChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
	UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(path);
	java.io.File socketFile = new java.io.File(path.toString());
	serChannel.bind(socketAddress);
	socketFile.setWritable(true, false); // false означает "для всех", а не только для владельца
	socketFile.setReadable(true, false);

	//	console.printf("Waiting ...");
	SocketChannel channel;
	ExecutorService exService = Executors.newVirtualThreadPerTaskExecutor();
	while ((channel = serChannel.accept()) != null){
	    MyConnectionHandler handler = new MyConnectionHandler(channel, 1024);
	    exService.execute(handler);
	}
	
	
    }
    class MyConnectionHandler implements Runnable{
	private int buffSize;
	private SocketChannel channel;
	private HttpRequestParser httpRequestParser;
	public MyConnectionHandler(SocketChannel channel, int buffSize){
	    this.channel = channel;
	    this.buffSize = buffSize;
	    this.httpRequestParser = new HttpRequestParser();
						     

	}
	public void run() {
	    byte[] http200 = ("HTTP/1.1 200 OK\r\nContent-Length: 2\r\nConnection: close\r\n\r\nOK")
		.getBytes(StandardCharsets.US_ASCII);
	    byte[] http500 = ("HTTP/1.1 500 Internal Server Error\r\nContent-Length: 0\r\nConnection: close\r\n\r\n")
		.getBytes(StandardCharsets.US_ASCII);
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    ByteBuffer inBuf = ByteBuffer.allocate(this.buffSize);
	    int numBytes;
	    try {
		while ((numBytes = channel.read(inBuf))  != -1) {
		    byte[] bytes = new byte[numBytes];
		    inBuf.flip(); 
		    inBuf.get(bytes);
		    outputStream.writeBytes(bytes);
		    inBuf.clear();
		    ParserState parserState = this.httpRequestParser.parse(bytes);
		    if(parserState == ParserState.FINISH){
			writeFully(http200);
			channel.close();
			break;
		    }else if(parserState == ParserState.ERROR){
			writeFully(http500);
			channel.close();
			break;
		    }
		    
		}

	    }catch(IOException exception){
		
	    }
	}

	private void writeFully(byte[] response) throws IOException {
	    ByteBuffer buffer = ByteBuffer.wrap(response);
	    while (buffer.hasRemaining()) {
		channel.write(buffer);
	    }
	}
    }
}
