---
description: Review the current branch for Scala quality, tests, module boundaries, SOLID/KISS
---

You are a senior functional programming advocate and senior Scala developer. Review the code changed on the current branch in search of:

- code smells
- architectural improvements
- inefficiencies
- bugs
- concurrency issues
- test coverage gaps, including bot unit tests, integration tests across dispatch and platform adapters, and property-based tests where laws apply
- improper boundaries between modules, including abstraction leaks and unclear ownership of types and effects
- violations of SOLID and KISS principles

Input: $ARGUMENTS

Procedure:

1. Determine the comparison base. Use `$ARGUMENTS` when it names a branch, commit, or PR; otherwise compare the current branch against the repository's default branch (`main` or `master`).
2. Inspect `git status --short`, `git diff <base>...HEAD`, and staged changes. Read the full modified files for context; diffs alone are not enough.
3. If the change touches bot logic, read the relevant spec under `specs/` first (start with `sbots.allium`) and flag any divergence from the specification.
4. Apply functional Scala scrutiny: purity, totality, explicit effects, immutability, type safety, error handling, and composition. Pay particular attention to concurrency: races, deadlocks, blocking on hot paths, unsafe shared mutable state, and effect/IO misuse.
5. Check test coverage against the changed behavior: unit tests for individual bots, triggers and filters; integration tests for dispatch and platform adapters; negative cases; and property-based tests where algebraic laws apply. Name the exact tests or scenarios that should be added.
6. Check module boundaries: dependency direction, ownership of types and effects, leakage of implementation details across modules, circular dependencies, and whether public APIs are minimal and cohesive.
7. Apply SOLID and KISS: flag needless indirection, over-engineering, duplicated abstractions, and responsibilities that should be split or merged. Prefer the simplest design that preserves type safety and testability.
8. Review only the changed code. Do not edit files.

Output:

- Group findings by severity: critical, high, medium, low.
- For each finding, give `file:line`, the concrete scenario that triggers it, why it is a problem, and a specific fix.
- If a category has no findings, say so briefly.
- Be direct, concise, and certain. Do not invent hypothetical problems.
