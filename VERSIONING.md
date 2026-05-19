# SUT Versioning

The SUT uses semantic versioning:

```text
MAJOR.MINOR.PATCH
```

## Baseline

Version `1.0.0` is the first formal QA baseline of the SUT. Versions before `1.0.0` are considered initial development snapshots.

The current implementation is `1.0.0`. This is the first formal QA-ready baseline and includes SUT metadata, GUI localization, and assisted task/issue content translation.

## Rules

- `PATCH`, for example `1.0.1`: defect fixes found during QA that do not intentionally change the expected functional behavior.
- `MINOR`, for example `1.1.0`: a meaningful set of compatible improvements or new capabilities.
- `MAJOR`, for example `2.0.0`: important or incompatible changes to SUT flows, API contracts, permissions, data model, or overall behavior.

## Operating Criteria

After each code change, evaluate whether the SUT version should be updated before handing the application back to QA.

Every issue report should include the exact SUT version where the behavior was observed.

The Maven project version and application metadata version must stay aligned.
