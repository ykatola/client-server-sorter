package rmi;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Compute extends Remote {

    String RMI_SERVER_NAME = "lpi.server.rmi";

    void ping() throws RemoteException;

    String echo(String text) throws RemoteException;

    <T> T executeTask(Task<T> t) throws RemoteException;

    long timeProcessMethod(TreeSort treeSort) throws RemoteException;

    interface Task<T> {
        T execute();
    }

    class TreeSort implements Task<byte[]>, Serializable {
        private static final long serialVersionUID = 227L;

        private String nameRandomFile;
        private String nameSortFile;
        private byte[] fileRandom;
        private byte[] fileSort;
        private int[] intMasForSort;
        static long startAlgoritm, finishAlgoritm, timeConsumedMilis;

        public TreeSort(String nameSortFile, File file) throws IOException {
            this.nameSortFile = nameSortFile;
            this.nameRandomFile = file.getName();
            this.fileRandom = Files.readAllBytes(file.toPath());
            fillArrayFromFile(fileRandom);
        }

        private void fillArrayFromFile(byte[] file) {
            String infoFromFile = new String(file);
            String[] numInStingType = infoFromFile.split(" ");
            intMasForSort = new int[numInStingType.length];

            for (int i = 0; i < numInStingType.length; i++) {
                intMasForSort[i] = Integer.parseInt(numInStingType[i]);
            }
        }

        private void treeSort() {
            Tree tree = new Tree(intMasForSort[0]);
            for(int num : intMasForSort){
                tree.insert(tree.node, num);
            }
            tree.inOrder(tree.node);
            tree.inOrderDesc(tree.node);
            fillArray(tree.node, intMasForSort, 0);
        }

        private int fillArray(Node root, int [] array, int pos) {
            if (root.left != null) {
                pos = fillArray(root.left, array, pos);
            }
            array[pos++] = root.value;
            if (root.right != null) {
                pos = fillArray(root.right, array, pos);
            }
            return pos; // return the last position filled in by this invocation
        }

        @Override
        public byte[] execute() {
            startAlgoritm = System.nanoTime();
            treeSort();
            finishAlgoritm = System.nanoTime();
            timeConsumedMilis = finishAlgoritm - startAlgoritm;
            setTimeConsumedMilis(timeConsumedMilis);
            System.out.println(getTimeConsumedMilis());

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < intMasForSort.length; i++) {
                if (i-1 != intMasForSort.length ) {
                    stringBuilder.append(intMasForSort[i]).append(" ");
                } else {
                    stringBuilder.append(intMasForSort[i]);
                }
            }
            return stringBuilder.toString().getBytes();
        }

        public void setTimeConsumedMilis(long timeConsumedMilis) {
            TreeSort.timeConsumedMilis = timeConsumedMilis;
        }

        public String getNameRandomFile() {
            return nameRandomFile;
        }

        public void setNameRandomFile(String nameRandomFile) {
            this.nameRandomFile = nameRandomFile;
        }

        public String getNameSortFile() {
            return nameSortFile;
        }

        public void setNameSortFile(String nameSortFile) {
            this.nameSortFile = nameSortFile;
        }

        public byte[] getFileRandom() {
            return fileRandom;
        }

        public void setFileRandom(byte[] fileRandom) {
            this.fileRandom = fileRandom;
        }

        public byte[] getFileSort() {
            return fileSort;
        }

        public void setFileSort(byte[] fileSort) {
            this.fileSort = fileSort;
        }

        public long getStartAlgoritm() {
            return startAlgoritm;
        }

        public long getFinishAlgoritm() {
            return finishAlgoritm;
        }

        public long getTimeConsumedMilis() {
            return timeConsumedMilis;
        }
    }

    class Node {
        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
            left = null;
            right = null;
        }
    }

    class Tree {
        Node node;

        Tree(int value) {
            node = new Node(value);
        }

        public Node insert(Node node, int value) {
            if (node == null) {
                return new Node(value);
            }
            if (value < node.value) {
                node.left = insert(node.left, value);
            } else if (value > node.value) {
                node.right = insert(node.right, value);
            }
            return node;
        }

        public void inOrder(Node node) {
            if (node != null) {
                inOrder(node.left);
                System.out.print(node.value + " ");
                inOrder(node.right);
            }
        }

        public void inOrderDesc(Node node) {
            if (node != null) {
                inOrderDesc(node.right);
                System.out.print(node.value + " ");
                inOrderDesc(node.left);
            }
        }
    }
}
