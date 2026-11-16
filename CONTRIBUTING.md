# Contributing to TiemePlan

First of all, thank you for considering contributing to TiemePlan!

Contributions of all kinds are welcome, including bug reports, feature suggestions, documentation improvements and code contributions.

## Getting started

### Prerequisites

Before contributing, make sure you have:

- JDK 26
- Git

### Setting up the project

1. Fork the repository on GitHub.
2. Clone your fork:

   ```bash
   git clone https://github.com/YOUR_USERNAME/tiemeplan.git
   cd tiemeplan
   ```

3. Build the project:

   ```bash
   ./mvnw clean install
   ```

4. Open the project in your preferred IDE.

## Reporting bugs

If you encounter a bug, please open a [GitHub issue](https://github.com/JoranVanBelle/tiemeplan/issues).

When reporting a bug, try to include:

- A clear description of the problem.
- Steps to reproduce the issue.
- Expected behaviour and actual behaviour.
- The diagnostic information, found in the bottom left corner in the About-dialog.
- Screenshots or error logs, if applicable.

Please check existing issues before creating a new one to avoid duplicates.

## Suggesting features

Feature suggestions are welcome!

Open a GitHub issue describing:

- The problem you are trying to solve.
- Your proposed solution.
- Why you think the feature would be useful.

For larger changes, it's best to discuss the proposal before starting implementation.

## Submitting changes

1. Create a branch for your changes:

   ```bash
   git checkout -b feat/your-feature
   ```

2. Make your changes.
3. Run the tests:

   ```bash
   ./mvnw verify
   ```

4. Ensure the project builds successfully.
5. Commit your changes.
6. Push your branch to your fork.
7. Open a pull request against the `main` branch.

### Pull request guidelines

Please make sure your pull request:

- Has a clear title and description.
- Explains what was changed and why.
- References related issues, if applicable.
- Includes tests for new functionality where appropriate.
- Does not introduce unrelated changes.
- Contains only one commit.

## Code contributions

When contributing code:

- Follow the existing project structure and coding conventions.
- Prefer readable and maintainable solutions.
- Keep changes focused on a single feature or bug fix.
- Add or update tests when introducing or changing functionality.
- Update documentation when necessary.

## License

By contributing to TiemePlan, you agree that your contributions will be licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0), under the same terms as the project.