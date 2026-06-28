import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();
            String[] words = input.split(" ");
            if (words.length == 0) {
                System.out.print("$ ");
            } else {
                String command = words[0];
                String[] arguments = Arrays.copyOfRange(words, 1, words.length);
                if (command.equals("echo")) {
                    System.out.println(String.join(" ", arguments));
                } else if (command.equals("exit")) {
                    sc.close();
                    return;
                } else {
                    System.out.printf("%s: command not found%n", input);
                }
            }
        }
    }
}
