# GitHub Setup for EasEBudget

## Initial Repository Setup

1. **Create GitHub Repository:**
   - Go to https://github.com and create a new repository named "EasEBudget"
   - Choose "Public" or "Private" based on your preference
   - Don't initialize with README, .gitignore, or license (we already have these)

2. **Connect Local Repository to GitHub:**
   ```bash
   git remote add origin https://github.com/YOUR_USERNAME/EasEBudget.git
   git branch -M main
   git push -u origin main
   ```

## Regular Commit Workflow

### Daily Development Commits
```bash
# Check current status
git status

# Add all changes
git add .

# Commit with descriptive message
git commit -m "feat: Add user profile settings implementation

- Added email and phone number validation
- Implemented language selection dropdown
- Added biometric authentication toggle
- Updated UI with Material Design components"

# Push to GitHub
git push origin main
```

### Commit Message Format
Use conventional commits for better version tracking:

- `feat:` - New features
- `fix:` - Bug fixes
- `refactor:` - Code refactoring
- `style:` - Code style changes
- `docs:` - Documentation updates
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks

### Example Commit Messages:
```bash
git commit -m "feat: Implement transaction receipt attachment feature"
git commit -m "fix: Resolve biometric authentication crash on older devices"
git commit -m "refactor: Optimize database queries for better performance"
git commit -m "style: Update color scheme for better accessibility"
```

## Branching Strategy

### Feature Branches
```bash
# Create new feature branch
git checkout -b feature/shared-accounts-enhancement

# Work on feature...
git add .
git commit -m "feat: Add approval workflow for shared account transactions"

# Merge back to main
git checkout main
git merge feature/shared-accounts-enhancement
git push origin main

# Delete feature branch
git branch -d feature/shared-accounts-enhancement
git push origin --delete feature/shared-accounts-enhancement
```

## Automated Workflow Script

Create a PowerShell script for daily commits:

```powershell
# commit-daily.ps1
$cd = "C:\Users\jadea\AndroidStudioProjects\Testapp"
Set-Location $cd

# Check if there are changes
$status = git status --porcelain
if ($status) {
    $date = Get-Date -Format "yyyy-MM-dd"
    git add .
    git commit -m "chore: Daily development update - $date"
    git push origin main
    Write-Host "Changes committed and pushed to GitHub"
} else {
    Write-Host "No changes to commit"
}
```

## Best Practices

1. **Commit Frequently:** Small, focused commits are easier to review and debug
2. **Write Clear Messages:** Explain what changed and why
3. **Test Before Pushing:** Ensure your code compiles and runs
4. **Use Branches:** Keep main branch stable, use feature branches for development
5. **Regular Backups:** Push to GitHub at least daily

## GitHub Actions (Optional)

You can set up automated workflows for:
- Code quality checks
- Automated testing
- CI/CD pipeline
- Dependency updates

Create `.github/workflows/ci.yml` for automated builds.

## Repository Structure
```
EasEBudget/
├── .github/workflows/     # GitHub Actions
├── app/src/main/         # Main source code
├── docs/                 # Documentation
├── scripts/              # Utility scripts
└── README.md            # Project documentation
```
