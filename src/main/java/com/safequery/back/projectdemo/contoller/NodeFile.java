package com.safequery.back.projectdemo.contoller;

import com.safequery.back.projectdemo.service.VulnerabilityDetectionService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class NodeFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFile.class);

    private static final Path PUBLIC_DIR = Paths.get(System.getProperty("user.dir"), "public");
    private static List<String> uploadedFiles = new ArrayList<>();
    private static String code ;

    private final VulnerabilityDetectionService vulnerabilityService;
    @Autowired

    public NodeFile(VulnerabilityDetectionService vulnerabilityService) {
        this.vulnerabilityService = vulnerabilityService;
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
@PostMapping("/upload")
public ResponseEntity<List<String>> uploadFile(@RequestParam("file") List<MultipartFile> files,@RequestParam(required = false) String folderPath ,  HttpSession session) {
    try {
        // Use the system's temporary directory as the default folder
        String uploadFolderPath = System.getProperty("java.io.tmpdir");

        for (MultipartFile file : files) {
            String relativePath = getRelativePath(file.getOriginalFilename(), uploadFolderPath);
            relativePath = relativePath.replaceFirst("tmp/", "");

            Path targetPath = Paths.get(uploadFolderPath, relativePath).normalize();

            // Remove "tmp" from the path

            // Save the file to the target path
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            LOGGER.info("Target Path: {}", targetPath.toString());
            uploadedFiles.add(targetPath.toString());
        }
        uploadedFiles = uploadedFiles.stream().filter(Objects::nonNull).collect(Collectors.toList());
        session.setAttribute("uploadedFiles", uploadedFiles);

        return ResponseEntity.ok(uploadedFiles);
    } catch (Exception e) {
        LOGGER.error("Error uploading files: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body(Collections.emptyList());
    }

}
    private String getRelativePath(String originalFilename, String baseFolder) {
        // Replace backslashes with slashes for compatibility
        String normalizedOriginalFilename = originalFilename.replace("\\", "/");

        // Remove "tmp" from the path
        return normalizedOriginalFilename.replaceFirst("tmp/", "");
    }

    @GetMapping("/getFolder")
    public ResponseEntity<List<String>> getFolder(HttpSession session) {
        try {
            // Generate relative paths without "/tmp"
            List<String> relativePaths = uploadedFiles.stream()
                    .map(this::getRelativePathWithoutTmp)
                    .map(path -> path.startsWith("/") ? path.substring(1) : path)  // Remove leading slash
                    // Filter out empty paths
                    .collect(Collectors.toList());

            return ResponseEntity.ok(relativePaths);
        } catch (Exception e) {
            LOGGER.error("Error getting folder: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    private String getRelativePathWithoutTmp(String fullPath) {
        // Replace "/tmp" with an empty string
        return fullPath.replace("/tmp", "");
    }

    private void listFiles(String folderPath) {
        File folder = new File(folderPath);

        // List all files in the folder
        File[] files = folder.listFiles();

        if (files != null) {
            List<String> filePaths = Arrays.stream(files)
                    .filter(Objects::nonNull)
                    .map(file -> folderPath + "/" + file.getName()) // Include directory path in the format "directoryPath/fileName"
                    .toList();

            System.out.println("List of files: " + filePaths);
        }

    }
    @PostMapping("/sendFilename")
    public ResponseEntity<String> sendFilename(@RequestBody Map<String, String> request) {
        String fileName = request.get("fileName");
        // Implement your logic here
        // You can store the filename in a variable or database for later retrieval
        return ResponseEntity.ok("Filename received successfully");
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        try {
            Optional<Path> filePath = findFile(System.getProperty("java.io.tmpdir"), fileName);

            if (filePath.isPresent()) {
                Path finalPath = filePath.get();

                byte[] fileContent = Files.readAllBytes(finalPath);
                 code = new String(fileContent, StandardCharsets.UTF_8);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", fileName);

                return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
            } else {
                LOGGER.error("File not found: {}", fileName);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            LOGGER.error("Error reading or writing file: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Optional<Path> findFile(String baseFolderPath, String fileName) throws IOException {
        File baseFolder = new File(baseFolderPath);

        if (baseFolder.exists() && baseFolder.isDirectory()) {
            return findFileRecursive(baseFolder, fileName);
        } else {
            LOGGER.error("Base folder not found: {}", baseFolderPath);
            return Optional.empty();
        }
    }

    private Optional<Path> findFileRecursive(File folder, String fileName) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    Optional<Path> filePath = findFileRecursive(file, fileName);
                    if (filePath.isPresent()) {
                        return filePath;
                    }
                } else if (file.getName().equals(fileName)) {
                    return Optional.of(file.toPath());
                }
            }
        }

        return Optional.empty();
    }

    @GetMapping("/check-sql-injection")
    public String checkForSQLInjection() {
        VulnerabilityDetectionService.VulnerabilityResult result = vulnerabilityService.checkForSQLInjection(code);
        VulnerabilityDetectionService.VulnerabilityResult result2 = vulnerabilityService.CheckForVulnerabilityUnion(code);
        return  result.getMessage() + result2.getMessage();
    }


}

