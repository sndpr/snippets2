package org.psc.io;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class FileUtils {

    @SneakyThrows
    public static Stream<URL> collectClassUrls(File file, String packageName) {
        try (var zipFile = new ZipFile(file)) {
            var entries = zipFile.entries();
            var fileUri = file.toURI().toString();
            List<URL> classUris = new ArrayList<>();
            while (entries.hasMoreElements()) {
                var next = entries.nextElement();
                var name = next.getName();
                if (name.startsWith(packageName.replace('.', '/')) && name.endsWith(".class")) {
                    classUris.add(URI.create("jar:" + fileUri + "!/" + name).toURL());
                }
            }
            return classUris.stream();
        }
    }

}
