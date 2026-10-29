$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
Write-Host '=== Event & Ticketing System (JavaFX) ===' -ForegroundColor Cyan

# Find JAVA_HOME even when JDK is installed through IntelliJ but not in PATH.
function Find-Jdk {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) { return $env:JAVA_HOME }
    $candidate = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($candidate) { return (Split-Path -Parent (Split-Path -Parent $candidate.Source)) }
    $roots = @(
      (Join-Path $env:USERPROFILE '.jdks'),
      'C:\Program Files\Java',
      'C:\Program Files\Eclipse Adoptium',
      'C:\Program Files\Microsoft'
    )
    foreach ($r in $roots) {
       if (Test-Path $r) {
         $found = Get-ChildItem -LiteralPath $r -Directory -ErrorAction SilentlyContinue |
           Where-Object {Test-Path (Join-Path $_.FullName 'bin\javac.exe')} |
           Sort-Object Name -Descending | Select-Object -First 1
         if ($found) {return $found.FullName}
       }
    }
    return $null
}
$jdk = Find-Jdk
if (-not $jdk) {
    Write-Host 'Khong tim thay JDK 17+. Hay cai JDK 17/21 hoac chon JDK trong IntelliJ.' -ForegroundColor Red
    exit 1
}
$env:JAVA_HOME = $jdk
$env:Path = (Join-Path $jdk 'bin') + ';' + $env:Path
Write-Host "JDK: $jdk"

# Prefer system Maven; otherwise use IntelliJ bundled Maven; otherwise download portable Maven once.
$mvn = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($mvn) { $mvn = $mvn.Source }
if (-not $mvn) {
    $ideaRoot = 'C:\Program Files\JetBrains'
    if (Test-Path $ideaRoot) {
        $candidates = Get-ChildItem -LiteralPath $ideaRoot -Directory -ErrorAction SilentlyContinue |
            ForEach-Object {Join-Path $_.FullName 'plugins\maven\lib\maven3\bin\mvn.cmd'} |
            Where-Object {Test-Path $_}
        $mvn = $candidates | Select-Object -First 1
    }
}
if (-not $mvn) {
    $mavenVer = '3.9.11'
    $mavenBase = Join-Path (Get-Location) '.tools'
    $mavenHome = Join-Path $mavenBase ('apache-maven-' + $mavenVer)
    $mvn = Join-Path $mavenHome 'bin\mvn.cmd'
    if (-not (Test-Path $mvn)) {
        Write-Host 'Chua co Maven; dang tai Maven portable tu Maven Central (chi lan dau)...' -ForegroundColor Yellow
        New-Item -ItemType Directory -Path $mavenBase -Force | Out-Null
        $zipFile = Join-Path $mavenBase 'apache-maven.zip'
        $uri = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVer/apache-maven-$mavenVer-bin.zip"
        try {
           [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
           Invoke-WebRequest -Uri $uri -OutFile $zipFile -UseBasicParsing
           Expand-Archive -LiteralPath $zipFile -DestinationPath $mavenBase -Force
           Remove-Item -LiteralPath $zipFile -Force
        } catch {
           Write-Host ('Khong tai duoc Maven: ' + $_.Exception.Message) -ForegroundColor Red
           Write-Host 'Cach khac: IntelliJ -> Maven -> Plugins -> javafx -> javafx:run'
           exit 1
        }
    }
}
Write-Host "Maven: $mvn"
Write-Host 'Lan dau can Internet de Maven tai thu vien JavaFX...' -ForegroundColor Yellow
& $mvn javafx:run
exit $LASTEXITCODE
