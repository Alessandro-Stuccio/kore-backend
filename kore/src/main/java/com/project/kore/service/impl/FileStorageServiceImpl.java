package com.project.kore.service.impl;

import com.project.kore.exception.document.DocumentStorageException;
import com.project.kore.exception.document.InvalidFileException;
import com.project.kore.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/** Salva i file nella directory di upload locale, nominandoli con UUID per evitare collisioni. */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Crea al volo la directory di upload se manca; il file prende un nome UUID + estensione originale.
    @Override
    public String store(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new InvalidFileException("Nome file non valido o estensione mancante.");
        }

        File directory = new File(uploadDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new DocumentStorageException("Impossibile creare la directory di upload: " + uploadDir);
        }

        String extension = originalName.substring(originalName.lastIndexOf("."));
        Path destinationPath = Paths.get(uploadDir, UUID.randomUUID() + extension);

        try {
            Files.copy(file.getInputStream(), destinationPath);
        } catch (IOException e) {
            throw new DocumentStorageException("Errore durante il salvataggio del file su disco.", e);
        }

        return destinationPath.toString();
    }

    // Non fallisce se il file non c'è (deleteIfExists).
    @Override
    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new DocumentStorageException("Errore durante l'eliminazione del file dal disco.", e);
        }
    }

    @Override
    public byte[] load(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new DocumentStorageException("Errore durante la lettura del file dal disco.", e);
        }
    }
}
