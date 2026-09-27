package upc.pe.nayrabackend.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/** Clave pública del dispositivo en la columna {@code bytea}: codificación X.509 SubjectPublicKeyInfo (DER), D-048. */
@Converter
public class ClavePublicaConverter implements AttributeConverter<PublicKey, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(PublicKey clave) {
        return clave == null ? null : clave.getEncoded();
    }

    @Override
    public PublicKey convertToEntityAttribute(byte[] der) {
        if (der == null) {
            return null;
        }
        try {
            return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(der));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Clave pública del dispositivo ilegible.", e);
        }
    }
}
