(function () {
    function showToastFromAlerts() {
        var alert = document.querySelector('[data-testid="success-message"], [data-testid="error-message"], .field-error');
        if (!alert || !alert.textContent.trim()) {
            return;
        }
        var toast = document.querySelector('[data-testid="toast-notification"]');
        if (!toast) {
            return;
        }
        toast.textContent = alert.textContent.trim();
        toast.classList.add('is-visible');
        if (alert.classList.contains('error') || alert.classList.contains('field-error')) {
            toast.classList.add('is-error');
        }
        window.setTimeout(function () {
            toast.classList.remove('is-visible', 'is-error');
        }, 3500);
    }

    function setupDeleteConfirmation() {
        var modal = document.querySelector('[data-testid="delete-confirmation-modal"], [data-testid="settings-confirmation-modal"]');
        if (!modal) {
            return;
        }
        var pendingForm = null;
        var message = modal.querySelector('[data-testid="delete-confirmation-message"]');
        var cancelButton = modal.querySelector('[data-testid="delete-cancel-button"]');
        var confirmButton = modal.querySelector('[data-testid="delete-confirm-button"]');
        var confirmationField = modal.querySelector('[data-testid="clear-data-confirmation-field"]');
        var confirmationInput = modal.querySelector('[data-testid="clear-data-confirmation-input"]');
        var requiredText = '';

        document.querySelectorAll('[data-confirm-delete="true"]').forEach(function (button) {
            button.addEventListener('click', function (event) {
                event.preventDefault();
                pendingForm = button.closest('form');
                requiredText = button.getAttribute('data-confirm-required-text') || '';
                message.textContent = button.getAttribute('data-confirm-message') || 'Delete this item?';
                if (confirmationField && confirmationInput) {
                    confirmationField.hidden = !requiredText;
                    confirmationInput.value = '';
                    confirmButton.disabled = Boolean(requiredText);
                }
                modal.classList.add('is-visible');
                if (requiredText && confirmationInput) {
                    confirmationInput.focus();
                }
            });
        });

        if (confirmationInput) {
            confirmationInput.addEventListener('input', function () {
                confirmButton.disabled = Boolean(requiredText) && confirmationInput.value.trim() !== requiredText;
            });
        }

        cancelButton.addEventListener('click', function () {
            pendingForm = null;
            requiredText = '';
            confirmButton.disabled = false;
            modal.classList.remove('is-visible');
        });

        confirmButton.addEventListener('click', function () {
            if (pendingForm && (!requiredText || confirmationInput.value.trim() === requiredText)) {
                pendingForm.submit();
            }
        });
    }

    function getStatusFromUrl() {
        return new URLSearchParams(window.location.search).get('status') || '';
    }

    function getSearchFromUrl() {
        return new URLSearchParams(window.location.search).get('search') || '';
    }

    function navigateWithParams(path, params) {
        var searchParams = new URLSearchParams();
        Object.keys(params).forEach(function (key) {
            var value = params[key];
            if (value !== null && value !== undefined && String(value).trim() !== '') {
                searchParams.set(key, value);
            }
        });
        var query = searchParams.toString();
        window.location.href = query ? path + '?' + query : path;
    }

    function setupTaskFilters() {
        var table = document.querySelector('[data-testid="task-table"]');
        if (!table) {
            return;
        }
        var tbody = table.querySelector('tbody');
        var statusFilter = document.querySelector('[data-testid="task-status-filter"]');
        var clearButton = document.querySelector('[data-testid="clear-task-filters-button"]');
        var globalSearch = document.querySelector('[data-testid="global-search-input"]');
        var emptyRow = document.querySelector('[data-testid="task-empty-state-row"]');
        var rows = Array.prototype.slice.call(document.querySelectorAll('[data-testid^="task-row-"]'));
        var emptyCell = emptyRow ? emptyRow.querySelector('td') : null;
        var sortState = {key: null, direction: 'asc'};

        function applyFilters() {
            var status = statusFilter.value;
            var query = normalize(globalSearch ? globalSearch.value : '');
            var visibleCount = 0;
            applySort(tbody, rows, emptyRow, sortState);

            rows.forEach(function (row) {
                var title = normalize(row.getAttribute('data-title') || row.getAttribute('data-sort-title') || '');
                var visible = (!status || row.getAttribute('data-status') === status)
                    && (!query || title.indexOf(query) !== -1);
                row.hidden = !visible;
                if (visible) {
                    visibleCount += 1;
                }
            });

            if (emptyCell) {
                emptyCell.textContent = query ? 'No results found.' : 'No tasks found.';
            }
            emptyRow.hidden = visibleCount !== 0;
        }

        statusFilter.value = getStatusFromUrl();
        if (globalSearch) {
            globalSearch.value = getSearchFromUrl();
        }
        statusFilter.addEventListener('change', function () {
            navigateWithParams('/tasks', {
                status: statusFilter.value,
                search: globalSearch ? globalSearch.value : ''
            });
        });
        if (globalSearch) {
            globalSearch.addEventListener('input', applyFilters);
            globalSearch.addEventListener('keydown', function (event) {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    navigateWithParams('/tasks', {
                        status: statusFilter.value,
                        search: globalSearch.value
                    });
                }
            });
        }
        clearButton.addEventListener('click', function () {
            navigateWithParams('/tasks', {});
        });
        setupSorting(table, tbody, rows, emptyRow, sortState, applyFilters);
        applyFilters();
    }

    function setupIssueFilters() {
        var table = document.querySelector('[data-testid="issue-table"]');
        if (!table) {
            return;
        }
        var tbody = table.querySelector('tbody');
        var statusFilter = document.querySelector('[data-testid="issue-status-filter"]');
        var priorityFilter = document.querySelector('[data-testid="issue-priority-filter"]');
        var titleSearch = document.querySelector('[data-testid="issue-title-search"]');
        var globalSearch = document.querySelector('[data-testid="global-search-input"]');
        var clearButton = document.querySelector('[data-testid="clear-issue-filters-button"]');
        var emptyRow = document.querySelector('[data-testid="issue-empty-state-row"]');
        var rows = Array.prototype.slice.call(document.querySelectorAll('[data-testid^="issue-row-"]'));
        var emptyCell = emptyRow ? emptyRow.querySelector('td') : null;
        var sortState = {key: null, direction: 'asc'};

        function applyFilters() {
            var status = statusFilter.value;
            var priority = priorityFilter.value;
            var title = normalize(titleSearch.value);
            var globalQuery = normalize(globalSearch ? globalSearch.value : '');
            var visibleCount = 0;
            applySort(tbody, rows, emptyRow, sortState);

            rows.forEach(function (row) {
                var rowStatus = row.getAttribute('data-status');
                var rowPriority = row.getAttribute('data-priority');
                var rowTitle = normalize(row.getAttribute('data-title') || '');
                var rowLabels = normalize(row.getAttribute('data-labels') || '');
                var visible = (!status || rowStatus === status)
                    && (!priority || rowPriority === priority)
                    && (!title || rowTitle.indexOf(title) !== -1)
                    && (!globalQuery || rowTitle.indexOf(globalQuery) !== -1 || rowLabels.indexOf(globalQuery) !== -1);
                row.hidden = !visible;
                if (visible) {
                    visibleCount += 1;
                }
            });

            if (emptyCell) {
                emptyCell.textContent = title || globalQuery ? 'No results found.' : 'No issues found.';
            }
            emptyRow.hidden = visibleCount !== 0;
        }

        statusFilter.value = getStatusFromUrl();
        if (globalSearch) {
            globalSearch.value = getSearchFromUrl();
        }
        statusFilter.addEventListener('change', function () {
            navigateWithParams('/issues', {
                status: statusFilter.value,
                priority: priorityFilter.value,
                search: titleSearch.value || (globalSearch ? globalSearch.value : '')
            });
        });
        priorityFilter.addEventListener('change', function () {
            navigateWithParams('/issues', {
                status: statusFilter.value,
                priority: priorityFilter.value,
                search: titleSearch.value || (globalSearch ? globalSearch.value : '')
            });
        });
        titleSearch.addEventListener('input', applyFilters);
        titleSearch.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                navigateWithParams('/issues', {
                    status: statusFilter.value,
                    priority: priorityFilter.value,
                    search: titleSearch.value
                });
            }
        });
        if (globalSearch) {
            globalSearch.addEventListener('input', applyFilters);
            globalSearch.addEventListener('keydown', function (event) {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    navigateWithParams('/issues', {
                        status: statusFilter.value,
                        priority: priorityFilter.value,
                        search: globalSearch.value
                    });
                }
            });
        }

        clearButton.addEventListener('click', function () {
            navigateWithParams('/issues', {});
        });

        setupSorting(table, tbody, rows, emptyRow, sortState, applyFilters);
        applyFilters();
    }

    function setupSorting(table, tbody, rows, emptyRow, sortState, afterSort) {
        table.querySelectorAll('[data-sort-key]').forEach(function (button) {
            button.addEventListener('click', function () {
                var key = button.getAttribute('data-sort-key');
                if (sortState.key === key) {
                    sortState.direction = sortState.direction === 'asc' ? 'desc' : 'asc';
                } else {
                    sortState.key = key;
                    sortState.direction = 'asc';
                }
                updateSortIndicators(table, sortState);
                afterSort();
            });
        });
    }

    function normalize(value) {
        return String(value || '').trim().toLowerCase();
    }

    function setupGlobalSearchRedirect() {
        var globalSearch = document.querySelector('[data-testid="global-search-input"]');
        if (!globalSearch || document.querySelector('[data-testid="task-table"], [data-testid="issue-table"]')) {
            return;
        }

        globalSearch.addEventListener('keydown', function (event) {
            if (event.key !== 'Enter') {
                return;
            }
            event.preventDefault();
            var query = globalSearch.value.trim();
            if (query) {
                window.location.href = '/tasks?search=' + encodeURIComponent(query);
            }
        });
    }

    function applySort(tbody, rows, emptyRow, sortState) {
        if (!sortState.key) {
            return;
        }
        var direction = sortState.direction === 'asc' ? 1 : -1;
        rows.sort(function (left, right) {
            var leftValue = left.getAttribute('data-sort-' + sortState.key) || '';
            var rightValue = right.getAttribute('data-sort-' + sortState.key) || '';
            return leftValue.localeCompare(rightValue, undefined, {numeric: true, sensitivity: 'base'}) * direction;
        });
        rows.forEach(function (row) {
            tbody.insertBefore(row, emptyRow);
        });
    }

    function updateSortIndicators(table, sortState) {
        table.querySelectorAll('[data-sort-indicator]').forEach(function (indicator) {
            var key = indicator.getAttribute('data-sort-indicator');
            indicator.textContent = key === sortState.key ? (sortState.direction === 'asc' ? '↑' : '↓') : '';
        });
    }

    function setupNotifications() {
        var bell = document.querySelector('[data-testid="notification-bell"]');
        var dropdown = document.querySelector('[data-testid="notification-dropdown"]');
        if (!bell || !dropdown) {
            return;
        }

        bell.addEventListener('click', function () {
            var isOpen = !dropdown.hidden;
            dropdown.hidden = isOpen;
            bell.setAttribute('aria-expanded', String(!isOpen));
        });

        document.addEventListener('click', function (event) {
            if (!bell.contains(event.target) && !dropdown.contains(event.target)) {
                dropdown.hidden = true;
                bell.setAttribute('aria-expanded', 'false');
            }
        });
    }

    function setupDashboardRows() {
        document.querySelectorAll('[data-href][data-testid^="recent-"]').forEach(function (row) {
            row.addEventListener('click', function (event) {
                if (event.target.closest('a, button, form')) {
                    return;
                }
                window.location.href = row.getAttribute('data-href');
            });
        });
    }

    function setupDashboardSidebarToggle() {
        var shell = document.querySelector('.dashboard-shell');
        var toggle = document.querySelector('[data-testid="dashboard-sidebar-toggle"]');
        if (!shell || !toggle) {
            return;
        }

        toggle.addEventListener('click', function () {
            var collapsed = shell.classList.toggle('is-sidebar-collapsed');
            toggle.setAttribute('aria-expanded', String(!collapsed));
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        showToastFromAlerts();
        setupDeleteConfirmation();
        setupTaskFilters();
        setupIssueFilters();
        setupGlobalSearchRedirect();
        setupNotifications();
        setupDashboardRows();
        setupDashboardSidebarToggle();
    });
})();
