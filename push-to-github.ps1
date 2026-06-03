# MythicTools - Upload to GitHub Script
# Run this script from the root of MythicTools folder

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  MythicTools to GitHub Uploader" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check if git is installed
try {
    git --version | Out-Null
} catch {
    Write-Host "ERROR: Git is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Get GitHub username
$username = Read-Host "GitHub username (e.g., blackowlzz)"
$repoName = "MythicTools"

Write-Host ""
Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  Username:    $username"
Write-Host "  Repository:  $repoName"
Write-Host "  Remote URL:  https://github.com/$username/$repoName.git"
Write-Host ""
Write-Host "Make sure you have already created the repo on GitHub!" -ForegroundColor Yellow
$confirm = Read-Host "Continue? (y/n)"
if ($confirm -ne "y") {
    Write-Host "Aborted." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Initializing Git..." -ForegroundColor Cyan

# Check if .git already exists
if (Test-Path ".git") {
    Write-Host "Git repo already initialized. Checking remote..." -ForegroundColor Yellow
    $remoteUrl = git remote get-url origin 2>$null
    if ($remoteUrl) {
        Write-Host "Current remote: $remoteUrl" -ForegroundColor Gray
        Write-Host "Skipping init, proceeding with push..." -ForegroundColor Gray
    }
} else {
    Write-Host "Running: git init" -ForegroundColor Gray
    git init

    Write-Host "Configuring remote..." -ForegroundColor Gray
    git remote add origin "https://github.com/$username/$repoName.git"
}

Write-Host ""
Write-Host "Staging all files..." -ForegroundColor Cyan
Write-Host "Running: git add ." -ForegroundColor Gray
git add .

Write-Host ""
Write-Host "Creating commit..." -ForegroundColor Cyan
$commitMsg = @"
Initial release: MythicTools v1.0.0

- Drill 3x3 with realistic mining speed
- Tree Chopper with house protection
- Multitool with ToolComponent support (Paper 1.20.5+)
- Sell Chest with dynamic shop pricing
- Version checker (Modrinth API)
- Multi-version support (Paper 1.19-1.21+)
- GNU GPL v3.0 + mandatory attribution
"@

Write-Host "Committing..." -ForegroundColor Gray
git commit -m $commitMsg

Write-Host ""
Write-Host "Setting branch..." -ForegroundColor Cyan
Write-Host "Running: git branch -M main" -ForegroundColor Gray
git branch -M main

Write-Host ""
Write-Host "Pushing to GitHub..." -ForegroundColor Cyan
Write-Host "Running: git push -u origin main" -ForegroundColor Gray
git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "SUCCESS!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Your repo is live at:" -ForegroundColor Green
    Write-Host "https://github.com/$username/$repoName" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "Push failed. Check the error above." -ForegroundColor Red
    exit 1
}
