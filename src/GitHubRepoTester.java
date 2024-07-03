import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitHubRepoTester {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Uso: java GitHubRepoTester <URL del repositorio de GitHub>");
      return;
    }

    String repoUrl = args[0];
    String repoName = getRepoNameFromUrl(repoUrl);

    try {
      // Clonar el repositorio
      System.out.println("Clonando el repositorio: " + repoUrl);
      executeCommand("git clone " + repoUrl);

      // Detectar si el proyecto usa Maven o Gradle
      String buildCommand = detectBuildTool(repoName);
      String testCommand = determineTestCommand(buildCommand);

      // Ejecutar el comando de construcción
      System.out.println("Ejecutando " + buildCommand + " en el repositorio: " + repoName);
      executeCommand("cd " + repoName + " && " + buildCommand);

      // Ejecutar solo los tests unitarios
      System.out.println("Ejecutando tests unitarios...");
      executeCommand("cd " + repoName + " && " + testCommand);

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
      return "mvn";
    } else if (new File(repoDir, "build.gradle").exists()) {
      System.out.println("Detectado proyecto Gradle.");
      return "gradle";
    } else {
      throw new RuntimeException("No se encontró ningún archivo de configuración de Maven o Gradle en el repositorio.");
    }
  }

  private static String determineTestCommand(String buildCommand) {
    if (buildCommand.equals("mvn")) {
      // Ejecutar solo los tests unitarios en Maven
      return "mvn test -DskipITs";
    } else if (buildCommand.equals("gradle")) {
      // Ejecutar solo los tests unitarios en Gradle
      return "gradle test";
    } else {
      throw new RuntimeException("No se pudo determinar el comando de prueba para " + buildCommand);
    }
  }

  private static void executeCommand(String command) throws IOException, InterruptedException {
    System.out.println("Ejecutando comando: " + command);
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
