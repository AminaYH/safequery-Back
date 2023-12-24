package com.safequery.back.projectdemo.contoller;

import ch.qos.logback.core.model.Model;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")

@RestController
@RequestMapping("/upload")
public class NodeFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFile.class);

    private static final Path PUBLIC_DIR = Paths.get(System.getProperty("user.dir"), "public");

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload (HttpServletRequest request) throws IOException, ServletException {

        for (var part : request.getParts()) {


            String fileName = FilenameUtils.separatorsToSystem(part.getSubmittedFileName());

            // Resolve the absolute path to the file based on the public folder
            Path file = PUBLIC_DIR.resolve(fileName);

            // Try to create the folder where the file is located
            if (Files.notExists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }

            // Write data to file
            try (var inputStream = part.getInputStream()){
                Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
            }

            LOGGER.info("write file: [{}] {}", part.getSize(), file);
        }
        return "ok";
    }

    @GetMapping("/files")
    public List<String> getAllFiles() {
        try {
            return Files.list(PUBLIC_DIR)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error listing files", e);
            return List.of("Error listing files");
        }
    }
    }


