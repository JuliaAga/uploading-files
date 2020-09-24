package com.example.uploadingfiles.models;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FileLines {
    private List<Line> fileLines;

    public FileLines() {
        this.fileLines = new ArrayList<>();
    }

    public FileLines(List<Line> fileLines) {
        this.fileLines = fileLines;
    }

    public List<Line> getFileLines() {
        return fileLines;
    }

    public void setFileLines(List<Line> fileLines) {
        this.fileLines = fileLines;
    }

    public Line getLine(int id) {
        return fileLines.stream().filter(line -> line.getId() == id).findAny().orElse(null);
    }

    public List<Line>  getTableOfContent() {

        List<Line> tableOfContent = new ArrayList<>();
        fileLines.forEach(line -> {
            if (line.isSection()) tableOfContent.add(line);
        });
        return tableOfContent;
    }

    public List<Line> getSection(int sectionId) {
        List<Line> section = new ArrayList<>();
        fileLines.forEach(line -> {
            if (line.getParentSectionId() == sectionId) section.add(line);
        });
        return section;
    }
    public boolean hasChildSection(int id){
        return fileLines.stream().anyMatch(line -> line.getParentSectionId() == id && line.isSection());
    }

    public List<Line> getChildrenSection(int id) {
        List<Line> childrenSection = new ArrayList<>();
        fileLines.forEach(line -> {
            if (line.getParentSectionId() == id && line.isSection()) childrenSection.add(line);
        });
        return childrenSection;
    }

    public void addLine(Line line) {
        this.fileLines.add(line);
    }
    public Line getFirstSection () {
        return fileLines.stream().filter(line -> line.getParentSectionId() == 0).findAny().orElse(null);
    }

}
