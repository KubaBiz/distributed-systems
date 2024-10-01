import java.io.IOException;


public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Required arguments: <host>:<port> <znode> <exec_path> <optional_exec_args>");
            System.exit(1);
        }
        String hostPort = args[0];
        String znode = args[1];
        if (!znode.startsWith("/")) {
            znode = "/" + znode;
        }
        String[] exec = new String[args.length - 2];
        System.arraycopy(args, 2, exec, 0, exec.length);

        MyWatcher myWatcher = null;
        try {
            myWatcher = new MyWatcher(hostPort, znode, exec);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        myWatcher.start();
    }
}