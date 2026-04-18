# sc29
Trabalho de SC grupo 29 primeiro projeto

Feito por:
Jaime Sousa 58171 - Dominio
Miguel Zhang 61829 - Cliente e Servidor
Teresa Grangeia 61869 - Historico, Logs e Testagem

## Keystore commands to generate keys and certificates

Keytool -genkeypair -alias spertaServer -keyalg RSA -keysize 2048 -keystore keystore.server

Keytool -list -keystore keystore.server

Keytool -exportcert -alias spertaServer -file certServer.cer -keystore keystore.server

Keytool -importcert -alias spertaServer -file certServer.cer -keystore truststore.client

System.setProperty

Atribuites: Generating 2048-bit RSA key pair and self-signed certificate (SHA384withRSA) with a validity of 90 days                                
for: CN=Jaime Sousa, OU=DI, O=FCUL, L=Lisboa, ST=Lisboa, C=PT 

pwd: Scream
     

## Requisitos

- Java JDK 17+ instalado (com `javac` e `java` no PATH)
- PowerShell (Windows)
- Bash (Linux)

## Compilacao (Windows)

Na raiz do projeto, corre:

```powershell
mkdir -Force bin
javac -d bin src\domain\*.java src\history\*.java src\server\*.java
```

Isto compila todo o codigo para a pasta `bin`.

## Compilacao (Linux)

Na raiz do projeto, corre:

```bash
mkdir -p bin
javac -d bin src/domain/*.java src/history/*.java src/server/*.java
```

Isto compila todo o codigo para a pasta `bin`.

## Execucao (Windows e Linux)

FAZER TODOS OS COMANDOS NO DIRETORIO BASE (sc29/)

### 1) Teste de dominio

```powershell
java -cp bin domain.Main
```

### 2) Servidor

```powershell
java -jar dist/sperta-server.jar 22345
```

O servidor fica a escuta na porta `22345`.

### 3) Cliente

Num segundo terminal:

```powershell
java -jar dist/sperta-client.jar 127.0.0.1:22345 <utilizador> <password>
```

Opcionalmente, podes indicar host e porta separados:

```powershell
java -jar dist/sperta-client.jar 127.0.0.1 22345 <utilizador> <password>
```

## Atestacao

O servidor valida o cliente antes de autenticar. O ficheiro `SpertaClient.txt`
deve estar presente no diretorio de execucao do servidor com o formato:

```
SpertaClient:<tamanho_em_bytes_do_jar>
```

O ficheiro ja esta configurado para o JAR incluido. Se reconstruires os JARs,
atualiza o valor com:

```bash
wc -c < dist/sperta-client.jar   # Linux/macOS
(Get-Item dist/sperta-client.jar).Length   # Windows
```

Nota: a atestacao so funciona ao correr o cliente a partir do JAR.

## Fluxo basico de uso

1. Inicia o servidor.
2. Inicia um ou mais clientes com utilizador e password.
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

### Teste PS

No diretorio base sc29 abrir powershell e executar:
$code = Get-Content -Raw .\test_smoke.ps1; Invoke-Expression $code

## JARs (cliente e servidor)

Para gerar os dois ficheiros JAR:

### Windows (PowerShell)

Se a politica permitir scripts:

```powershell
.\build_jars.ps1
```

Se estiver com politica `AllSigned`:

```powershell
$code = Get-Content -Raw .\build_jars.ps1; Invoke-Expression $code
```

Isto cria:

- `dist/sperta-client.jar`
- `dist/sperta-server.jar`

### Executar os JARs

Servidor:

```powershell
java -jar dist/sperta-server.jar 22345
```

Cliente (noutro terminal):

```powershell
java -jar dist/sperta-client.jar 127.0.0.1:22345 <utilizador> <password>
```

## Limitacoes

- A atestacao apenas funciona com o cliente executado a partir do JAR.
- Nao existe cifra nem assinatura digital nas comunicacoes (previsto para a Fase 2).
- O ficheiro `SpertaClient.txt` deve ser atualizado manualmente ao reconstruir o JAR do cliente.
