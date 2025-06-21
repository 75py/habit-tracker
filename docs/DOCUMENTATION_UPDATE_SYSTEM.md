# Documentation Update System

This document describes the interactive documentation update system for the Habit Tracker project.

## Overview

The project adopts a systematic approach to capture and document development knowledge interactively. This ensures that valuable insights and solutions discovered during development are preserved and organized properly.

## Update Rules

### When to Propose Updates

Documentation updates should be proposed in these situations:

1. **After resolving errors or problems** - Document the solution for future reference
2. **When discovering efficient implementation patterns** - Share best practices with the team
3. **When establishing new API/library usage methods** - Document integration approaches
4. **When finding outdated/incorrect documentation** - Keep documentation current
5. **When discovering frequently referenced information** - Make knowledge easily accessible
6. **After completing code review fixes** - Document common review feedback
7. **When adding new features or components** - Document new functionality

### Proposal Format

When proposing documentation updates, use this format:

```
💡 Documentation update proposal: [situation description]
【Update Content】 [specific additions/modifications]
【Update Candidates】
1. /docs/ARCHITECTURE.md - [reason]
2. /docs/CODING_STANDARDS.md - [reason]  
3. Create new file - [reason]

Which should be updated? (select number or "skip")
```

### Approval Process

1. **User selects update destination** - Choose which document to update
2. **Preview actual update content** - Review the proposed changes
3. **User gives final approval** - Confirm with yes/edit/no
4. **Update file after approval** - Apply the changes

## Integration Guidelines

### Existing Documentation

- Follow existing documentation format and style
- Reference related existing content when applicable  
- Include update date (YYYY-MM-DD format) in history

### Important Constraints

1. **Never update files without user approval**
2. **Only add content, never delete or modify existing content**
3. **Never record sensitive information** (API keys, passwords, etc.)
4. **Follow project conventions and style guides**

## Documentation Split Management

To prevent documentation files from becoming too large:

### When to Split

- **When exceeding 100 lines**: Consider splitting related content to separate files
- **When topics diverge**: Separate unrelated content into focused documents
- **When sections become complex**: Extract detailed sections to dedicated files

### Recommended Structure

- `/docs/UPDATE_SYSTEM.md` - Documentation update system rules (this file)
- `/docs/PROJECT_SPECIFIC.md` - Project-specific settings and configurations
- `/docs/REFERENCE_LIST.md` - Comprehensive list of all documentation
- Keep only overview and links in main documents

### Linking Strategy

- Use relative links between documentation files
- Maintain a table of contents in key documents
- Cross-reference related documentation

## Interactive Rule Addition Process

When receiving instructions that appear to require continuous application:

1. **Always ask**: "Should this be made a standard rule?"
2. **If YES is received**: Add it as an additional rule to appropriate documentation
3. **Apply it as standard rule thereafter**

This process enables continuous improvement of project documentation through active interaction.

## Best Practices

### Documentation Quality

- Write clear, concise explanations
- Use examples when applicable
- Include code snippets for technical documentation
- Maintain consistent formatting

### Organization

- Group related information together
- Use meaningful headings and subheadings
- Create logical flow between sections
- Include navigation aids (TOC, links)

### Maintenance

- Review documentation regularly
- Update outdated information promptly
- Archive deprecated documentation
- Track major changes in update history

## Update History

- 2025-06-21: Initial documentation created from CLAUDE.md interactive update system