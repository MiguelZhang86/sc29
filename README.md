# sc29
Trabalho de SC grupo 29 primeiro projeto

Feito por:
Jaime Sousa 58171 - Dominio
Miguel Zhang - Cliente e Servidor
Teresa Grangeia - Historico, Logs e Testagem

## Requisitos

- Java JDK 17+ instalado (com `javac` e `java` no PATH)
- PowerShell (Windows)
- Bash (Linux)

## Compilacao (Windows)

Na raiz do projeto, corre:

```powershell
javac -d bin src\domain\*.java src\history\*.java src\server\*.java
```

Isto compila todo o codigo para a pasta `bin`.

## Compilacao (Linux)

Na raiz do projeto, corre:

```bash
javac -d bin src/domain/*.java src/history/*.java src/server/*.java
```

Isto compila todo o codigo para a pasta `bin`.

## Execucao (Windows e Linux)

### 1) Teste de dominio

```powershell
java -cp bin domain.Main
```

### 2) Servidor

```powershell
java -cp bin server.SpertaServer
```

O servidor fica a escuta na porta `23456`.

### 3) Cliente

Num segundo terminal:

```powershell
java -cp bin server.SpertaClient
```

Opcionalmente, podes indicar host e porta:

```powershell
java -cp bin server.SpertaClient 127.0.0.1 23456
```

## Fluxo basico de uso

1. Inicia o servidor.
2. Inicia um ou mais clientes.
3. Envia comandos pelo cliente.
4. Usa `Ctrl+C` no terminal do cliente para terminar a sessao.

## Compilacao limpa (opcional)

Para apagar classes antigas e recompilar:

### Windows (PowerShell)

```powershell
Get-ChildItem -Path bin -Recurse -File | Remove-Item -Force
javac -d bin src\domain\*.java src\history\*.java src\server\*.java
```

### Linux (Bash)

```bash
find bin -type f -name "*.class" -delete
javac -d bin src/domain/*.java src/history/*.java src/server/*.java
```
