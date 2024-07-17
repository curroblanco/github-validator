import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitHubRepoTester {

  public static void main(String[] args) {
    String repoUrl = args.length > 0 ? args[0] : System.getenv("REPO_URL");

    if (repoUrl == null || repoUrl.isEmpty()) {
      System.out.println("Error: No se proporcionó una URL del repositorio ni se estableció la variable de entorno REPO_URL.");
      return;
    }

    String repoName = getRepoNameFromUrl(repoUrl);

    try {
      System.out.println("Clonando el repositorio: " + repoUrl);
      executeCommand("git clone " + repoUrl);

      String buildCommand = detectBuildTool(repoName);

      System.out.println("Ejecutando " + buildCommand + " en el repositorio: " + repoName);
      executeCommand("cd " + repoName + " && " + buildCommand);

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static String getRepoNameFromUrl(String url) {
    String[] parts = url.split("/");
    return parts[parts.length - 1].replace(".git", "");
  }

  private static String detectBuildTool(String repoName) {
    File repoDir = new File(repoName);
    if (new File(repoDir, "pom.xml").exists()) {
      System.out.println("Detectado proyecto Maven.");
      return "mvn clean compile";
    } else if (new File(repoDir, "build.gradle").exists()) {
      System.out.println("Detectado proyecto Gradle.");
      return "gradle clean compileJava";
    } else {
      throw new RuntimeException("No se encontró ningún archivo de configuración de Maven o Gradle en el repositorio.");
    }
  }

  private static void executeCommand(String command) throws IOException, InterruptedException {
    System.out.println("Ejecutando comando: " + command);
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("bash", "-c", command);

    Process process = processBuilder.start();
    printCommandOutput(process);
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new RuntimeException("El comando falló con el código de salida " + exitCode);
    }
  }

  private static void printCommandOutput(Process process) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    }

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.err.println(line);
      }
    }
  }
}