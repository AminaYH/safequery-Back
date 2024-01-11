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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("")
public class NodeFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFile.class);

    private static final Path PUBLIC_DIR = Paths.get(System.getProperty("user.dir"), "public");



    @GetMapping("/files")
    public List<String> getAllFiles() {
        try (Stream<Path> paths = Files.list(PUBLIC_DIR)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error listing files", e);
            return List.of("Error listing files");
        }
    }

//    @PostMapping(value = "/uploadFolder", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
//    public String uploadFolder (HttpServletRequest request) throws IOException, ServletException {
////
////        for (var part : request.getParts()) {
////
////
////            String folderName = FilenameUtils.separatorsToSystem(part.getSubmittedFileName());
////
////            // Resolve the absolute path to the file based on the public folder
////            Path folder = PUBLIC_DIR.resolve(folderName);
////
////            // Try to create the folder where the file is located
////            if (Files.notExists(folder.getParent())) {
////                Files.createDirectories(folder.getParent());
////            }
////
////            // Write data to file
////            try (var inputStream = part.getInputStream()){
////                Files.copy(inputStream, folder, StandardCopyOption.REPLACE_EXISTING);
////            }
////
////            LOGGER.info("write file: [{}] {}", part.getSize(), folder);
////        }
////        return "folder";
//    }
private static String uploadedFolderPath;
@PostMapping("/upload")
public ResponseEntity<List<String>> uploadFile(@RequestParam("file") List<MultipartFile> files,@RequestParam(required = false) String folderPath) {
    try {
        List<String> uploadedFiles = new ArrayList<>();

        String targetFolderPath = (folderPath != null) ? folderPath : System.getProperty("user.dir");
       //extract file by file
        for (MultipartFile file : files) {
            Path targetPath = Paths.get(targetFolderPath, file.getOriginalFilename());
            LOGGER.info("Source File: {}", file.getOriginalFilename());

            LOGGER.info("Target Path: {}", targetPath);
            uploadedFiles.add(folderPath);
            uploadedFiles.add(file.getOriginalFilename());

            java.nio.file.Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        listFiles(targetFolderPath);

        return ResponseEntity.ok(uploadedFiles);
    } catch (Exception e) {
        LOGGER.error("Error uploading files: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body(Collections.emptyList());
    }
}
    @GetMapping("/getFolder")
    public ResponseEntity<List<String>> getFolder() {
        try {
            String folderPath = (uploadedFolderPath != null) ? uploadedFolderPath : System.getProperty("user.dir");

            File folder = new File(folderPath);

            // List all files in the folder
            File[] files = folder.listFiles();

            if (files != null && files.length > 0) {
                List<String> fileNames = Arrays.stream(files)
                        .filter(Objects::nonNull)
                        .map(File::getName)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(fileNames);
            } else {
                return ResponseEntity.ok(Collections.emptyList());
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
    private void listFiles(String folderPath) {
        File folder = new File(folderPath);

        // List all files in the folder
        File[] files = folder.listFiles();

        if (files != null) {
            List<String> fileNames = Arrays.stream(files)
                    .filter(Objects::nonNull)
                    .map(File::getName)
                    .toList();

            System.out.println("List of files: " + fileNames);

        }
    }
}


