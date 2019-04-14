import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class Server<E, T> implements Runnable {
    private ServerSocket serverSocket;
    private Socket socket;
    private List<T> result;
    private List<E> data;
    private Function<E, T> function;
    private int port = 9999;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private int limit;

    public Server() throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public InetAddress getServerSocketAddress() {
        return serverSocket.getInetAddress();
    }

    public int getServerPort() {
        return serverSocket.getLocalPort();
    }

    protected void doSomeWork() {
        try {
            //get input cl
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            function = (Function<E, T>) objectInputStream.readObject();
            data = (List<E>) objectInputStream.readObject();
            objectOutputStream.writeObject(assignjob(function, data));
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<T> assignjob(Function<E, T> function, List<E> data) throws IOException, ClassNotFoundException {
        List<List<E>> chunk = new ArrayList<List<E>>();
        List<Socket> workerSockets = new ArrayList<Socket>();
        List<Future> queue = new ArrayList<>();
        List<T> res = new ArrayList<>();
        int check = 0;
        //split data
        for (int i = 10; i < data.size(); i += 10) {
            chunk.add(data.subList(check, i));
            check += 10;
        }
        chunk.add(data.subList(check, data.size()));
        //assign worker
        limit = chunk.size();
        for (int i = 0; i < chunk.size(); i++) {
            Worker worker = new Worker(port + i + 1);
            Socket workerSocket = new Socket(worker.getAddress(), worker.getPort());
            workerSockets.add(workerSocket);
            Executor executor = Executors.newCachedThreadPool();
            queue.add(((ExecutorService) executor).submit(worker));
            //send input worker
            List<E> list = new ArrayList<>(chunk.get(i));
            objectOutputStream = new ObjectOutputStream(workerSocket.getOutputStream());
            objectOutputStream.writeObject(function);
            objectOutputStream.flush();
            objectOutputStream.writeObject(data);
            objectOutputStream.flush();
        }
        //get results from workers back
        for (int i = 0; i < chunk.size(); i++) {
            if (queue.get(i) != null) {
                res.addAll(Output(workerSockets.get(i)));
            }
        }
        return res;
    }

    public List<T> Output(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream1 = new ObjectInputStream(socket.getInputStream());
        result = (List<T>) objectInputStream1.readObject();
        return result;

    }

    @Override
    public void run() {
        while (true) {
            try {
                socket = serverSocket.accept();
                doSomeWork();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}