package com.meridianbank.onboarding.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public document intake board for the branch lobby kiosk: it shows the documents received on the
 * day so a walk-in customer can confirm their submission landed, and lets the desk open one to
 * check it. The kiosk has no sign-in of its own, so these routes are reachable without a session.
 */
@RestController
public class PortalDocumentsController {

    private final Path docRoot;

    public PortalDocumentsController(@Value("${app.storage.dir}") String dir) {
        this.docRoot = Paths.get(dir).toAbsolutePath().normalize();
    }

    /** Directory listing of everything received into the document store. */
    @GetMapping("/portal/documents")
    public Map<String, Object> index() throws IOException {
        List<Map<String, Object>> items = new ArrayList<>();
        if (Files.isDirectory(docRoot)) {
            try (var files = Files.list(docRoot)) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", file.getFileName().toString());
                    item.put("sizeBytes", Files.size(file));
                    item.put("receivedAt", Files.getLastModifiedTime(file).toInstant().toString());
                    items.add(item);
                }
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        return body;
    }

    /** Opens one received document by its stored name. */
    @GetMapping("/portal/documents/{name}")
    public ResponseEntity<byte[]> content(@PathVariable String name) throws IOException {
        Path file = docRoot.resolve(name).normalize();
        if (!file.startsWith(docRoot) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(Files.readAllBytes(file));
    }
}
