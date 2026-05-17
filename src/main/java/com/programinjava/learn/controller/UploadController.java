package com.programinjava.learn.controller;

import com.programinjava.learn.service.StorageService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class UploadController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UploadController.class);
    private static final String userHomeDir = System.getProperty("user.home");

    @Autowired
    StorageService storageService;
    //	CHANGE IT ACCORDING TO YOUR LOCATION
    //TODO: change to user home directory
    @Value("${myconfig.catalogForSave}")
    String catalogForSave = "";

    public UploadController() {
        logger.info("=====================");
        logger.info("Catalog For Save location= '{}'", catalogForSave);
        logger.info("=====================");
    }

    /* call from Homepage.html:
                <div class="col-md-6">
                    <form method="POST" action="/upload" <---- post /upload
                        onsubmit="return Validate(this);" enctype="multipart/form-data">
                        <div class="col-sm-6">
                            <input type="file" name="file"  /> <----- name="file" for post @RequestParam("file")
                        </div>
                        <div class="col-sm-6">
                            <input type="submit" class="btn btn-success btn-sm" value="Upload data" />
                        </div>
                    </form>
                </div>

         */
    @PostMapping("/upload")
    public String singleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, ModelMap modelMap) {
        logger.info("POST /upload");
        logger.info("Multipart name: {}", file.getName());
        logger.info("Multipart OriginalFilename={}", file.getOriginalFilename());

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }
//        String UploadedFolderLocation = CATALOG_FOR_SAVE + "/";

        logger.info("Домашний каталог пользователя: {}", userHomeDir); //  /home/vasi
        logger.info("Catalog for save: {}", catalogForSave);

        String fileName = file.getOriginalFilename();
//			this is done to work on IE as well
//        String pattern = Pattern.quote(System.getProperty("file.separator"));
//        String[] str = file.getOriginalFilename().split(pattern);
//        if (str.length > 0)
//            fileName = str[str.length - 1];
//        else
//            fileName = str[0];
        logger.info("FileName : {}", fileName);
        // save file to disk
        if (!storageService.store(file, userHomeDir + catalogForSave, fileName)) {
            redirectAttributes.addFlashAttribute("message", "Error occurred while uploading the file");
            redirectAttributes.addFlashAttribute("status", "false");
            return "redirect:/uploadStatus";
        }
        String catalog = userHomeDir + catalogForSave;
        logger.info("last saved file: {}/{}", catalog, fileName);

        List<String> listLoadedFiles = storageService.getHistoryLoadedFiles();
        redirectAttributes.addFlashAttribute("listLoadedFiles", listLoadedFiles);

//        listLoadedFiles.add(fileName);

        redirectAttributes.addFlashAttribute("fileName", fileName);
        redirectAttributes.addFlashAttribute("message", "Загружен файл '" + fileName + "'");
        redirectAttributes.addFlashAttribute("status", "true");
        redirectAttributes.addFlashAttribute("getLastUploaded", storageService.getHistoryLoadedFiles());

        return "redirect:/uploadStatus";

    }

    @GetMapping("/")
    public String redirectToIndex(ModelMap m) {
        logger.info("/");
        logger.info("catalogForSave: {}", catalogForSave);
        return "Homepage";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus(ModelMap m) {
        logger.info("/uploadStatus");
        return "Homepage";
    }

    @GetMapping("/upload")
    public String displayHomePageForAlarm() {
        logger.info("GET /upload");
        return "Homepage";
    }

    /**
     * Удаляет все загруженные файлы из каталога для сохранения CATALOG_FOR_SAVED
     *
     * @return - HomePage
     */
    @GetMapping("/deleteAll")
    public String deleteAll() {
        logger.info("deleteAll");
        String userHomeDir = System.getProperty("user.home");
        if (storageService.getHistoryLoadedFiles().size() > 0) {
            for (String f : storageService.getHistoryLoadedFiles()) {
                logger.info("Delete file: {}", f);
                try {
                    Files.delete(Paths.get(userHomeDir + catalogForSave + "/" + f));
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
            getStorageService().clearHistory();
        }
        return "Homepage";
    }

    /**
     * Получение последнего загруженного файла
     *
     * @param modelMap
     * @return - string HomePage
     */
    @GetMapping("/getLastUploaded")
    public String getLastUploaded(ModelMap modelMap) {
        logger.info("GET /getLastUploaded");
        if (!storageService.getHistoryLoadedFiles().isEmpty()) {
            int historySize = storageService.getHistoryLoadedFiles().size();
            String catalog = userHomeDir + catalogForSave;
            Path path = Paths.get(catalog + '/' + getStorageService().getHistoryLoadedFiles().get(historySize - 1));
            try {
                String content = Files.readString(path, Charset.forName("UTF-8"));
                logger.info("Content of last loaded File:\n{}\r\n", content);
                modelMap.addAttribute("contentLastSavedFile", content);
                modelMap.addAttribute("listLoadedFiles", getStorageService().getHistoryLoadedFiles());
                modelMap.addAttribute("fileName", path.getFileName());

            } catch (IOException e) {
                logger.error("{}", e.getMessage());
            }
        }
        return "Homepage";
    }

    public StorageService getStorageService() {
        return storageService;
    }

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }
}
