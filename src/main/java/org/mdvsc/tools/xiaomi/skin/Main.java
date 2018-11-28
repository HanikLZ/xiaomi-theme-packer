package org.mdvsc.tools.xiaomi.skin;

import javafx.application.Application;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {

    public static void main(String... args) throws IOException {

        var file = ApplicationStarter.Companion.getAppPropertiesFile();
        if (!file.exists()) {
            var parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.mkdirs()) throw new RuntimeException("cannot create folder " + parentFile.getAbsolutePath());
            if (!file.createNewFile()) throw new RuntimeException("cannot create file " + file.getAbsolutePath());
            try (InputStream is = new FileInputStream(file)) {
                ApplicationStarter.Companion.getAppProperties().load(is);
            }
        }
        Application.launch(ApplicationStarter.class, args);

    }
}
