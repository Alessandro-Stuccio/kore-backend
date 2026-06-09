package com.project.kore.service.impl;

import com.project.kore.enums.DocumentType;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import com.project.kore.repository.DocumentRepository;
import com.project.kore.service.DocumentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** CRUD dei documenti caricati dagli utenti. */
@Service
public class DocumentServiceImpl implements DocumentService {

    // Estensione file → content-type atteso, per il controllo di coerenza ereditato dal vecchio build().
    private static final Map<String, String> EXT_CONTENT_TYPE = Map.of(
            "pdf",  "application/pdf",
            "jpg",  "image/jpeg",
            "jpeg", "image/jpeg",
            "png",  "image/png",
            "doc",  "application/msword",
            "gif",  "image/gif",
            "txt",  "text/plain"
    );

    private final DocumentRepository documentRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Document uploadDocument(String filePath, String originalName, String contentType,
                                   String docTypeStr, User client, User uploader) {
        validateContentTypeCoherence(originalName, contentType);
        Document doc = new Document();
        doc.setFileName(originalName);
        doc.setFilePath(filePath);
        doc.setContentType(contentType);
        doc.setType(DocumentType.valueOf(docTypeStr));
        doc.setOwner(client);
        doc.setUploadedBy(uploader);
        doc.setUploadDate(LocalDateTime.now());
        return documentRepository.save(doc);
    }

    // Invariante cross-field (fileName vs contentType) ereditata dal vecchio DocumentBuilder.build().
    private static void validateContentTypeCoherence(String fileName, String contentType) {
        if (fileName != null && contentType != null) {
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0) {
                String ext = fileName.substring(dot + 1).toLowerCase();
                String expected = EXT_CONTENT_TYPE.get(ext);
                if (expected != null && !contentType.equals(expected)) {
                    throw new IllegalArgumentException(
                            "contentType '" + contentType + "' non è coerente con l'estensione '." + ext + "'");
                }
            }
        }
    }

    @Override
    public List<Document> findRecentByOwner(User owner, LocalDateTime since) {
        return documentRepository.findRecentByOwner(owner, since);
    }

    @Override
    public List<Document> findRecentByProfessional(User professional, LocalDateTime since) {
        return documentRepository.findRecentByUploader(professional, since);
    }

    @Override
    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new CustomResourceNotFoundException("Documento", documentId));
    }

    @Override
    public List<Document> getUserDocuments(User owner) {
        return documentRepository.findByOwnerOrderByUploadDateDesc(owner);
    }

    @Override
    public List<Document> getUserDocumentsByType(User owner, String docType) {
        return documentRepository.findByOwnerAndTypeOrderByUploadDateDesc(owner, DocumentType.valueOf(docType));
    }

    @Override
    public void deleteDocument(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new CustomResourceNotFoundException("Documento", documentId));
        documentRepository.delete(doc);
    }

    @Override
    public Document updateNotes(Long documentId, String notes) {
        Document doc = getDocumentById(documentId);
        doc.setNotes(notes);
        return documentRepository.save(doc);
    }

    @Override
    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    @Override
    public Document findLatestByOwnerAndType(User owner, DocumentType type) {
        return documentRepository.findLatestByOwnerAndType(owner, type);
    }

    @Override
    public int countUploadedSince(User professional, LocalDateTime since) {
        return documentRepository.countByUploaderSince(professional, since);
    }
}
