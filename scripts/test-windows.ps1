$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
$jdk = $env:JAVA_HOME
if (-not $jdk -or -not (Test-Path (Join-Path $jdk 'bin\javac.exe'))) {
  $roots = @((Join-Path $env:USERPROFILE '.jdks'), 'C:\Program Files\Java', 'C:\Program Files\Eclipse Adoptium')
  foreach ($root in $roots) {
    if (Test-Path $root) {
      $cand=Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue | Where-Object { Test-Path (Join-Path $_.FullName 'bin\javac.exe') } | Sort-Object Name -Descending | Select-Object -First 1
      if($cand) {$jdk=$cand.FullName;break}
    }
  }
}
if(-not $jdk) { Write-Host 'Khong tim thay JDK.'; exit 1 }
$env:JAVA_HOME=$jdk
$env:Path=(Join-Path $jdk 'bin')+';'+$env:Path
$source=Get-ChildItem 'src\main\java\com\nhom5\eventticket' -Recurse -Filter '*.java' | Where-Object { $_.FullName -notlike '*\ui\*' -and $_.Name -ne 'MainApp.java' } | ForEach-Object { $_.FullName }
New-Item -Force -ItemType Directory 'out\test' | Out-Null
& javac -encoding UTF-8 -d 'out\test' $source 'src\test\java\com\nhom5\eventticket\BasicLogicTest.java'
if($LASTEXITCODE -ne 0) {exit $LASTEXITCODE}
& java -cp 'out\test' com.nhom5.eventticket.BasicLogicTest
exit $LASTEXITCODE
