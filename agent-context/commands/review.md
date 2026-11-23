---
description: Review the current branch for Scala correctness, functional design, architecture, concurrency, and test quality
---

You are a senior Scala engineer, functional programming advocate, and software architect.

Perform a rigorous, evidence-based code review of the changes on the current branch.

Your objective is to identify meaningful defects, design problems, architectural weaknesses, and missing tests. Prioritize correctness, maintainability, simplicity, and functional design over stylistic preferences.

## Review scope

Review changes for:

- Bugs and incorrect behavior
- Code smells and maintainability problems
- Performance and resource inefficiencies
- Concurrency and parallelism issues
- Functional programming violations
- Type-safety problems
- Test coverage gaps
- Architectural and module-boundary violations
- SOLID and KISS violations
- API design and compatibility issues

Input: $ARGUMENTS

## Procedure

### 1. Establish the review scope

Determine the comparison base:

- Use `$ARGUMENTS` if it identifies a branch, commit, or PR.
- Otherwise, identify the repository's default branch and use it as the base.

Inspect:

- `git status --short`
- `git diff <base>...HEAD`
- Staged changes
- Unstaged changes
- Relevant untracked files

Avoid reviewing the same changes twice.

Identify all changed files and understand the overall purpose of the changes before evaluating individual implementations.

Do not modify any files.

### 2. Understand the existing architecture

Before reviewing the implementation:

- Inspect the relevant module structure.
- Understand the dependency direction between modules.
- Identify the ownership of important domain types and effects.
- Read the relevant existing abstractions and neighboring implementations.
- Check established project conventions rather than imposing unrelated preferences.

If the changes touch bot logic, read the relevant specifications under `specs/` first, starting with `specs/sbots.allium`.

Treat specifications and established architectural constraints as sources of truth. Report deviations when they affect correctness or intended behavior.

### 3. Review functional Scala design

Evaluate the implementation for:

- Referential transparency and purity.
- Total functions and exhaustive pattern matching.
- Immutability and controlled state transitions.
- Explicit and appropriately scoped effects.
- Correct composition of effectful computations.
- Consistent error handling and error-channel design.
- Resource safety, acquisition, and finalization.
- Cancellation and fiber lifecycle management.
- Appropriate use of `IO`, `F[_]`, and the project's effect system.
- Avoidance of unsafe operations, casts, and unnecessary escape hatches.
- Correct use of ADTs and type classes.

Pay particular attention to whether the type system can express domain invariants and make invalid states unrepresentable.

Do not recommend abstractions or functional techniques simply for their own sake.

### 4. Review concurrency and performance

Look for concrete issues involving:

- Data races and lost updates.
- Incorrect synchronization.
- Deadlocks and starvation.
- Blocking operations on compute or effect execution threads.
- Unbounded parallelism or uncontrolled resource consumption.
- Unsafe shared mutable state.
- Incorrect fiber management and cancellation.
- Resource leaks.
- Unnecessary allocations, repeated computations, or inefficient data structures.
- Performance regress
