package com.meridianbank.onboarding.web;

import com.meridianbank.onboarding.auth.UserContextService;
import com.meridianbank.onboarding.domain.entity.Document;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.service.AuditService;
import com.meridianbank.onboarding.service.documents.DocumentAccessService;
import com.meridianbank.onboarding.service.documents.DocumentRenderService;
import com.meridianbank.onboarding.service.documents.DocumentStorage;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Document download and metadata. Downloads re-authorize against the owning application on every
 * request — including re-downloads of a previously issued URL and expired URLs — through the
 * shared {@link DocumentAccessService} (the ownership check lives in that service, one call frame
 * above this controller).
 */
@RestController
public class DocumentController {

    private final UserContextService userContextService;
    private final DocumentAccessService documentAccessService;
    private final DocumentStorage documentStorage;
    private final DocumentRenderService documentRenderService;
    private final AuditService auditService;

    public DocumentController(UserContextService userContextService,
                              DocumentAccessService documentAccessService,
                              DocumentStorage documentStorage,
                              DocumentRenderService documentRenderService,
                              AuditService auditService) {
        this.userContextService = userContextService;
        this.documentAccessService = documentAccessService;
        this.documentStorage = documentStorage;
        this.documentRenderService = documentRenderService;
        this.auditService = auditService;
    }

    @GetMapping("/api/documents/{documentId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long documentId) throws IOException {
        UserAccount user = userContextService.currentUser();
        // Re-authorized on EVERY request (never cached): owner / branch officer / compliance.
        Document document = documentAccessService.requireAccessibleDocument(user, documentId);
        var path = documentStorage.resolve(document);
        long size = Files.size(path);

        auditService.recordRead(user, "DOCUMENT_DOWNLOAD", "Document", 1, "REAUTHORIZED_EACH_REQUEST");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getContentType()));
        headers.setContentLength(size);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"document\" ; filename*=UTF-8''"
                        + java.net.URLEncoder.encode(document.getOriginalFilename(), java.nio.charset.StandardCharsets.UTF_8)
                                .replace("+", "%20"));
        headers.setCacheControl("no-store");
        return ResponseEntity.ok().headers(headers)
                .body(new InputStreamResource(Files.newInputStream(path)));
    }

    /**
     * Document metadata endpoint. The ownership/scope check is performed in the shared
     * {@link DocumentAccessService} one call frame above this controller method.
     */
    @GetMapping("/api/applications/{applicationId}/documents/{documentId}/metadata")
    public Map<String, Object> metadata(@PathVariable Long applicationId, @PathVariable Long documentId) {
        UserAccount user = userContextService.currentUser();
        Document document = documentAccessService.requireAccessibleDocument(user, documentId);
        if (!document.getApplicationId().equals(applicationId)) {
            throw com.meridianbank.onboarding.service.error.ApiException.forbidden(
                    "Document does not belong to this application");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", document.getId());
        body.put("applicationId", document.getApplicationId());
        body.put("type", document.getDocType().name());
        body.put("category", document.getCategory().getLabel());
        body.put("filename", document.getOriginalFilename());
        body.put("state", document.getVerificationState().getLabel());
        body.put("contentType", document.getContentType());
        body.put("sizeBytes", document.getSizeBytes());
        body.put("uploadedAt", document.getCreatedAt());
        body.put("verifiedAt", document.getVerifiedAt());
        body.put("expiryDate", document.getExpiryDate());
        return body;
    }

    /** Normalises an uploaded scan to PDF through the branch office converter before filing. */
    @PostMapping(value = "/api/documents/render", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> render(@RequestParam("file") MultipartFile file) throws IOException {
        UserAccount user = userContextService.currentUser();
        byte[] rendered = documentRenderService.toPdf(file.getOriginalFilename(), file.getBytes());
        auditService.recordUser(user, "DOCUMENT_RENDERED", "Document",
                String.valueOf(file.getOriginalFilename()), null, null, "APPLICANT_OWNER");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(rendered);
    }
}
