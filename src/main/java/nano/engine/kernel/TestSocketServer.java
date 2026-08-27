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
import nano.engine.kernel.WirthHttpParser;
import nano.engine.kernel.HttpRequestParser.ParsedData;
import sun.security.provider.HSS;
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
	    this.wirthParser =new WirthHttpParser();
	    this.httpRequestParser = new HttpRequestParser();

	}
	public void run() {
	    //temporally ofcourse
	    byte[] response = (
			       "HTTP/1.1 200 OK\r\n" +
			       "Content-Type: text/plain\r\n" +
			       "Content-Length: 2\r\n" +
			       "Connection: close\r\n" +
			       "\r\n" +
			       "OK"
			       ).getBytes(java.nio.charset.StandardCharsets.US_ASCII);
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    ByteBuffer inBuf = ByteBuffer.allocate(this.buffSize);
	    int numBytes;
	    long threadId = Thread.currentThread().getId();

	    try {
		while ((numBytes = channel.read(inBuf))  != -1) {
		    byte[] bytes = new byte[numBytes];
		    inBuf.flip(); 
		    inBuf.get(bytes);
		    outputStream.writeBytes(bytes);

		    inBuf.clear();

		    //		    var offsetsTable = wirthParser.parse(bytes);
		    ParsedData parsedData = this.httpRequestParser.parse(bytes);
		    if(parsedData.offsetTable() != null){

			channel.write(ByteBuffer.wrap(response));

			channel.close();
			break;
		    }else{

			break;
		    }
		    
		}

	    }catch(IOException exception){
		
	    }
	}
    }
}
