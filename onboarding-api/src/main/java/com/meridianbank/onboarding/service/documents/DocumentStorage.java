package com.meridianbank.onboarding.service.documents;

import com.meridianbank.onboarding.config.AppProperties;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.enums.*;
import com.meridianbank.onboarding.domain.repo.DocumentRepository;
import com.meridianbank.onboarding.domain.repo.ReKycCaseRepository;
import com.meridianbank.onboarding.service.AuditService;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Local document store. Files are written outside the web root under a server-generated storage
 * name; the original filename is metadata only. Downloads resolve paths through a canonicalised
 * base directory and re-authorize against the owning application on every request.
 */
@Service
public class DocumentStorage {

    public static final long MAX_BYTES = 10L * 1024 * 1024;

    private final Path storageRoot;

    public DocumentStorage(AppProperties props) {
        this.storageRoot = Paths.get(props.getStorage().getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create storage directory " + storageRoot, e);
        }
    }

    @Transactional
    public Document store(Application application, UserAccount uploader, String docTypeName,
                          String originalFilename, String contentType, InputStream content, long sizeBytes) {
        if (sizeBytes <= 0 || sizeBytes > MAX_BYTES) {
            throw ApiException.badRequest("File must be between 1 byte and 10 MB");
        }
        DocType docType = parseDocType(docTypeName);
        String extension = extensionFor(contentType);
        String storageName = UUID.randomUUID().toString().replace("-", "") + extension;

        Path target = storageRoot.resolve(storageName).normalize();
        if (!target.startsWith(storageRoot)) {
            throw ApiException.badRequest("Invalid storage path");
        }
        try (InputStream in = content) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store uploaded file", e);
        }

        Document document = new Document();
        document.setApplicationId(application.getId());
        document.setDocType(docType);
        document.setCategory(docType.getCategory());
        document.setOriginalFilename(sanitizeFilename(originalFilename));
        document.setStorageName(storageName);
        document.setContentType(contentType);
        document.setSizeBytes(sizeBytes);
        document.setUploadedBy(uploader.getId());
        document.setVerificationState(DocState.UPLOADED);
        document.setCreatedAt(Instant.now());
        document.setExpiryDate(defaultExpiry(docType));
        return document;
    }

    /** Resolves a stored document through the canonicalised base directory. */
    public Path resolve(Document document) {
        Path base = storageRoot.toAbsolutePath().normalize();
        Path resolved = base.resolve(document.getStorageName()).normalize();
        if (!resolved.startsWith(base)) {
            throw ApiException.badRequest("Invalid storage path");
        }
        if (!Files.isRegularFile(resolved)) {
            throw ApiException.notFound("Stored file");
        }
        return resolved;
    }

    @Transactional
    public void deleteStoredFile(Document document) {
        Path path = resolve(document);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new IllegalStateException("Could not delete stored file", e);
        }
    }

    public static DocType parseDocType(String name) {
        try {
            return DocType.valueOf(name);
        } catch (Exception e) {
            throw ApiException.badRequest("Unknown document type");
        }
    }

    public static String extensionFor(String contentType) {
        return switch (contentType) {
            case "application/pdf" -> ".pdf";
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            default -> throw ApiException.badRequest("Unsupported file type; upload PDF, PNG or JPEG");
        };
    }

    private static Instant defaultExpiry(DocType docType) {
        return switch (docType) {
            case NATIONAL_ID, PASSPORT -> Instant.now().plus(5 * 365L, ChronoUnit.DAYS);
            case ADDRESS_PROOF, PROOF_OF_INCOME -> Instant.now().plus(365L, ChronoUnit.DAYS);
        };
    }

    public static String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) {
            return "document";
        }
        String cleaned = name.replace("\\", "/");
        cleaned = cleaned.substring(cleaned.lastIndexOf('/') + 1);
        cleaned = cleaned.replaceAll("[\\x00-\\x1f\\x7f]", "");
        return cleaned.length() > 255 ? cleaned.substring(0, 255) : cleaned;
    }

    public static boolean isAllowedContentType(String contentType) {
        return "application/pdf".equals(contentType)
                || "image/png".equals(contentType)
                || "image/jpeg".equals(contentType);
    }
}
