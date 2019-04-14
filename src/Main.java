
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class Main {


    public static void main(String args[]) {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            data.add(i);
        }

        Function<Integer, Integer> times10 = (Function<Integer, Integer> & Serializable) x -> x = x * 10;
        try {
            Server server = new Server();
            Client client = new Client(data, times10, server.getServerSocketAddress(), server.getServerPort());
            Executor executor = Executors.newCachedThreadPool();
            executor.execute(server);
            Future clients = ((ExecutorService) executor).submit(client);
            if (clients.get() == null) {
                System.out.println(client.getResult());
            }
        } catch (IOException | ClassNotFoundException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


    }
}