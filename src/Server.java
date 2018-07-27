import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(12345));

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            System.out.println("Searching requests");
            int sockets = selector.select();
            System.out.println("Found request");

            Set<SelectionKey> keys = selector.selectedKeys();

            for (SelectionKey key : keys) {
                if ((key.readyOps() & SelectionKey.OP_ACCEPT)  == SelectionKey.OP_ACCEPT) {
                    System.out.println("Accept request found");
                    ServerSocketChannel channel = (ServerSocketChannel)key.channel();

                    SocketChannel socketChannel = channel.accept();
                    socketChannel.configureBlocking(false);

                    socketChannel.register(selector, SelectionKey.OP_READ);
                    keys.remove(key);
                } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                    System.out.println("READING CONTENT:");
                    SocketChannel channel = (SocketChannel)key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    channel.read(buffer);
                    buffer.flip();

                    CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
                    String message = new String(charBuffer.array());
                    System.out.println(message);

                    charBuffer.clear();
                    channel.close();
                    keys.remove(key);
                    key.cancel();
                }
            }
        }
    }
}
