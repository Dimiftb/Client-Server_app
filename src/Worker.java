import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Worker<E, T> implements Runnable {

    private ServerSocket serverSocket;
    private Socket socket;
    private Function<E, T> function;
    private E data;
    private List<E> result;

    public Worker(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void inputOutput() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream((socket.getOutputStream()));
            //get input server
            function = (Function<E, T>) objectInputStream.readObject();
            data = (E) objectInputStream.readObject();
            //send output server
            objectOutputStream.writeObject(apply());
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<E> apply() {
        result = new ArrayList<>();
        for (E d : (List<E>) data)
            result.add((E) function.apply(d));
        return result;
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public InetAddress getAddress() {
        return serverSocket.getInetAddress();
    }

    @Override
    public void run() {
        try {
            socket = serverSocket.accept();
            inputOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}