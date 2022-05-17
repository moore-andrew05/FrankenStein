import java.util.Scanner;

public class UserIn {
    private Scanner UserIn = new Scanner(System.in);

    public String getInput(String message) {
        System.out.print(message + ": ");
        String input = UserIn.nextLine();
        return input;
    }
}
