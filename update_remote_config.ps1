# PowerShell wrapper for update_remote_config.py
# Usage:
#   .\update_remote_config.ps1            # Publishes to Firebase Remote Config
#   .\update_remote_config.ps1 --dry-run  # Validates and prepares template without deploying

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

python "$scriptDir\update_remote_config.py" @args
