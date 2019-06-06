package rmi;

public final class ProtocolManager {

    enum Request {
        PING("Ping", 0),
        ECHO("Echo", 1),
        PROCESS("Process", 2),
        GEN("Generate", 2);

        final String type;
        final int argumentsAmount;

        Request(String type, int argumentsAmount) {
            this.type = type;
            this.argumentsAmount = argumentsAmount;
        }
    }

}