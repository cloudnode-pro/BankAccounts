/*
 * BankAccounts is a Minecraft economy plugin that enables players to hold multiple bank accounts.
 * Copyright © 2023–2026 Cloudnode OÜ.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <https://www.gnu.org/licenses/>.
 */

package pro.cloudnode.smp.bankaccounts.internal.auth;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
import java.util.Arrays;
import java.util.Base64;

/**
 * Represents an Ed25519 signature.
 *
 * @param bytes the signature bytes
 */
public record EdECSignature(byte @NotNull [] bytes) {
    private static final int LENGTH = 64;

    /**
     * Creates a new Ed25519 signature from a byte array.
     *
     * @param bytes the signature bytes
     */
    public EdECSignature(final byte @NotNull [] bytes) {
        if (bytes.length != LENGTH) {
            throw new IllegalArgumentException(String.format("Invalid signature length: %d", bytes.length));
        }
        this.bytes = bytes.clone();
    }

    /**
     * Decodes a base64-encoded Ed25519 signature.
     *
     * @param base64 the base64-encoded signature
     * @return the decoded signature
     */
    @NotNull
    public static EdECSignature fromBase64(final @NotNull String base64) {
        return new EdECSignature(Base64.getDecoder().decode(base64));
    }

    /**
     * Decodes a base64-url-safe-encoded Ed25519 signature.
     *
     * @param base64Url the base64-url-safe-encoded signature
     * @return the decoded signature
     */
    @NotNull
    public static EdECSignature fromBase64Url(final @NotNull String base64Url) {
        return new EdECSignature(Base64.getUrlDecoder().decode(base64Url));
    }

    /**
     * Signs the given byte array using the given private key.
     *
     * @param key     the Ed25519 private key
     * @param message the message to sign
     * @return the signature
     */
    @NotNull
    public static EdECSignature sign(final @NotNull EdECPrivateKey key, final byte @NotNull [] message) {
        final Signature sig;
        try {
            sig = Signature.getInstance("Ed25519");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Ed25519 signature algorithm not supported", e);
        }

        try {
            sig.initSign(key);
        } catch (final InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Ed25519 private key", e);
        }

        try {
            sig.update(message);

            final byte[] out = sig.sign();
            if (out.length != LENGTH) {
                throw new IllegalStateException("Non-canonical Ed25519 signature");
            }
            return new EdECSignature(out);
        } catch (final SignatureException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Signs the given string using the given private key.
     *
     * @param key     the Ed25519 private key
     * @param message the message to sign
     * @return the signature
     */
    @NotNull
    public static EdECSignature sign(final @NotNull EdECPrivateKey key, final @NotNull String message) {
        return sign(key, message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Returns the signature bytes.
     *
     * @return the signature bytes
     */
    @Override
    public byte @NotNull [] bytes() {
        return bytes.clone();
    }

    /**
     * Returns this signature as a base64-encoded string.
     *
     * @return the signature in base64
     */
    @NotNull
    public String toBase64() {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Returns this signature as a base64-url-safe-encoded string.
     *
     * @return the signature in base64-url
     */
    @NotNull
    public String toBase64Url() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Verifies this signature for the given byte array using the signer’s public key.
     *
     * @param key     the Ed25519 public key of the signer
     * @param message the message to verify
     * @return true if the message is signed by the owner of the given key with this signature, false otherwise
     */
    public boolean verify(final @NotNull EdECPublicKey key, final byte @NotNull [] message) {
        final Signature sig;
        try {
            sig = Signature.getInstance("Ed25519");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Ed25519 signature algorithm not supported", e);
        }

        try {
            sig.initVerify(key);
        } catch (final InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Ed25519 public key", e);
        }

        try {
            sig.update(message);
            return sig.verify(bytes);
        } catch (final SignatureException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Verifies this signature for the given string using the signer’s public key.
     *
     * @param key     the Ed25519 public key of the signer
     * @param message the message to verify
     * @return true if the message is signed by the owner of the given key with this signature, false otherwise
     */
    public boolean verify(final @NotNull EdECPublicKey key, final @NotNull String message) {
        return verify(key, message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean equals(final @NotNull Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof final EdECSignature signature && MessageDigest.isEqual(bytes, signature.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    @NotNull
    public String toString() {
        return EdECSignature.class.getSimpleName() + '[' + LENGTH + " bytes]";
    }
}
