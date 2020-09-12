package com.example.uploadingfiles.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

@Controller
public class SimpleController {
    @Value("${spring.application.name}")
    String appName;

    @GetMapping("/home")
    public String homePage(Model model) throws FileNotFoundException {
        model.addAttribute("appName", appName);

        Scanner scanner = new Scanner(new FileReader(new File("testFile")));
        scanner.useDelimiter("\r\n");
        ArrayList<String> fileStrings = new ArrayList<>();
        ArrayList<String> tableOfContent = new ArrayList<>();
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            fileStrings.add(line);

        }

        model.addAttribute("fileStrings", fileStrings);

        return "home";
    }
}