# Stable UI Selector Contract

Use `data-testid` values as the primary selector strategy for UI automation. Treat these values as stable contracts once automated tests are created.

## Navigation

| Element | Selector |
| --- | --- |
| Main navigation | `dashboard-navigation` |
| Dashboard link | `nav-dashboard-link` |
| Tasks link | `nav-tasks-link` |
| Issues link | `nav-issues-link` |
| Admin link | `nav-admin-link` |
| Settings link | `nav-settings-link` |
| Profile link | `nav-profile-link` |
| SUT information link | `nav-sut-info-link` |
| Logout button | `nav-logout-button` |

## Login

| Element | Selector |
| --- | --- |
| Seed users block | `seed-users` |
| Login error | `login-error` |
| Logout success | `logout-success` |
| Language switcher | `language-switcher` |
| Language select | `language-select` |

## Tasks

| Element | Selector |
| --- | --- |
| Task table | `task-table` |
| Task row | `task-row-{id}` |
| Status filter | `task-status-filter` |
| Clear filters | `clear-task-filters-button` |
| Empty state row | `task-empty-state-row` |
| Pagination | `task-pagination` |
| Pagination summary | `task-pagination-summary` |
| Create task link | `create-task-link` |
| View/Edit/Delete task | `view-task-{id}`, `edit-task-{id}`, `delete-task-{id}` |
| Task content translation toggle | `content-translation-toggle` on task details |

## Issues

| Element | Selector |
| --- | --- |
| Issue table | `issue-table` |
| Issue row | `issue-row-{id}` |
| Status filter | `issue-status-filter` |
| Priority filter | `issue-priority-filter` |
| Title search | `issue-title-search` |
| Clear filters | `clear-issue-filters-button` |
| Empty state row | `issue-empty-state-row` |
| Pagination | `issue-pagination` |
| Pagination summary | `issue-pagination-summary` |
| Create issue link | `create-issue-link` |
| View/Edit/Delete issue | `view-issue-{id}`, `edit-issue-{id}`, `delete-issue-{id}` |
| Issue content translation toggle | `content-translation-toggle` on issue details |

## Settings

| Element | Selector |
| --- | --- |
| Settings page title | `settings-title` |
| Admin-only note | `settings-admin-only-note` |
| Reset data button | `settings-reset-data-button` |
| Clear data card | `settings-clear-card` |
| Clear data form | `settings-clear-data-form` |
| Clear data button | `settings-clear-data-button` |
| Clear data confirmation field | `clear-data-confirmation-field` |
| Clear data confirmation input | `clear-data-confirmation-input` |
| Load demodata button | `settings-load-demodata-button` |
| Import file input | `settings-import-file-input` |
| Import button | `settings-import-data-button` |
| Confirmation modal | `settings-confirmation-modal` |
| Success message | `settings-success-message` |
| Error message | `settings-error-message` |
| Data summary | `settings-data-summary` |

## SUT Information

| Element | Selector |
| --- | --- |
| Page title | `sut-info-title` |
| Details grid | `sut-info-details` |
| Display name | `sut-info-display-name` |
| Version | `sut-info-version` |
| Active profiles | `sut-info-active-profiles` |
| Java version | `sut-info-java-version` |
| Spring Boot version | `sut-info-spring-version` |
| Database product | `sut-info-database-product` |
| Database version | `sut-info-database-version` |
| Operating system | `sut-info-os` |
| Architecture | `sut-info-architecture` |

## Content Translation

| Element | Selector |
| --- | --- |
| Translation control | `content-translation-control` |
| Translation toggle | `content-translation-toggle` |
| Tooltip icon | `content-translation-tooltip-icon` |

## Shared Components

| Element | Selector |
| --- | --- |
| Global search | `global-search-input` |
| Toast notification | `toast-notification` |
| Delete confirmation modal | `delete-confirmation-modal` |
| Delete cancel | `delete-cancel-button` |
| Delete confirm | `delete-confirm-button` |
| Notification bell | `notification-bell` |
| Notification badge | `unread-notification-badge` |
| Notification dropdown | `notification-dropdown` |
| Notification item | `notification-item` |
| Mark notification read | `mark-notification-read-button` |
| Error page | `error-page` |
| Error status/title/message | `error-status`, `error-title`, `error-message` |
