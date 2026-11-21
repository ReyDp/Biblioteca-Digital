import java.security.*;
import java.util.Base64;
import java.nio.file.*;

public class GenerarClaves {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair pair = generator.generateKeyPair();

        String privatePem = """
                -----BEGIN PRIVATE KEY-----
                """ + Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(pair.getPrivate().getEncoded()) + """
                \n-----END PRIVATE KEY-----
                """;

        String publicPem = """
                -----BEGIN PUBLIC KEY-----
                """ + Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(pair.getPublic().getEncoded()) + """
                \n-----END PUBLIC KEY-----
                """;

        Path directory = Path.of("C:/Proyectos/secrets");
        Files.createDirectories(directory);

        Files.writeString(directory.resolve("prestamos-private.pem"), privatePem);
        Files.writeString(directory.resolve("prestamos-public.pem"), publicPem);

        System.out.println("Claves RSA generadas correctamente.");
        System.out.println("Directorio: " + directory);
    }
}
