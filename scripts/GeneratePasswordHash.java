import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Simple utility to generate BCrypt password hash
 * Usage: Run this from your IDE or compile and run from command line
 */
public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plainPassword = "password123";  // Change this to your desired password
        String hashedPassword = encoder.encode(plainPassword);
        
        System.out.println("Plain password: " + plainPassword);
        System.out.println("BCrypt hash: " + hashedPassword);
        System.out.println("\nSQL UPDATE statement:");
        System.out.println("UPDATE guides SET password = '" + hashedPassword + "' WHERE password IS NULL;");
    }
}
