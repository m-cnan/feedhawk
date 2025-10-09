import auth.PasswordHasher;

public class HashPassword {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java HashPassword <password>");
            return;
        }
        
        String password = args[0];
        String hashedPassword = PasswordHasher.hashPassword(password);
        System.out.println(hashedPassword);
    }
}