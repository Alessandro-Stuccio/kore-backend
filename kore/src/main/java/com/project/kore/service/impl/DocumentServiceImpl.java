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

/** CRUD dei documenti caricati dagli utenti. */
@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Document uploadDocument(String filePath, String originalName, String contentType,
                                   String docTypeStr, User client, User uploader) {
        Document doc = Document.builder()
                .fileName(originalName)
                .filePath(filePath)
                .contentType(contentType)
                .type(DocumentType.valueOf(docTypeStr))
                .owner(client)
                .uploadedBy(uploader)
                .uploadDate(LocalDateTime.now())
                .build();
        return documentRepository.save(doc);
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
