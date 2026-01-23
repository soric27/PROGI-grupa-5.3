# Loads key=value pairs from .env.local into the current PowerShell process environment
param(
  [string]$EnvFile = ".env.local"
)

$path = Join-Path -Path (Get-Location) -ChildPath $EnvFile
if (!(Test-Path $path)) {
  Write-Error ".env file not found: $path"
  exit 1
}

Get-Content -Raw $path |
  ForEach-Object { $_ -split "`n" } |
  ForEach-Object {
    $line = $_.Trim()
    if ([string]::IsNullOrWhiteSpace($line)) { return }
    if ($line.StartsWith('#')) { return }
    $idx = $line.IndexOf('=')
    if ($idx -lt 1) { return }
    $key = $line.Substring(0, $idx).Trim()
    $val = $line.Substring($idx + 1).Trim()
    if (($val.StartsWith('"') -and $val.EndsWith('"')) -or ($val.StartsWith("'") -and $val.EndsWith("'"))) {
      $val = $val.Substring(1, $val.Length - 2)
    }
    # Export to current process environment and $env:
    [System.Environment]::SetEnvironmentVariable($key, $val, 'Process')
    Set-Item -Path Env:$key -Value $val | Out-Null
  }

Write-Host "Loaded environment variables from $EnvFile"