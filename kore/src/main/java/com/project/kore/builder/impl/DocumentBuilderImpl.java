package com.project.kore.builder.impl;

import com.project.kore.builder.DocumentBuilder;
import com.project.kore.enums.DocumentType;
import java.time.LocalDateTime;
import java.util.Map;
import com.project.kore.model.*;


/**
 * Builder concreto per Document. In build verifica che il contentType dichiarato sia coerente con
 * l'estensione del file, quando questa è tra quelle note.
 */
public class DocumentBuilderImpl implements DocumentBuilder {

    private static final Map<String, String> EXT_CONTENT_TYPE = Map.of(
        "pdf",  "application/pdf",
        "jpg",  "image/jpeg",
        "jpeg", "image/jpeg",
        "png",  "image/png",
        "doc",  "application/msword",
        "gif",  "image/gif",
        "txt",  "text/plain"
    );

    private Long id;
    private String fileName;
    private String filePath;
    private String contentType;
    private DocumentType type;
    private User owner;
    private User uploadedBy;
    private LocalDateTime uploadDate;
    private String notes;

    @Override
    public DocumentBuilder id(Long id) {
        this.id = id;
        return this;
    }
    @Override
    public DocumentBuilder fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
    @Override
    public DocumentBuilder filePath(String filePath) {
        this.filePath = filePath;
        return this;
    }
    @Override
    public DocumentBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
    @Override
    public DocumentBuilder type(DocumentType type) {
        this.type = type;
        return this;
    }
    @Override
    public DocumentBuilder owner(User owner) {
        this.owner = owner;
        return this;
    }
    @Override
    public DocumentBuilder uploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
        return this;
    }
    @Override
    public DocumentBuilder uploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
        return this;
    }
    @Override
    public DocumentBuilder notes(String notes) {
        this.notes = notes;
        return this;
    }

    @Override
    public Document build() {
        if (this.fileName != null && this.contentType != null) {
            int dot = this.fileName.lastIndexOf('.');
            if (dot >= 0) {
                String ext = this.fileName.substring(dot + 1).toLowerCase();
                String expected = EXT_CONTENT_TYPE.get(ext);
                if (expected != null && !this.contentType.equals(expected))
                    throw new IllegalArgumentException(
                        "contentType '" + this.contentType + "' non è coerente con l'estensione '." + ext + "'");
            }
        }

        Document obj = new Document();
        obj.setId(this.id);
        obj.setFileName(this.fileName);
        obj.setFilePath(this.filePath);
        obj.setContentType(this.contentType);
        obj.setType(this.type);
        obj.setOwner(this.owner);
        obj.setUploadedBy(this.uploadedBy);
        obj.setUploadDate(this.uploadDate);
        obj.setNotes(this.notes);
        return obj;
    }
}
