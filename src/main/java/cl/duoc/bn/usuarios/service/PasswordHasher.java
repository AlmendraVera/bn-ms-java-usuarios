package cl.duoc.bn.usuarios.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

/**
 * Hash de contraseñas con PBKDF2 (incluido en el JDK, sin dependencias extra).
 * Formato guardado: pbkdf2$iteraciones$salt$hash
 */
@Component
public class PasswordHasher {

    private static final int ITERACIONES = 120_000;
    private static final int BITS = 256;
    private final SecureRandom random = new SecureRandom();

    public String hash(String passwordPlano) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        byte[] hash = derivar(passwordPlano, salt, ITERACIONES);
        return "pbkdf2$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public boolean verificar(String passwordPlano, String guardado) {
        try {
            String[] partes = guardado.split("\\$");
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return MessageDigest.isEqual(esperado, derivar(passwordPlano, salt, iteraciones));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private byte[] derivar(String password, byte[] salt, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iteraciones, BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }
}