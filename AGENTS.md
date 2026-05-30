# Smart Farm Project - AI Coding Agent Guidelines

## 1. Tech Stack & Environment
- **Backend**: Java 21 (LTS), Spring Boot 3.4.3, Spring Data JPA
- **Database**: MySQL 
- **IoT Architecture**: MQTT Protocol via Spring Integration (Using `MessagingGateway`)
    - MQTT Broker URL: `tcp://host.docker.internal:1883` (Dockerized environment)
- **Frontend**: React / TypeScript (Port: 3000 / 5173 with CORS sharing)

## 2. Core Architectural Rules (CRITICAL)
- **Layered Architecture**: Always maintain a strict separation of concerns: `Controller` ➡️ `Service` ➡️ `Repository`.
    - Controllers must NEVER call Repositories directly. Always route via Service layers (e.g., `UserStatusService`).
- **Transaction Management**:
    - For data queries, always explicit use `@Transactional(readOnly = true)` to optimize memory snapshotting.
    - Rely on Hibernate **Dirty Checking** (변경 감지) for entities inside `@Transactional` blocks. Avoid boilerplate `.save()` calls for updates.
- **Fail-Fast Policy (Transaction Contamination Defense)**:
    - Before running any database mutation/insert within loops, always implement a defensive check utilizing `existsBy...` (e.g., `existsByUser_IdAndAssignedDate`).
    - Never catch DB-level Unique Constraints errors with raw java `try-catch` inside the same transaction context, as it marks the transaction as `rollback-only` and triggers an `UnexpectedRollbackException`.

## 3. Database & Entity Specifications
- **User Entity**:
    - Contains a primitive `int xp = 0` field for the gamified quest rewards.
    - When using Lombok `@Builder`, remember that primitive `int` fields default to `0` automatically; do not force `@Builder.Default` unless the base default shifts from zero.
- **Spring Scheduled Cron Tab**:
    - Use the exact standard Spring Cron format: `0 0 0 * * *` for daily midnight updates. Avoid hyper-frequent test ticks in production code.

## 4. Git & Branching Hygiene
- **Branch Naming Convension**: Use `feature/kebab-case-description` or `feature/action-target` (e.g., `feature/serve-quests`). Always lowercase with hyphens.
- **Git Commit Exclusion**: NEVER stage or commit heavy asset archives, tarballs (`*.tar`), compilation output (`/build/`, `/target/`), or dependency catalogs (`node_modules/`). Ensure `.gitignore` intercepts them before indexing.

## 5. Interaction Policy
- **Language**: Respond exclusively in Korean unless explicitly requested otherwise.
- **Response Style**: Be professional, technically precise, skip fluff, and focus on clean, secure, and production-ready enterprise code patterns.