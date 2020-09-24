package com.example.uploadingfiles.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.example.uploadingfiles.models.FileLines;
import com.example.uploadingfiles.models.Line;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

@Controller
public class FileUploadController {

    private final StorageService storageService;
    private final Map<String, FileLines> files = new HashMap<String, FileLines>();

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));

        model.addAttribute("fileNames", files.keySet());

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        storageService.store(file);
        String filename = file.getOriginalFilename();
        try {
            files.put(filename, createFileLines(filename));
        } catch (FileNotFoundException ex) {
            files.put(filename, null);
        }
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @GetMapping("/{filename}")
    public String mainFilePage(@PathVariable String filename, Model model) {
        model.addAttribute("fileName", filename);
        model.addAttribute("tableOfContent", files.get(filename).getFirstSection());
        model.addAttribute("countLines", files.get(filename).getFileLines().size());
        return "mainFilePage";
    }

    @GetMapping("/{filename}/line/{lineId}")
    @ResponseBody
    public String linePage(@PathVariable String filename, @PathVariable int lineId) {

        return files.get(filename).getLine(lineId).getLine();
    }

    @GetMapping("/{filename}/section/{lineId}")
    public String sectionPage(@PathVariable String filename, @PathVariable int lineId, Model model) {
        model.addAttribute("fileName", filename);
        model.addAttribute("lineIdText", files.get(filename).getLine(lineId).getLine());
        model.addAttribute("sectionLines", files.get(filename).getSection(lineId));

        return "sectionPage";
    }

    @GetMapping("/error")
    public String errorPage() {

        return "error";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }


    public FileLines createFileLines(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(new File(String.valueOf(storageService.load(filename)))));
        scanner.useDelimiter("\r\n");


        FileLines fileLines = new FileLines();

        int id = 0;
        int currentSectionId = 0;

        while (scanner.hasNext()) {

            String line = scanner.nextLine();
            Line currentLine = new Line(++id, line, currentSectionId);

            if (line.startsWith("#")) {
                currentLine.setSection(true);
                currentSectionId = defineCurrentSection(currentSectionId, fileLines.getFileLines(), currentLine);
                if (currentLine.getParentSectionId() != 0) {
                    fileLines.getLine(currentLine.getParentSectionId()).addChildrenSection(currentLine);
                }

            }

            fileLines.addLine(currentLine);

        }

        return fileLines;
    }

    private int defineCurrentSection(int lastSectionId, List<Line> fileLines, Line currentLine) {
        if (lastSectionId == 0) return currentLine.getId();

        int quantityHashtags = countHashtags(currentLine.getLine());
        Line lastSectionLine = fileLines.stream().filter(line -> line.getId() == lastSectionId).findAny().orElse(null);

        int quantityHashtagsCurrentSection = countHashtags(lastSectionLine.getLine());

        if (quantityHashtagsCurrentSection < quantityHashtags) {
            return currentLine.getId();
        } else if (quantityHashtagsCurrentSection == quantityHashtags) {
            currentLine.setParentSectionId(lastSectionLine.getParentSectionId());
            return currentLine.getId();
        } else {
            return defineCurrentSection(lastSectionLine.getParentSectionId(), fileLines, currentLine);
        }


    }


    private int countHashtags(String line) {
        String lineCopy = line;
        int counter = 0;
        while (lineCopy.startsWith("#")) {
            counter++;
            lineCopy = lineCopy.substring(1);
        }
        return counter;
    }

}
