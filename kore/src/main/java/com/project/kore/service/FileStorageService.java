package com.project.kore.service;

import com.project.kore.exception.document.DocumentStorageException;
import com.project.kore.exception.document.InvalidFileException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

/** Salvataggio fisico dei file sul filesystem. */
@Validated
public interface FileStorageService {

    /**
     * Scrive il file su disco e ritorna il percorso dove è stato archiviato.
     *
     * @param file il file caricato
     * @return il percorso del file salvato su disco
     * @throws InvalidFileException     se il nome del file non è valido o manca l'estensione
     * @throws DocumentStorageException se non è possibile creare la cartella o scrivere il file
     */
    String store(@NotNull MultipartFile file);

    /**
     * Elimina dal disco il file indicato.
     *
     * @param filePath percorso del file da eliminare
     * @throws DocumentStorageException se l'eliminazione dal disco fallisce
     */
    void delete(@NotBlank String filePath);

    /**
     * Legge dal disco il contenuto binario del file.
     *
     * @param filePath percorso del file da leggere
     * @return il contenuto binario del file
     * @throws DocumentStorageException se la lettura dal disco fallisce
     */
    byte[] load(@NotBlank String filePath);
}
