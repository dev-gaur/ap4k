package io.ap4k.utils;

import io.ap4k.Ap4kException;
import io.ap4k.deps.commons.compress.archivers.tar.TarArchiveEntry;
import io.ap4k.deps.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Packaging {

    private static final String DEFAULT_DOCKERFILE = "Dockerfile";
    private static final String DOCKER_IGNORE = ".dockerignore";

    protected static final String DEFAULT_TEMP_DIR = System.getProperty("java.io.tmpdir", "/tmp");
    protected static final String DOCKER_PREFIX = "docker-";
    protected static final String BZIP2_SUFFIX = ".tar.bzip2";

    private static final Charset UTF_8 = Charset.forName("UTF-8");

   public static File packageFile(String path) {
    try {
      final Path root = Paths.get(path).getParent();
      File tempFile = Files.createTempFile(Paths.get(DEFAULT_TEMP_DIR), DOCKER_PREFIX, BZIP2_SUFFIX).toFile();
      try ( final TarArchiveOutputStream tout = buildTarStream(tempFile)) {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
          }
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String absolutePath = file.toAbsolutePath().toString();
            if (!path.equals(absolutePath)) {
             return FileVisitResult.CONTINUE;
            }
            final Path relativePath = root.relativize(file);
            final TarArchiveEntry entry = new TarArchiveEntry(file.toFile());
            entry.setName(relativePath.toString());
            entry.setMode(TarArchiveEntry.DEFAULT_FILE_MODE);
            entry.setSize(attrs.size());
            putTarEntry(tout, entry, file);
            return FileVisitResult.CONTINUE;
          }
        });
        tout.flush();
      }
      return tempFile;

    } catch (IOException e) {
      throw Ap4kException.launderThrowable(e);
    }
  }

      public static void putTarEntry(TarArchiveOutputStream tarArchiveOutputStream, TarArchiveEntry tarArchiveEntry,
        Path inputPath) throws IOException {
        tarArchiveEntry.setSize(Files.size(inputPath));
        tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);
        Files.copy(inputPath, tarArchiveOutputStream);
        tarArchiveOutputStream.closeArchiveEntry();
    }

    public static TarArchiveOutputStream buildTarStream(File outputPath) throws IOException {
        FileOutputStream fout = new FileOutputStream(outputPath);
        BufferedOutputStream bout = new BufferedOutputStream(fout);
        //BZip2CompressorOutputStream bzout = new BZip2CompressorOutputStream(bout);
        return new TarArchiveOutputStream(bout);
    }

    public static void tar(Path inputPath, Path outputPath) throws IOException {
        if (!Files.exists(inputPath)) {
            throw new FileNotFoundException("File not found " + inputPath);
        }

        try (TarArchiveOutputStream tarArchiveOutputStream = buildTarStream(outputPath.toFile())) {
            if (!Files.isDirectory(inputPath)) {
                TarArchiveEntry tarEntry = new TarArchiveEntry(inputPath.toFile().getName());
                if (inputPath.toFile().canExecute()) {
                    tarEntry.setMode(tarEntry.getMode() | 0755);
                }
                putTarEntry(tarArchiveOutputStream, tarEntry, inputPath);
            } else {
                Files.walkFileTree(inputPath,
                    new TarDirWalker(inputPath, tarArchiveOutputStream));
            }
            tarArchiveOutputStream.flush();
        }
    }

  public static class TarDirWalker extends SimpleFileVisitor<Path> {
    private Path basePath;
    private TarArchiveOutputStream tarArchiveOutputStream;

    public TarDirWalker(Path basePath, TarArchiveOutputStream tarArchiveOutputStream) {
      this.basePath = basePath;
      this.tarArchiveOutputStream = tarArchiveOutputStream;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      if (!dir.equals(basePath)) {
        tarArchiveOutputStream.putArchiveEntry(new TarArchiveEntry(basePath.relativize(dir).toFile()));
        tarArchiveOutputStream.closeArchiveEntry();
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      TarArchiveEntry tarEntry = new TarArchiveEntry(basePath.relativize(file).toFile());
      tarEntry.setSize(attrs.size());
      if (file.toFile().canExecute()) {
        tarEntry.setMode(tarEntry.getMode() | 0755);
      }
      Packaging.putTarEntry(tarArchiveOutputStream, tarEntry, file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
      tarArchiveOutputStream.close();
      throw exc;
    }
  }

}
