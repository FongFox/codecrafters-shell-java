import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Shell {
    private static final Set<String> BUILTINS = Set.of("echo", "type", "pwd", "cd", "exit");
    private Path currentDirectoryPath = Path.of(System.getProperty("user.dir"));

    public void run() {
        Scanner scanner = new Scanner(System.in);
        //noinspection InfiniteLoopStatement
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                continue;
            }

            String[] words = parseArguments(input);
            String command = words[0];
            String[] arguments = Arrays.copyOfRange(words, 1, words.length);
            switch (command) {
                case "echo" -> handleEcho(arguments);
                case "type" -> handleType(arguments);
                case "pwd" -> handlePwd();
                case "cd" -> handleCd(arguments);
                case "exit" -> handleExit();
                default -> handleUnknown(command, arguments);
            }
        }
    }

    // --- built-in commands ---
    private void handleEcho(String[] arguments) {
        System.out.println(String.join(" ", arguments));
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

    private void handlePwd() {
        System.out.printf("%s%n", currentDirectoryPath);
    }

    private void handleCd(String[] arguments) {
        Path targetPath;
        String home = Objects.requireNonNullElse(System.getenv("HOME"), System.getProperty("user.home"));

        if (arguments.length == 0) {
            currentDirectoryPath = Path.of(home);
            return;
        }

        if (arguments[0].startsWith("~")) {
            String path = arguments[0].replace("~", home);
            targetPath = Path.of(path).normalize();
        } else {
            targetPath = currentDirectoryPath.resolve(arguments[0]).normalize();
        }

        if (!Files.isDirectory(targetPath)) {
            System.out.printf("cd: %s: No such file or directory%n", arguments[0]);
        } else {
            currentDirectoryPath = targetPath;
        }
    }

    private void handleExit() {
        System.exit(0);
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

//    private String[] parseArguments(String input) {
//        var tokens = new ArrayList<String>();
//        var current = new StringBuilder();
//        boolean inSingleQuotes = false;
//
//        for (char c : input.toCharArray()) {
//            if (!inSingleQuotes && c == '\'') {
//                inSingleQuotes = true;
//            } else if (inSingleQuotes && c == '\'') {
//                inSingleQuotes = false;
//            } else if (!inSingleQuotes && c == ' ') {
//                if (!current.isEmpty()) {
//                    tokens.add(current.toString());
//                    current.setLength(0);
//                }
//            } else {
//                current.append(c);
//            }
//        }
//
//        if (!current.isEmpty()) {
//            tokens.add(current.toString());
//        }
//
//        return tokens.toArray(new String[0]);
//    }

    private String[] parseArguments(String input) {
        var tokens = new ArrayList<String>();
        var currentToken = new StringBuilder();
        /*
         * 0 = không trong dấu nháy nào;
         * '\'' = đang trong '...';
         * '"'  = đang trong "..."
         */
        char openQuote = 0;

        for (char c : input.toCharArray()) {
            if (openQuote == 0 && c == '\'') {
                openQuote = c;
            } else if (openQuote == 0 && c == '"') {
                openQuote = c;
            } else if (openQuote == 0 && c == ' ') {
                if (!currentToken.isEmpty()) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else if (openQuote != 0 && openQuote == c) {
                openQuote = 0;
            } else {
                currentToken.append(c);
            }
        }

        if (!currentToken.isEmpty()) {
            tokens.add(currentToken.toString());
            currentToken.setLength(0);
        }

        return tokens.toArray(new String[0]);
    }

    // --- fallback ---
    private void handleUnknown(String command, String[] arguments) {
        String filePath = findInPath(command);
        if (filePath == null) {
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
