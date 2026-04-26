# Daily Git Commit Script for EasEBudget
# Run this script daily to commit your changes

$projectPath = "C:\Users\jadea\AndroidStudioProjects\Testapp"
Set-Location $projectPath

Write-Host "=== EasEBudget Daily Commit Script ===" -ForegroundColor Green
Write-Host "Checking for changes..." -ForegroundColor Yellow

# Check if there are changes to commit
$changes = git status --porcelain

if ($changes) {
    Write-Host "Changes detected:" -ForegroundColor Green
    Write-Host $changes
    
    $date = Get-Date -Format "yyyy-MM-dd HH:mm"
    $commitMessage = "chore: Daily development update - $date"
    
    Write-Host "Committing changes..." -ForegroundColor Yellow
    git add .
    
    if ($LASTEXITCODE -eq 0) {
        git commit -m $commitMessage
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Pushing to GitHub..." -ForegroundColor Yellow
            git push origin main
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✅ Successfully committed and pushed changes!" -ForegroundColor Green
                Write-Host "Commit message: $commitMessage" -ForegroundColor Cyan
            } else {
                Write-Host "❌ Failed to push to GitHub" -ForegroundColor Red
                Write-Host "Please check your internet connection and GitHub credentials" -ForegroundColor Yellow
            }
        } else {
            Write-Host "❌ Failed to commit changes" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Failed to add changes" -ForegroundColor Red
    }
} else {
    Write-Host "ℹ️ No changes to commit" -ForegroundColor Cyan
}

Write-Host "=== Script completed ===" -ForegroundColor Green
