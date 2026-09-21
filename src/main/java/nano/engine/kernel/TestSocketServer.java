package nano.engine.kernel;


import java.io.IOException;

import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nano.engine.kernel.HttpRequestParser.ParserState;

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
	private WirthHttpParser wirthParser;
	private HttpRequestParser httpRequestParser;
	public MyConnectionHandler(SocketChannel channel, int buffSize){
	    this.channel = channel;
	    this.buffSize = buffSize;
	    this.httpRequestParser = new HttpRequestParser();

	}
	public void run() {
	    //temporally ofcourse
	    byte [] r_prefix = ("HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\nOK").getBytes();
	    byte [] r_body = ("\r\n\r\n").getBytes();

	    byte[] http200 = ("HTTP/1.1 200 OK\r\n\r\n").getBytes();
	    byte[] http500 = ("HTTP/1.1 500 Internal Server Error\r\n\r\n").getBytes();
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    ByteBuffer inBuf = ByteBuffer.allocate(this.buffSize);
	    var buf = new byte[4];
	    var box = new byte[1];
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
			int ptr =0;
			if((ptr =httpRequestParser.getArenaPtr()) > 0){
			    channel.write(ByteBuffer.wrap(r_prefix));

			    buf[0]= (byte)(ptr >>> 24);
			    buf[1] = (byte)(ptr >>> 16);
			    buf[2] = (byte)(ptr >>> 8);
			    buf[3] = (byte) ptr;
			    channel.write(ByteBuffer.wrap(buf));
			    channel.write(ByteBuffer.wrap(r_body));
			    
			    for(int i =0; i< ptr;i++){
				box[0] = (byte) 65;
				channel.write(ByteBuffer.wrap(box));
			    }
			
			    
			}else{
			    channel.write(ByteBuffer.wrap(http200));
			}
			//channel.write(ByteBuffer.wrap(http200));
			channel.close();
			break;
		    }else if(parserState == ParserState.ERROR){
			channel.write(ByteBuffer.wrap(http500));
			channel.close();
			break;
		    }
		    
		}

	    }catch(IOException exception){
		
	    }
	}
    }
}
