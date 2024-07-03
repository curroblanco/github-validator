import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitHubRepoTester {

  public static void main(String[] args) {

    String repoUrl = "https://github.com/SantiiL/zara-challenge.git";
    String repoName = getRepoNameFromUrl(repoUrl);

    try {
      // Clonar el repositorio
      executeCommand("rm -rf " + repoUrl);
      System.out.println("Clonando el repositorio: " + repoUrl);
      executeCommand("git clone " + repoUrl);

      // Detectar si el proyecto usa Maven o Gradle
      String buildCommand = detectBuildTool(repoName);

      // Ejecutar el comando de construcción
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
      return "mvn clean compile";
    } else if (new File(repoDir, "build.gradle").exists()) {
      return "gradle clean compileJava";
    } else {
      throw new RuntimeException("No se encontró ningún archivo de configuración de Maven o Gradle en el repositorio.");
    }
  }

  private static void executeCommand(String command) throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder();
    // Usar bash para ejecutar los comandos
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
