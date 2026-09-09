# Sources of Truth & Conflict Precedence (SOURCES_OF_TRUTH.md)

When two documents, code snippets, or team assumptions conflict during rapid execution under the 30-hour clock, apply the following strict hierarchy of precedence. The higher-ranked source always overrides lower-ranked sources without debate.

```
+-------------------------------------------------------------------------+
| Level 1: Live Hackathon Organiser Rulings & Check-In Clarifications    |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
| Level 2: DECISIONS.md (Locked Technical & Architectural Choices)        |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
| Level 3: CONSTRAINTS.md (Hardware Limits, Latency Budgets, Permissions)|
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
| Level 4: SCOPE.md (Strict MVP Boundaries & Out-of-Scope Rules)          |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
| Level 5: DATA_MODELS.md (Database Schemas & Ephemeral Models)           |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
| Level 6: TECH_STACK.md (Locked Libraries, Dependencies, Versions)       |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
| Level 7: ARCHITECTURE.md & FLOW.md (System Pipelines & Workflows)       |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
| Level 8: PRD.md (High-Level Product Requirements & Problem Definition)  |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
| Level 9: TASKS.md (Operational Checklist & Sprints)                     |
+-------------------------------------------------------------------------+
```

## Conflict Resolution Protocol
1. If code or library usage violates `CONSTRAINTS.md` (e.g., adding `INTERNET` permission or running at 30 FPS), it must be rejected immediately regardless of feature utility.
2. If an implementation idea conflicts with `DECISIONS.md` (e.g., switching to Flutter, raw llama.cpp, or 2D coordinates), the decision in `DECISIONS.md` stands without discussion.
3. If an proposed feature is listed under `Out-of-Scope` in `SCOPE.md`, work on it is halted until all MVP tasks in `TASKS.md` are signed off.
