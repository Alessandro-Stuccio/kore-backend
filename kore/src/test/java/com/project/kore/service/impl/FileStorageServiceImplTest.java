package com.project.kore.service.impl;

import com.project.kore.exception.document.DocumentStorageException;
import com.project.kore.exception.document.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageServiceImpl();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
    }

    @Test
    @DisplayName("store — file PDF valido: restituisce percorso assoluto")
    void store_validPdf_returnsAbsolutePath() {
        MockMultipartFile file = new MockMultipartFile(
                "cv", "curriculum.pdf", "application/pdf", "PDF content".getBytes());

        String path = fileStorageService.store(file);

        assertThat(path).isNotBlank();
        assertThat(path).endsWith(".pdf");
        assertThat(Files.exists(Path.of(path))).isTrue();
    }

    @Test
    @DisplayName("store — file con estensione .docx: percorso termina con .docx")
    void store_docxFile_returnsPathWithDocxExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "doc", "document.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "DOCX content".getBytes());

        String path = fileStorageService.store(file);

        assertThat(path).endsWith(".docx");
    }

    @Test
    @DisplayName("store — originalFilename null: lancia InvalidFileException")
    void store_nullFilename_throwsInvalidFileException() {
        MockMultipartFile file = new MockMultipartFile("file", (String) null, "application/pdf", "content".getBytes());

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    @DisplayName("store — filename senza estensione: lancia InvalidFileException")
    void store_filenameWithoutExtension_throwsInvalidFileException() {
        MockMultipartFile file = new MockMultipartFile("file", "nomefilesestaensione", "application/pdf", "content".getBytes());

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    @DisplayName("delete — file esistente: viene eliminato dal filesystem")
    void delete_existingFile_deletesFile() throws IOException {
        Path fileToDelete = tempDir.resolve("test-delete.txt");
        Files.write(fileToDelete, "contenuto".getBytes());
        assertThat(Files.exists(fileToDelete)).isTrue();

        fileStorageService.delete(fileToDelete.toString());

        assertThat(Files.exists(fileToDelete)).isFalse();
    }

    @Test
    @DisplayName("delete — file inesistente: non lancia eccezione")
    void delete_nonExistentFile_doesNotThrow() {
        String nonExistent = tempDir.resolve("non-existent.pdf").toString();

        org.assertj.core.api.Assertions.assertThatNoException()
                .isThrownBy(() -> fileStorageService.delete(nonExistent));
    }

    @Test
    @DisplayName("load — file esistente: restituisce i byte corretti")
    void load_existingFile_returnsByteContent() throws IOException {
        byte[] content = "Test file content".getBytes();
        Path file = tempDir.resolve("test-load.txt");
        Files.write(file, content);

        byte[] loaded = fileStorageService.load(file.toString());

        assertThat(loaded).isEqualTo(content);
    }

    @Test
    @DisplayName("load — file inesistente: lancia DocumentStorageException")
    void load_nonExistentFile_throwsDocumentStorageException() {
        String nonExistent = tempDir.resolve("missing.pdf").toString();

        assertThatThrownBy(() -> fileStorageService.load(nonExistent))
                .isInstanceOf(DocumentStorageException.class);
    }

    @Test
    @DisplayName("store — directory di upload inesistente: viene creata automaticamente")
    void store_nonExistentUploadDir_createsDirectoryAndStoresFile() {
        Path newSubDir = tempDir.resolve("new/nested/dir");
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", newSubDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "cv", "cv.pdf", "application/pdf", "content".getBytes());

        String path = fileStorageService.store(file);

        assertThat(path).isNotBlank();
        assertThat(Files.exists(Path.of(path))).isTrue();
    }
}
