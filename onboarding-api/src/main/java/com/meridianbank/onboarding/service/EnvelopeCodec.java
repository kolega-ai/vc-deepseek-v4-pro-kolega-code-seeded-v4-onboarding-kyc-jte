package com.meridianbank.onboarding.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/** Opens the legacy AES/CBC envelopes posted by the case-management vendor. */
@Component
public class EnvelopeCodec {

    private static final byte[] IV = new byte[16];

    private final byte[] keyMaterial;

    public EnvelopeCodec(@Value("${app.envelope.key:Meridian-Envelope-Key-2026-000000}") String keyMaterial) {
        this.keyMaterial = keyMaterial.getBytes(StandardCharsets.UTF_8);
    }

    /** Decrypts a Base64 AES/CBC/PKCS5 envelope body. */
    public byte[] open(String envelope) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(Arrays.copyOf(keyMaterial, 32), "AES"),
                new IvParameterSpec(IV));
        return cipher.doFinal(Base64.getDecoder().decode(envelope));
    }
}
