package com.example.uploadingfiles.models;

import java.util.ArrayList;
import java.util.List;

public class Line {
    private int id;
    private String line;
    private boolean isSection;
    private int parentSectionId;
    private List<Line> childrenSections;

    public Line(int id, String line, boolean isSection, int parentSectionId) {
        this.id = id;
        this.line = line;
        this.isSection = isSection;
        this.parentSectionId = parentSectionId;
        if (isSection && childrenSections == null) childrenSections = new ArrayList<>();
    }

    public Line(int id, String line) {
        this.id = id;
        this.line = line;
        this.isSection = false;
    }

    public Line(int id, String line, int parentSectionId) {
        this.id = id;
        this.line = line;
        this.parentSectionId = parentSectionId;
        this.isSection = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public boolean isSection() {
        return isSection;
    }

    public void setSection(boolean section) {
        isSection = section;
        if (isSection && childrenSections == null) childrenSections = new ArrayList<>();
    }

    public int getParentSectionId() {
        return parentSectionId;
    }

    public void setParentSectionId(int parentSectionId) {
        this.parentSectionId = parentSectionId;
    }

    public void addChildrenSection(Line children) {
        this.childrenSections.add(children);
    }

    public List<Line> getChildrenSections() {
        return childrenSections;
    }
}
