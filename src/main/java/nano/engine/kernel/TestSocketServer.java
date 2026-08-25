package nano.engine.kernel;

import java.io.Console;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nano.engine.kernel.SimpleHttpReqParser;

public class TestSocketServer {

    private Path path;
    //private Console console ;

      
    public TestSocketServer(String path){

	this.path = Path.of(path);
	try{
	    Files.delete(this.path);

	}catch(IOException exception){
	    
	}
	//this.console = System.console();


    }
    public void run()throws IOException {
	ServerSocketChannel serChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
	UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(path);
	java.io.File socketFile = new java.io.File(path.toString());
	socketFile.setWritable(true, false); // false означает "для всех", а не только для владельца
	socketFile.setReadable(true, false);
	serChannel.bind(socketAddress);
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
	//private Console console;
	public MyConnectionHandler(SocketChannel channel, int buffSize){
	    this.channel = channel;
	    this.buffSize = buffSize;
	    //	    this.console = System.console();
	}
	public void run() {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    ByteBuffer inBuf = ByteBuffer.allocate(this.buffSize);
	    int numBytes;
	    long threadId = Thread.currentThread().getId();

	    
	    //System.out.println("Task running on Thread ID: " + threadId);
	    try {
		while ((numBytes = channel.read(inBuf))  != -1) {
		    byte[] bytes = new byte[numBytes];
		    inBuf.flip(); 
		    inBuf.get(bytes);
		    outputStream.writeBytes(bytes);
		    String message = new String(bytes); 
		    //		    System.out.printf("[Incoming] %s\n", message);
		    inBuf.clear();
		    var thisIsTheEnd = SimpleHttpReqParser.isFinished(outputStream);
		    if(thisIsTheEnd != null){
			//console.printf("Recieved end of http req, breaking ..\n");
			var okResp = SimpleHttpReqParser.isOK(thisIsTheEnd);
			for( String h:thisIsTheEnd){
			    //console.printf("%s\n", h);
			}
			channel.write(ByteBuffer.wrap(okResp));
			//console.printf("this string will be output %s \n", new String(okResp));
			channel.close();
			break;
		    }else{
			//console.printf("No end of req yet ..\n");
			break;
		    }
		    
		}
		//console.printf("Leaving the cycle\n");
	    }catch(IOException exception){
		
	    }
	}
    }
}
