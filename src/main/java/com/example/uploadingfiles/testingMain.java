package com.example.uploadingfiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class testingMain {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(new File("testFile")));
        scanner.useDelimiter("\r\n");
        int i = 0;
        while (scanner.hasNext()) {
            scanner.nextLine();
            i++;
        }
        System.out.println(i);
    }
}
