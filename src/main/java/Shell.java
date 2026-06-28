import java.util.Arrays;
import java.util.Scanner;

public class Shell {
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            String[] words = input.split(" ");
            if (words.length == 0) {
                System.out.print("$ ");
            } else {
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
        switch (command) {
            case "echo", "type", "exit" -> {
                System.out.printf("%s is a shell builtin%n", command);
                break;
            }
            default -> {
                System.out.printf("%s: not found%n", command);
            }
        }
    }

    // --- fallback ---
    private void handleUnknown(String input) {
        System.out.printf("%s: command not found%n", input);
    }
}
