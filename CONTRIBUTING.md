# Contributing to Viewa
Thank you for your interest in contributing! Here's how to get started.
## Branch model
| Branch | Purpose |
|---|---|
| `master` | Stable, release-ready code. Protected — only merge via PR. |
| `development` | Active integration branch. All feature branches merge here first. |
| `feature/<name>` | Short-lived branches for new features or bug-fixes. Branch off `development`. |
**Workflow:**
`feature/<name>` -> PR -> `development` -> PR -> `master` (on release)
## Getting started
1. Fork the repository and clone your fork.
2. Set up Android Studio (Arctic Fox or newer) and let Gradle sync.
3. Make sure the NDK is installed (r25c is used in CI).
## Submitting changes
1. Create a feature branch off `development`.
2. Make your changes, keeping commits focused and well-described.
3. Open a Pull Request **targeting `development`** (not `master`).
4. Fill in the PR template and link any related issues.
## Code style
- Follow standard Kotlin/Android conventions.
- Keep new code consistent with the existing style.
## Reporting bugs / requesting features
Please use [GitHub Issues](https://github.com/NeuropsyOL/VIEWA/issues) and choose the appropriate template.
## License
By contributing you agree that your contributions will be licensed under the [GPL v3 License](LICENSE).
