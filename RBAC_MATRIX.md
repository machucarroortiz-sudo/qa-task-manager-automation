# RBAC Matrix

This matrix documents expected access for role-based UI and API checks.

## Users

| User | Role |
| --- | --- |
| `admin@example.com` | `ADMIN` |
| `user1@example.com` | `USER` |
| `user2@example.com` | `USER` |

## Tasks

| Action | ADMIN | Task owner | Other USER |
| --- | --- | --- | --- |
| List visible tasks | All tasks | Own tasks | Own tasks |
| View task | Allowed | Allowed | Forbidden |
| Create task | Allowed, owner is current user | Allowed, owner is current user | Allowed, owner is current user |
| Update task | Allowed | Allowed | Forbidden |
| Complete task | Allowed | Allowed | Forbidden |
| Delete task | Allowed | Allowed | Forbidden |

## Issues

| Action | ADMIN | Creator USER | Assigned USER | Other USER |
| --- | --- | --- | --- | --- |
| List visible issues | All issues | Created or assigned issues | Created or assigned issues | Created or assigned issues only |
| View issue | Allowed | Allowed | Allowed | Forbidden |
| Create issue | Allowed, creator is current user | Allowed, creator is current user | Allowed, creator is current user | Allowed, creator is current user |
| Update issue | Allowed | Allowed | Forbidden unless creator | Forbidden |
| Delete issue | Allowed | Allowed | Forbidden unless creator | Forbidden |
| Add comment | Allowed when visible | Allowed when visible | Allowed when visible | Forbidden when not visible |
| Upload attachment | Allowed when comment issue is visible | Allowed when comment issue is visible | Allowed when comment issue is visible | Forbidden when not visible |

## Labels

| Action | ADMIN | USER |
| --- | --- | --- |
| List labels | Allowed | Allowed |
| Create label | Allowed | Forbidden |
| Update label | Allowed | Forbidden |
| Delete label | Allowed | Forbidden |

## Settings and Test Data

| Action | ADMIN | USER |
| --- | --- | --- |
| Open Settings UI | Allowed | Forbidden |
| Reset SUT data | Allowed | Forbidden |
| Load demodata | Allowed | Forbidden |
| Import test data | Allowed | Forbidden |

## Notifications

| Action | Recipient | Other authenticated user |
| --- | --- | --- |
| List unread notifications | Allowed | Only own notifications returned |
| Mark notification as read | Allowed | Forbidden |
