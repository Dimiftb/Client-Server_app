import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class Client<E, T> implements Runnable {

    private List<E> data;
    private List<T> result;
    private Function<E, T> function;
    private Socket socket;
    private InetAddress inetAddress;
    private int port;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public Client(List<E> data, Function<E, T> function, InetAddress inetAddress, int port) throws IOException {
        this.data = data;
        this.inetAddress = inetAddress;
        this.port = port;
        this.function = function;
        result = new ArrayList<>();
        socket = new Socket(inetAddress, port);


    }

    public void sendResult() throws IOException {
        //send input serv
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(function);
        objectOutputStream.flush();
        objectOutputStream.writeObject(data);
        objectOutputStream.flush();
    }

    public List<T> getResult() throws IOException, ClassNotFoundException {
        result();
        return result;
    }

    public void result() throws ClassNotFoundException, IOException {
        //get output serv
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        result = (List<T>) objectInputStream.readObject();


    }

    @Override
    public void run() {
        try {
            sendResult();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
