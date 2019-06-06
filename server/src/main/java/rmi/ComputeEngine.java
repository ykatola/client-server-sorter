package rmi;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;

public class ComputeEngine implements Runnable, Closeable, Compute {

    private int port = 4321;
    private Compute proxy;
    private Registry registry;
    private Timer sessionTimer = new Timer("Session Cleanup Timer", true);

    public ComputeEngine(String[] args) {
        if (args.length > 0) {
            try {
                this.port = Integer.parseInt(args[0]);
            } catch (Exception ex) {
            }
        }
//		super();
    }

    @Override
    public void close() throws IOException {

        if (this.sessionTimer != null) {
            this.sessionTimer.cancel();
            this.sessionTimer = null;
        }

        if (this.registry != null) {
            try {
                this.registry.unbind(RMI_SERVER_NAME);
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
            this.registry = null;
        }

        if (this.proxy != null) {
            UnicastRemoteObject.unexportObject(this, true);
            this.proxy = null;
        }
    }

    @Override
    public long timeProcessMethod(TreeSort quickSort) {
        return TreeSort.timeConsumedMilis;
    }

    @Override
    public void run() {
        try {
            this.proxy = (Compute) UnicastRemoteObject.exportObject(this, this.port);

            this.registry = LocateRegistry.createRegistry(this.port);
            this.registry.bind(RMI_SERVER_NAME, this.proxy);

            System.out.printf("The RMI server was started successfully on the port %s%n", this.port);

        } catch (AlreadyBoundException | RemoteException e) {
            throw new RuntimeException("Failed to start server", e);
        }
    }

    @Override
    public void ping() {
    }

    @Override
    public String echo(String text) {
        return String.format("ECHO: %s", text);
    }

    @Override
    public <T> T executeTask(Task<T> t) throws RemoteException {
        try {
            return t.execute();
        } catch (Exception ex) {
            throw new ServerException("Server failed to process your command", ex);
        }
    }
}
