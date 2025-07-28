Git Branching Strategy
Project: DFS Financial Compliance Management System
Version Control: Git + GitHub
Date: July 28, 2025

Branching Model

We use a simplified Git Flow.

Branch Types

main  
develop  
feature/*  
bugfix/*  
hotfix/*


Main Branches

main Branch
Purpose: production-ready code
Protection: protected, no direct commits
Merges from: develop, hotfix
Deploy target: Production

Rules: tests pass, docs updated, version tagged.

develop Branch
Purpose: integration for new work
Merges from feature and bugfix
Merged regularly to main
Must be stable; CI runs on all pushes.

Supporting Branches

Feature Branch
Naming: feature/description
Created from develop, merged back, then deleted.
Example actions:

git checkout develop
git checkout -b feature/database-schema
git add .
git commit -m "Add database schema"
git checkout develop
git merge feature/database-schema --no-ff
git branch -d feature/database-schema


Bugfix Branch
Naming: bugfix/description
Created from develop, merged back.

Hotfix Branch
Naming: hotfix/description
Created from main, merged to main and develop.
Used for urgent fixes.

Commit Message Convention

Format:

type: subject


Use types like feat, fix, docs, chore, refactor, test, perf, security, database, ui.
Subject: imperative mood, no period, ~50 char limit.

Good commit examples:
feat: add entity endpoint
fix: resolve null pointer
docs: add API docs
refactor: extract validation utility

Version Tagging

Use semantic versioning: vMAJOR.MINOR.PATCH.

Examples:
v1.0.0 initial release
v1.1.0 new feature
v1.1.1 bug fix

Tagging workflow:

git checkout main
git tag -a v1.0.0 -m "Initial release"
git push origin v1.0.0


Pull Request Guidelines

Include: brief description, change type, checklist (style, self-review, docs, tests), testing notes.

Review checklist: readable code, no sensitive data, adequate error handling, good logging, tests cover changes.

Workflow Examples

Typical Feature:

git checkout develop
git pull
git checkout -b feature/apex-dashboard
git add .
git commit -m "Add dashboard skeleton"
git checkout develop
git merge feature/apex-dashboard --no-ff
git push


Release:

git checkout develop
git pull
git checkout -b release/v1.0.0
git commit -m "Update version"
git checkout main
git merge release/v1.0.0 --no-ff
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin main v1.0.0
git checkout develop
git merge release/v1.0.0
git push
git branch -d release/v1.0.0


Best Practices

Commit frequency: logical units, daily pushes.
Keep branches short.
Self-review before merging.
Never commit credentials, large binaries, build artifacts, local configs.

GitHub Integration

.gitignore is preconfigured.

Future CI (example):

name: CI
on: push, pull_request
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2 with java-version 11
      - run: mvn clean install
      - run: mvn test


Use GitHub Issues for bugs, enhancements, docs, questions.

Emergency Procedures

Undo last local commit:

git reset --soft HEAD~1


Undo pushed commit:

git revert HEAD
git push


Summary

main is production-ready, develop is integration, feature and bugfix branches for work, clear commit messages, frequent pushes, semantic tags, protect sensitive data.