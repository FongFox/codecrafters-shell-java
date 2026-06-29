import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Shell {
    private static final Set<String> BUILTINS = Set.of("echo", "type", "exit");

    public void run() {
        Scanner scanner = new Scanner(System.in);
        //noinspection InfiniteLoopStatement
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                continue;
            }

            String[] words = input.split(" ");
            String command = words[0];
            String[] arguments = Arrays.copyOfRange(words, 1, words.length);
            switch (command) {
                case "echo" -> handleEcho(arguments);
                case "type" -> handleType(arguments);
                case "exit" -> handleExit();
                default -> handleUnknown(command, arguments);
            }
        }
    }

    // --- built-in commands ---
    private void handleEcho(String[] arguments) {
        System.out.println(String.join(" ", arguments));
    }

    private void handleExit() {
        System.exit(0);
    }

    private void handleType(String[] arguments) {
        String command = arguments[0];
        if (BUILTINS.contains(command)) {
            System.out.printf("%s is a shell builtin%n", command);
        } else {
            String filePath = findInPath(command);
            if (filePath == null) {
                System.out.printf("%s: not found%n", command);
            } else {
                System.out.printf("%s is %s%n", command, filePath);
            }
        }
    }

    // --- helpers ---
    private String findInPath(String command) {
        String pathEnv = System.getenv("PATH");
        String[] directories = pathEnv.split(":");
        for (String directory : directories) {
            Path filePath = Path.of(directory, command);
            if (Files.isRegularFile(filePath) && Files.isExecutable(filePath)) {
                return filePath.toAbsolutePath().toString();
            }
        }

        return null;
    }

    // --- fallback ---
    private void handleUnknown(String command, String[] arguments) {
        if (findInPath(command) == null) {
            System.out.printf("%s: command not found%n", command);
        } else {
            List<String> cmd = new ArrayList<>();
            cmd.add(command);
            cmd.addAll(Arrays.asList(arguments));
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.inheritIO();
            try {
                Process process = processBuilder.start();
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    //print exception
                    System.err.printf("[InterruptedException] %s%n", e.getMessage());
                    e.printStackTrace(System.err);
                    Thread.currentThread().interrupt();
                }
            } catch (IOException e) {
                // print exception
                System.err.printf("[IOException] %s%n", e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }
}
