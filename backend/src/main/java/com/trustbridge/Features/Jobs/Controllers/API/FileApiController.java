package com.trustbridge.Features.Jobs.Controllers.API;

import com.trustbridge.Domain.Entities.MilestoneSubmissionFile;
import com.trustbridge.Domain.Repositories.MilestoneSubmissionFileRepository;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileApiController {

    private final MilestoneSubmissionFileRepository milestoneSubmissionFileRepository;


    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> getFile(
            @PathVariable("fileId") UUID fileId,
            @RequestParam(value = "download", defaultValue = "false") boolean isDownload) {

        MilestoneSubmissionFile file =  milestoneSubmissionFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found!"));

        try {

            Path path = Paths.get(file.getStoredPath());

            log.info("ATTEMPTING TO READ FILE FROM: " + file.getStoredPath());

            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("File not found or not readable: {}", file.getStoredPath());
                throw new RuntimeException("File not found!");
            }

            String dispositionType = isDownload ? "attachment" : "inline";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=\"" + file.getOriginalFilename() + "\"")
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
