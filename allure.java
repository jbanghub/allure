///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVAC_OPTIONS -Xlint:-options
//DEPS dev.jbang:jbang-cli:0.119.0

// workaround to avoid weird method not found issue
//DEPS org.apache.maven:maven-model:3.6.1

//JAVA 11+

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.jbang.dependencies.DependencyUtil;
import dev.jbang.util.UnpackUtil;
import dev.jbang.util.Util;

public class allure {

    static final String ALLURE_DEFAULT_VERSION = "2.30.0";

    String version = ALLURE_DEFAULT_VERSION;
    Path installationDirectory = Path.of(".allure");

    private Path getAllureExecutablePath() {
        final String allureExecutable = isWindows() ? "allure.bat" : "allure";
        return getAllureHome().resolve("bin").resolve(allureExecutable);
    }

    boolean isWindows() {
        return Util.isWindows();
    }

    private Path getAllureHome() {
        return installationDirectory.resolve(String.format("allure-%s", version));
    }

    public boolean allureExists() {
        final Path allureExecutablePath = getAllureExecutablePath();
        return Files.exists(allureExecutablePath) && Files.isExecutable(allureExecutablePath);
    }

    void run(String... args) throws IOException, InterruptedException {
        if (!allureExists()) {
            System.out.println(String.format("Fetching allure %s", version));
           
            var deps = DependencyUtil.resolveDependencies(
                    List.of("io.qameta.allure:allure-commandline:"+version+"@zip"),
                    List.of(), false, false, false, false);

            UnpackUtil.unpack(
                    deps.getArtifacts().get(0).getFile(),
                    Path.of(".allure"));
        }

        Util.setVerbose(true);
        if(allureExists()) {
            var fullargs = new ArrayList<String>();
            String cmd = getAllureExecutablePath().toAbsolutePath().toString();
            fullargs.add(cmd);
            fullargs.addAll(Arrays.asList(args));
            ProcessBuilder pb = new ProcessBuilder(fullargs).inheritIO();
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            System.exit(exitCode);
        } else {
            System.err.println("Failed to find allure.");
        }
    }

    public static void main(String... args) throws IOException, InterruptedException {
        new allure().run(args);
    }
}
