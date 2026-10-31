$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$jdk = Get-ChildItem -LiteralPath "$projectRoot\.local" -Directory -Filter 'jdk-17*' | Select-Object -First 1
$maven = Get-ChildItem -LiteralPath "$projectRoot\.local" -Directory -Filter 'apache-maven-*' | Select-Object -First 1
if (!$jdk -or !$maven) {
    throw 'Khong tim thay JDK 17 hoac Maven trong thu muc .local.'
}
$env:JAVA_HOME = $jdk.FullName
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:MAVEN_OPTS = '-Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavax.net.ssl.trustStore=NONE'
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://127.0.0.1:3306/music_library'
$env:SPRING_DATASOURCE_USERNAME = 'root'
$env:SPRING_DATASOURCE_PASSWORD = ''
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'update'
$env:SERVER_ADDRESS = '127.0.0.1'
$env:SERVER_PORT = '8080'
Push-Location "$projectRoot\app\MainProject\QuanLyThuVienNhacOnline\web"
try {
    & "$($maven.FullName)\bin\mvn.cmd" "-Dmaven.repo.local=$projectRoot\.local\m2" spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw "Maven exited with code $LASTEXITCODE" }
} finally {
    Pop-Location
}
