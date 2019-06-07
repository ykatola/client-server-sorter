package rmi;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Optional;
import java.util.Random;

public class RMIClient implements Closeable {

    private Registry registry;
    private Compute proxy;
    private volatile String responseInfo;

    private RMIClient(String hostname, int port) throws RemoteException, NotBoundException {
        this.registry = LocateRegistry.getRegistry(hostname, port);
        this.proxy = (Compute) registry.lookup(Compute.RMI_SERVER_NAME);
    }

    public static RMIClient newRMIClient(String hostname, int port) throws RemoteException, NotBoundException {
        return new RMIClient(hostname, port);
    }

    @Override
    public void close() {
        if (this.proxy == null) {
            this.registry = null;
        } else {
            this.registry = null;
            this.proxy = null;
        }
    }


    public String sendRequest(String command) {
        Optional<ProtocolManager.Request> request = RequestHelper.mapToRequest(command);
        if (!request.isPresent()) {
            return "Unknown command " + command;
        }
        String[] parameters = RequestHelper.splitCommand(command);
        if (parameters.length != request.get().argumentsAmount) {
            throw new RuntimeException("Command " + command + " must contain " + request.get().argumentsAmount + " arguments!");
        }
        try {
            switch (request.get()) {
                case PING:
                    proxy.ping();
                    responseInfo = "Ping was successful";
                    break;
                case ECHO:
                    responseInfo = proxy.echo(parameters[0]);
                    break;
                case PROCESS:
                    process(parameters[0], parameters[1]);
                    responseInfo = "Your request was processed";
                    break;
                case GEN:
                    writeRandomCombinationToFile(parameters[0]);
                    responseInfo = "Your request was processed";
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return responseInfo;
    }

    private void process(String stringPathRandFile, String stringPathSortFile) {

        Path pathToRandomNumFile = Paths.get(stringPathRandFile);
        File fileRandomNum = new File(pathToRandomNumFile.toString());

        Path pathToSortNumFile = Paths.get(stringPathSortFile);
        File fileSortNum = new File(pathToSortNumFile.toString());

        Compute.TreeSort treeSort;
        try {
            treeSort = new Compute.TreeSort(fileSortNum.getName(), fileRandomNum);
        } catch (NoSuchFileException e) {
            responseInfo = "No such file. Check input info and try again!";
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        byte[] bytes = new byte[0];
        try {
            bytes = proxy.executeTask(treeSort);
        } catch (RemoteException e) {
            responseInfo = "There was an server error processing!";
        }
        try {
            if (fileSortNum.exists()) {
                fileSortNum.delete();
                fileSortNum.createNewFile();
            }
            Files.write(pathToSortNumFile, bytes, StandardOpenOption.APPEND);
        } catch (NoSuchFileException e) {
            responseInfo = "No such file. Check input info and try again!";
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            long timeProcesMethod = proxy.timeProcessMethod(treeSort);
            responseInfo = "Done! Time proces algoritm TreeSort = " + timeProcesMethod + " nsec| " + timeProcesMethod / 1000 + " mksec| " + timeProcesMethod / 1000000 + " msec.";
        } catch (RemoteException e) {
            responseInfo = "There was an server error processing!";
        }
    }

    private void writeRandomCombinationToFile(String pathToFile) {

        int[] arrayRandNum = new int[1000000];
        boolean isRecreate = false;
        Random generator = new Random();
        StringBuilder finalGenRandSeq = new StringBuilder();
        for (int i = 0; i < arrayRandNum.length; i++) {
            if (i != arrayRandNum.length - 1) {
                arrayRandNum[i] = generator.nextInt(1000000000);
                finalGenRandSeq.append(arrayRandNum[i] + " ");
            } else {
                arrayRandNum[i] = generator.nextInt(1000000000);
                finalGenRandSeq.append(arrayRandNum[i]);
            }
        }

        Path pathToDir = Paths.get(pathToFile);
        System.out.println(pathToDir.toString());
        File fileRandNum = new File(pathToDir.toString());

        if (fileRandNum.exists()) {
            fileRandNum.delete();
            isRecreate = true;
        }

        try {
            fileRandNum.createNewFile();
            if (!isRecreate) {
                responseInfo = "File - " + pathToDir.toString() + " created with random number!";
            } else {
                responseInfo = "File - " + pathToDir.toString() + " recreated with random number!";
            }
        } catch (IOException e) {
            responseInfo = "Input directory" + pathToDir.toString() + " not exist!";
            return;
        }
        try {
            Files.write(pathToDir, finalGenRandSeq.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            responseInfo = "Some trouble with writing to file!";
        }
    }

}
