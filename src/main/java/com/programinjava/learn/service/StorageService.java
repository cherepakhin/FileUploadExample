package com.programinjava.learn.service;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;


@Service
public class StorageService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(StorageService.class);

    private Path rootLocation = Paths.get("~/temp/input_data.txt");
    // static!!!
    private static List<String> historyLoadedFiles = new ArrayList<>();
    /**
     * Save the uploaded files
     *
     * @param file     - content
     * @param fileName - name (temp.csv)
     * @param catalog  - (/home/vasi/temp/1)
     */
    public boolean store(MultipartFile file, String catalog, String fileName) {
        log.info("Store method");
        log.info("Multipart name: {}", file.getName());
        log.info("Multipart OriginalFilename: {}", file.getOriginalFilename());
        log.info("fileName: {}", fileName);
        log.info("catalog: {}", catalog);
        historyLoadedFiles.add(fileName);
        try {
            rootLocation = Paths.get(catalog);
            //TODO: change to format string
            log.info("rootLocation:" + rootLocation);
            // rootLocation:/home/vasi/temp/1

            log.info("user.home: " + System.getProperty("user.home"));
            // user.home: /home/vasi

            log.info("rootLocation.resolve: " + this.rootLocation.resolve(fileName));
            // log:  rootLocation.resolve: /home/vasi/temp/1/temp.csv
            // see UploadController.CATALOG_FOR_SAVE
            // rootLocation = Paths.get("~/temp/input_data.txt")

            // StandardCopyOption.REPLACE_EXISTING - replace existing file
            if (Files.copy(file.getInputStream(), this.rootLocation.resolve(fileName), StandardCopyOption.REPLACE_EXISTING) > 0) {
                System.out.println(this.rootLocation.resolve(fileName)); // /home/vasi/temp/1/temp.csv
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("FAIL!" + e.getMessage());
        }
        return false;
    }

    public Resource loadFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("FAIL!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("FAIL!");
        }
    }

    public void deleteAll(String path) {
        if (path != null && !path.isEmpty()) {
            FileSystemUtils.deleteRecursively(Paths.get(path).toFile());
        } else {
            FileSystemUtils.deleteRecursively(rootLocation.toFile());
        }
    }

    public static List<String> getHistoryLoadedFiles() {
        return historyLoadedFiles;
    }

    public static void clearHistory() {
        historyLoadedFiles.clear();
    }
}
