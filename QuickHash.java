import org.mindrot.jbcrypt.BCrypt; public class QuickHash { public static void main(String[] args) { System.out.println(BCrypt.hashpw("password123", BCrypt.gensalt())); } }
