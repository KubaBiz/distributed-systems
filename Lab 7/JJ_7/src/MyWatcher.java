import org.apache.zookeeper.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class MyWatcher implements Watcher {
    private static final int SESSION_TIMEOUT_MS = 3000;
    private static final int CLOSE_TIMEOUT_MS = 5000;
    private final String znode;
    private final String[] exec;
    private final ZooKeeper zk;
    private Process execProcess;

    public MyWatcher(String hostPort, String zNodeName, String[] exec) throws IOException {
        this.znode = zNodeName;
        this.exec = exec;
        zk = new ZooKeeper(hostPort, SESSION_TIMEOUT_MS, this);
    }

    public void start() {
        try {
            zk.addWatch(znode, AddWatchMode.PERSISTENT_RECURSIVE);
        } catch (KeeperException | InterruptedException e)
        {
            e.printStackTrace();
        }
        mainLoop();
    }

    private void mainLoop() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String input = br.readLine();
                if ("tree".equalsIgnoreCase(input))
                {
                    printTree(znode, 0);
                }
                else if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input))
                {
                    zk.close(CLOSE_TIMEOUT_MS);
                    killExecProcess();
                    break;
                }
            } catch (IOException ignored) {
                // ignored
            } catch (InterruptedException | KeeperException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        String watchedEventPath = watchedEvent.getPath();
        if (watchedEvent.getType() == Event.EventType.NodeCreated) {
            handleNodeCreatedEvent(watchedEventPath);
        }
        else if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
            handleNodeDeletedEvent(watchedEventPath);
        }
    }

    private void handleNodeCreatedEvent(String watchedEventPath) {
        System.out.println("Created zNode \"" + watchedEventPath + "\"");
        if (watchedEventPath.startsWith(znode)) {
            if (watchedEventPath.length() == znode.length()) {
                System.out.println("Running exec " + Arrays.toString(exec) + "");
                try {
                    execProcess = Runtime.getRuntime().exec(exec);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    System.out.println("zNode \"" + znode + "\" has " + zk.getAllChildrenNumber(znode) + " children");
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleNodeDeletedEvent(String watchedEventPath) {
        System.out.println("Deleted zNode \"" + watchedEventPath + "\"");
        if (znode.equals(watchedEventPath)) {
            killExecProcess();
        }
    }

    private void killExecProcess() {
        if (execProcess == null || !execProcess.isAlive()) {
            System.out.println("Exec process already dead");
        } else {
            execProcess.destroy();
            System.out.println("Exec process killed");
        }
    }

    private void printTree(String path, int indent) throws KeeperException, InterruptedException {
        // Print the current node path with indentation
        for (int i = 0; i < indent; i++) {
            System.out.print("    ");
        }

        // Get the children of the current node
        try {
            // Get the children of the current node
            List<String> children = zk.getChildren(path, false);
            System.out.println("<" + indent + "> " + getNodeName(path));
            for (String child : children) {
                String childPath = path.equals("/") ? "/" + child : path + "/" + child;
                printTree(childPath, indent + 1);
            }
        }
        catch (KeeperException e) {
            if (e.code().name().equals("NONODE")) {
                if (e.getPath().equals(znode)) {
                    System.out.println("zNode \"" + znode + "\" doesn't exist");
                } else {
                    System.out.println("Expected zNode \"" + e.getPath() + "\" but was missing");
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getNodeName(String path) {
        int lastIndex = path.lastIndexOf('/');
        if (lastIndex == -1) {
            return path;
        }
        return path.substring(lastIndex + 1);
    }
}
