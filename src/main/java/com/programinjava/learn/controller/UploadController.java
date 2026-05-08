package com.programinjava.learn.controller;

import com.programinjava.learn.service.StorageService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.logging.Logger;
import java.util.regex.Pattern;

@Controller
public class UploadController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    StorageService storageService;

    //	CHANGE IT ACCORDING TO YOUR LOCATION
    //TODO: change to user home directory
    private final String CATALOG_FOR_SAVE = "/home/vasi/temp/1";

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
    public String singleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        logger.info("POST /upload");
        logger.info("Multipart name: {}", file.getName());
        logger.info("Multipart OriginalFilename={}", file.getOriginalFilename());

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }
//        String UploadedFolderLocation = CATALOG_FOR_SAVE + "/";
        String fileName = file.getOriginalFilename();
//			this is done to work on IE as well
//        String pattern = Pattern.quote(System.getProperty("file.separator"));
//        String[] str = file.getOriginalFilename().split(pattern);
//        if (str.length > 0)
//            fileName = str[str.length - 1];
//        else
//            fileName = str[0];
        logger.info("FileName : " + fileName);
        // save file to disk
        if (!storageService.store(file, CATALOG_FOR_SAVE, fileName)) {
            redirectAttributes.addFlashAttribute("message", "Error occurred while uploading the file");
            redirectAttributes.addFlashAttribute("status", "false");
            return "redirect:/uploadStatus";
        }
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded '" + fileName + "'");
        redirectAttributes.addFlashAttribute("status", "true");
        return "redirect:/uploadStatus";
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

    @GetMapping("/deleteAll")
    public String deleteAll() {
        logger.info("deleteAll");
        return "Homepage";
    }

}
