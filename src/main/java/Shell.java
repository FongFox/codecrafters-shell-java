import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;

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
                default -> handleUnknown(input);
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

    // Todo next stage
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
    private void handleUnknown(String input) {
        System.out.printf("%s: command not found%n", input);
    }
}
