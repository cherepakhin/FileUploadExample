package com.programinjava.learn.service;

import com.programinjava.learn.controller.UploadController;
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


@Service
public class StorageService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(StorageService.class);

    private Path rootLocation = Paths.get("~/temp/input_data.txt");

    /**
     * Save the uploaded files
     *
     * @param file         - content
     * @param fileName     - name (temp.csv)
     * @param catalog - (/home/vasi/temp/1)
     */
    public boolean store(MultipartFile file, String catalog, String fileName ) {
        log.info("Multipart name: {}", file.getName());
        log.info("Multipart OriginalFilename={}", file.getOriginalFilename());
        log.info("Store method. fileName {}, catalog {}", fileName, catalog);
        try {
            rootLocation = Paths.get(catalog);
            //TODO: change to format string
            log.info("rootLocation:" + rootLocation);
            // StandardCopyOption.REPLACE_EXISTING - replace existing file
            //System.getProperty("user.home")
            log.info("rootLocation.resolve: " + this.rootLocation.resolve(fileName));
            //log:  rootLocation.resolve: /home/vasi/temp/1/temp.csv , see UploadController.CATALOG_FOR_SAVE
            // rootLocation = Paths.get("~/temp/input_data.txt")

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


}
