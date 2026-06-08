package com.project.kore.model;

import com.project.kore.builder.DocumentBuilder;
import com.project.kore.builder.impl.DocumentBuilderImpl;
import com.project.kore.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Un documento caricato sulla piattaforma: il file sta sul filesystem (cartella uploads/),
 * qui teniamo solo i metadati. Attenzione che owner (il cliente a cui appartiene) e uploadedBy
 * (chi l'ha caricato, spesso un professionista o un insurance manager) possono non coincidere.
 */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome originale del file al caricamento
    private String fileName;

    private String filePath;

    // MIME type, es. application/pdf
    private String contentType;

    @Enumerated(EnumType.STRING)
    private DocumentType type;

    // Il cliente a cui appartiene il documento
    @ManyToOne
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_document_owner_id"))
    private User owner;

    // Chi materialmente l'ha caricato, non per forza l'owner
    @ManyToOne
    @JoinColumn(name = "uploaded_by_id", foreignKey = @ForeignKey(name = "fk_document_uploaded_by_id"))
    private User uploadedBy;

    private LocalDateTime uploadDate;

    // TEXT per ospitare note lunghe
    @Column(columnDefinition = "TEXT")
    private String notes;

    public Document() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static DocumentBuilder builder() {
        return new DocumentBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document that = (Document) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Document{id=" + id + ", fileName='" + fileName + "', type=" + type + ", uploadDate=" + uploadDate + "}";
    }
}
