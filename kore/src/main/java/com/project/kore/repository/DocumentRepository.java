package com.project.kore.repository;

import com.project.kore.enums.DocumentType;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Documenti dei clienti, con ricerche per proprietario, tipo, uploader e intervallo di tempo.
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwnerOrderByUploadDateDesc(User owner);

    List<Document> findByOwnerAndTypeOrderByUploadDateDesc(User owner, DocumentType type);

    // Conta i documenti caricati da un professionista dopo una certa data: per le statistiche settimanali della dashboard.
    @Query("SELECT COUNT(d) FROM Document d WHERE d.uploadedBy = :uploader AND d.uploadDate >= :since")
    int countByUploaderSince(@Param("uploader") User uploader, @Param("since") LocalDateTime since);

    // Ultimo documento di un certo tipo per un cliente, per capire se serve un aggiornamento (es. scheda scaduta da oltre 7 giorni).
    @Query("SELECT d FROM Document d WHERE d.owner = :owner AND d.type = :type ORDER BY d.uploadDate DESC LIMIT 1")
    Document findLatestByOwnerAndType(@Param("owner") User owner, @Param("type") DocumentType type);

    // Documenti recenti di un cliente, per il feed attività.
    @Query("SELECT d FROM Document d WHERE d.owner = :owner AND d.uploadDate >= :since ORDER BY d.uploadDate DESC")
    List<Document> findRecentByOwner(@Param("owner") User owner, @Param("since") LocalDateTime since);

    // Documenti recenti caricati da un professionista, per il feed attività.
    @Query("SELECT d FROM Document d WHERE d.uploadedBy = :uploader AND d.uploadDate >= :since ORDER BY d.uploadDate DESC")
    List<Document> findRecentByUploader(@Param("uploader") User uploader, @Param("since") LocalDateTime since);

}