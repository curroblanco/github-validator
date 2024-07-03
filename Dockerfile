# Usa una imagen base con Gradle y JDK, e instala Maven y Git
FROM gradle:7.2.0-jdk17

# Instala Maven y Git
RUN apt-get update && \
    apt-get install -y maven git && \
    apt-get clean

# Establece el directorio de trabajo
WORKDIR /app

# Copia el script Java en el contenedor
COPY src/GitHubRepoTester.java /app/

# Compila el script Java
RUN javac GitHubRepoTester.java

# Define el punto de entrada, pero permite argumentos adicionales
ENTRYPOINT ["java", "GitHubRepoTester"]