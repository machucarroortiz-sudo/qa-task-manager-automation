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
        var modal = document.querySelector('[data-testid="delete-confirmation-modal"]');
        if (!modal) {
            return;
        }
        var pendingForm = null;
        var message = modal.querySelector('[data-testid="delete-confirmation-message"]');
        var cancelButton = modal.querySelector('[data-testid="delete-cancel-button"]');
        var confirmButton = modal.querySelector('[data-testid="delete-confirm-button"]');

        document.querySelectorAll('[data-confirm-delete="true"]').forEach(function (button) {
            button.addEventListener('click', function (event) {
                event.preventDefault();
                pendingForm = button.closest('form');
                message.textContent = button.getAttribute('data-confirm-message') || 'Delete this item?';
                modal.classList.add('is-visible');
            });
        });

        cancelButton.addEventListener('click', function () {
            pendingForm = null;
            modal.classList.remove('is-visible');
        });

        confirmButton.addEventListener('click', function () {
            if (pendingForm) {
                pendingForm.submit();
            }
        });
    }

    function getStatusFromUrl() {
        return new URLSearchParams(window.location.search).get('status') || '';
    }

    function setupTaskFilters() {
        var table = document.querySelector('[data-testid="task-table"]');
        if (!table) {
            return;
        }
        var tbody = table.querySelector('tbody');
        var statusFilter = document.querySelector('[data-testid="task-status-filter"]');
        var clearButton = document.querySelector('[data-testid="clear-task-filters-button"]');
        var emptyRow = document.querySelector('[data-testid="task-empty-state-row"]');
        var rows = Array.prototype.slice.call(document.querySelectorAll('[data-testid^="task-row-"]'));
        var sortState = {key: null, direction: 'asc'};

        function applyFilters() {
            var status = statusFilter.value;
            var visibleCount = 0;
            applySort(tbody, rows, emptyRow, sortState);

            rows.forEach(function (row) {
                var visible = !status || row.getAttribute('data-status') === status;
                row.hidden = !visible;
                if (visible) {
                    visibleCount += 1;
                }
            });

            emptyRow.hidden = visibleCount !== 0;
        }

        statusFilter.value = getStatusFromUrl();
        statusFilter.addEventListener('change', applyFilters);
        clearButton.addEventListener('click', function () {
            statusFilter.value = '';
            applyFilters();
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
        var clearButton = document.querySelector('[data-testid="clear-issue-filters-button"]');
        var emptyRow = document.querySelector('[data-testid="issue-empty-state-row"]');
        var rows = Array.prototype.slice.call(document.querySelectorAll('[data-testid^="issue-row-"]'));
        var sortState = {key: null, direction: 'asc'};

        function applyFilters() {
            var status = statusFilter.value;
            var priority = priorityFilter.value;
            var title = titleSearch.value.trim().toLowerCase();
            var visibleCount = 0;
            applySort(tbody, rows, emptyRow, sortState);

            rows.forEach(function (row) {
                var rowStatus = row.getAttribute('data-status');
                var rowPriority = row.getAttribute('data-priority');
                var rowTitle = row.getAttribute('data-title').toLowerCase();
                var visible = (!status || rowStatus === status)
                    && (!priority || rowPriority === priority)
                    && (!title || rowTitle.indexOf(title) !== -1);
                row.hidden = !visible;
                if (visible) {
                    visibleCount += 1;
                }
            });

            emptyRow.hidden = visibleCount !== 0;
        }

        statusFilter.value = getStatusFromUrl();
        [statusFilter, priorityFilter, titleSearch].forEach(function (control) {
            control.addEventListener('input', applyFilters);
            control.addEventListener('change', applyFilters);
        });

        clearButton.addEventListener('click', function () {
            statusFilter.value = '';
            priorityFilter.value = '';
            titleSearch.value = '';
            applyFilters();
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

    document.addEventListener('DOMContentLoaded', function () {
        showToastFromAlerts();
        setupDeleteConfirmation();
        setupTaskFilters();
        setupIssueFilters();
    });
})();
