# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🔨 Critical Rule - Interactive Rule Addition Process

When receiving instructions from users that appear to require continuous application:

1. **Always ask**: "Should this be made a standard rule?"
2. **If YES is received**: Add it as an additional rule to CLAUDE.md
3. **Apply it as standard rule thereafter**

This process enables continuous improvement of project rules through active interaction.

## 📚 Interactive Documentation Update System

This project uses an interactive system to capture and document development knowledge. See `/docs/DOCUMENTATION_UPDATE_SYSTEM.md` for complete details.

### Quick Reference - When to Propose Updates

Propose documentation updates after:
- Resolving errors or problems
- Discovering efficient implementation patterns  
- Establishing new API/library usage methods
- Finding outdated/incorrect documentation
- Discovering frequently referenced information

### Proposal Format
```
💡 Documentation update proposal: [situation description]
【Update Content】 [specific additions/modifications]
【Update Candidates】
1. /docs/ARCHITECTURE.md - [reason]
2. /docs/CODING_STANDARDS.md - [reason]  
3. Create new file - [reason]

Which should be updated? (select number or "skip")
```

## 📖 Essential Documentation

Always review these documents before starting work:

- `/docs/ARCHITECTURE.md` - Complete system design, three-layer architecture, and platform specifics
- `/docs/CODING_STANDARDS.md` - Logging requirements, communication guidelines, and coding conventions
- `/docs/SPECIFICATIONS.md` - Screen specifications and UI requirements
- `/docs/DEVELOPMENT_COMMANDS.md` - Build, test, and maintenance commands
- `/docs/DOCUMENTATION_UPDATE_SYSTEM.md` - Full documentation update process and guidelines
- `/docs/FIREBASE_APP_DISTRIBUTION.md` - Firebase setup guide (Japanese)
- `/docs/IOS_SWIPE_BACK.md` - iOS gesture implementation details

## 🚀 Quick Start

### Build and Test
```bash
# Build everything
./gradlew clean build

# Run unit tests
./gradlew testDebugUnitTest

# Run lint checks
./gradlew lint
```

See `/docs/DEVELOPMENT_COMMANDS.md` for complete command reference.

### Key Project Information

- **Architecture**: Kotlin Multiplatform with Compose Multiplatform
- **Pattern**: Clean Architecture (Presentation → Domain → Data)
- **Database**: Room with Kotlin Multiplatform support
- **DI**: Koin for dependency injection
- **Logging**: Must use `Logger.e()` in all catch blocks

## ⚠️ Important Constraints

1. **Never update files without user approval**
2. **Always check `/docs` directory before starting work**
3. **Follow the interactive documentation update process**
4. **Use Japanese for PR titles/descriptions, English for code**
