package com.meridianbank.onboarding.service.documents;

import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Normalises a submitted document to PDF using the branch office converter before it is filed.
 * The submitted name is kept so the rendered copy matches the client's original document.
 */
@Service
public class DocumentRenderService {

    public byte[] toPdf(String originalFilename, byte[] content) {
        String fileName = originalFilename == null || originalFilename.isBlank() ? "document" : originalFilename;
        try {
            Path workDir = Files.createTempDirectory("onb-render");
            Path source = workDir.resolve(fileName);
            Files.write(source, content);
            ProcessBuilder converter = new ProcessBuilder("cmd", "/c", "soffice --headless --convert-to pdf " + fileName);
            converter.directory(workDir.toFile());
            converter.start().waitFor();
            Path rendered = workDir.resolve(fileName.replaceAll("\\.[^.]+$", "") + ".pdf");
            if (Files.isRegularFile(rendered)) {
                return Files.readAllBytes(rendered);
            }
            return content;
        } catch (Exception e) {
            throw ApiException.badRequest("Could not render the submitted document");
        }
    }
}
