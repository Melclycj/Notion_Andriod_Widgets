# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Build a lightweight Android companion application that extends Notion’s usability on the home screen by providing a dedicated widget for viewing and updating a Notion-based to-do list. The app’s core responsibility is authentication and secure API communication; all task storage and business logic remains in Notion via official Notion APIs.

##Technology requirements

Language: Kotlin
Widget framework: AppWidgetProvider (Android App Widgets)
UI: Jetpack Compose (for in-app authentication/settings UI; widget UI uses RemoteViews/AppWidget constraints unless using Glance)
Build system: Gradle (Kotlin DSL preferred)

## Hooks System

This repo has an active hooks system (`hooks.json` + `scripts/hooks/`). Be aware of:

- **Write hook**: Blocks creation of `.md`/`.txt` files except `README.md`, `CLAUDE.md`, `AGENTS.md`, `CONTRIBUTING.md`. Keep documentation consolidated.
- **Post-edit hooks**: JS/TS files are auto-formatted with Prettier, type-checked, and scanned for `console.log` after edits.
- **Stop hook**: Checks for leftover `console.log` in modified files at end of each response — clean them up before finishing.
- **Dev server hook** (Linux/macOS only): Dev servers must run inside tmux.

## Conventions

### Git Commits

Use conventional commit format: `<type>: <description>` where type is one of: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`, `ci`.

### Code Style

- Prefer immutability — create new objects rather than mutating existing ones.
- Keep files small: 200-400 lines typical, 800 max.
- Functions under 50 lines, nesting under 4 levels.
- Organize code by feature/domain, not by type.
- No emojis in codebase
- Refrain from purple hues in frontend
- Always test code before deployment
- Prioritize modular code over mega-files
- Never commit console.logs
