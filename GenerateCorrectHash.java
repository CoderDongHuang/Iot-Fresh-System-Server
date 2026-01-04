import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateCorrectHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Raw password: " + rawPassword);
        System.out.println("BCrypt hash: " + encodedPassword);
        System.out.println("Verification: " + encoder.matches(rawPassword, encodedPassword));
    }
}