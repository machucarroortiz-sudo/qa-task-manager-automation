(function () {
    function i18n(key, fallback) {
        var body = document.body;
        if (!body) {
            return fallback;
        }
        return body.getAttribute('data-i18n-' + key) || fallback;
    }

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
                message.textContent = button.getAttribute('data-confirm-message') || i18n('delete-item', 'Delete this item?');
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
                emptyCell.textContent = query ? i18n('no-results', 'No results found.') : i18n('empty-tasks', 'No tasks found.');
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
                emptyCell.textContent = title || globalQuery ? i18n('no-results', 'No results found.') : i18n('empty-issues', 'No issues found.');
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
            indicator.textContent = key === sortState.key ? (sortState.direction === 'asc' ? '^' : 'v') : '';
        });
    }

    var contentTranslationsToSpanish = {
        'Review automation strategy': 'Revisar la estrategia de automatizacion',
        'Create UI smoke tests': 'Crear pruebas de humo de interfaz',
        'Prepare negative API cases': 'Preparar casos negativos de API',
        'Update regression checklist': 'Actualizar la lista de regresion',
        'Validate permission matrix': 'Validar la matriz de permisos',
        'User cannot edit another user\'s issue': 'El usuario no puede editar la incidencia de otro usuario',
        'RBAC negative case for issue modification via UI and API.': 'Caso negativo de RBAC para modificar incidencias desde la interfaz y la API.',
        'Upload validation for issue comments': 'Validacion de subida para comentarios de incidencias',
        'Verify that PNG, JPG and MP4 files are accepted while unsupported files are rejected.': 'Verificar que los archivos PNG, JPG y MP4 se aceptan mientras los archivos no soportados se rechazan.',
        'Admin reviews issue workflow': 'El administrador revisa el flujo de incidencias',
        'Admin-visible issue used for dashboard and label management smoke checks.': 'Incidencia visible para administracion usada en comprobaciones de humo del panel y la gestion de etiquetas.',
        'Seed comment for permission regression testing.': 'Comentario semilla para pruebas de regresion de permisos.',
        'Seed comment for attachment upload testing.': 'Comentario semilla para pruebas de subida de adjuntos.',
        'Initial RBAC reproduction note.': 'Nota inicial de reproduccion de RBAC.',
        'Attachment scenarios are ready for manual API checks.': 'Los escenarios de adjuntos estan preparados para comprobaciones manuales de API.',
        'Waiting for workflow clarification.': 'Pendiente de aclaracion del flujo de trabajo.',
        'Final demo issue comment for deterministic count checks.': 'Comentario final de incidencia demo para comprobaciones deterministas de recuento.'
    };

    var contentGlossaryToSpanish = [
        ['Demo task', 'Tarea demo'],
        ['Demo issue', 'Incidencia demo'],
        ['Verify', 'Verificar'],
        ['Review', 'Revisar'],
        ['Create', 'Crear'],
        ['Complete', 'Completar'],
        ['Update', 'Actualizar'],
        ['Validate', 'Validar'],
        ['Prepare', 'Preparar'],
        ['Confirm', 'Confirmar'],
        ['Check', 'Comprobar'],
        ['Run', 'Ejecutar'],
        ['Search', 'Buscar'],
        ['Issues', 'Incidencias'],
        ['issues', 'incidencias'],
        ['Issue', 'Incidencia'],
        ['issue', 'incidencia'],
        ['Tasks', 'Tareas'],
        ['tasks', 'tareas'],
        ['Task', 'Tarea'],
        ['task', 'tarea'],
        ['Dashboard', 'Panel'],
        ['dashboard', 'panel'],
        ['Workflow', 'Flujo de trabajo'],
        ['workflow', 'flujo de trabajo'],
        ['permissions', 'permisos'],
        ['permission', 'permiso'],
        ['attachments', 'adjuntos'],
        ['attachment', 'adjunto'],
        ['upload', 'subida'],
        ['validation', 'validacion'],
        ['regression', 'regresion'],
        ['security', 'seguridad'],
        ['labels', 'etiquetas'],
        ['label', 'etiqueta'],
        ['sorting', 'ordenacion'],
        ['filtering', 'filtrado'],
        ['filter', 'filtro'],
        ['manual', 'manual'],
        ['API', 'API'],
        ['UI', 'interfaz'],
        ['data', 'datos'],
        ['negative', 'negativo'],
        ['positive', 'positivo'],
        ['boundary', 'limite'],
        ['default', 'por defecto'],
        ['user', 'usuario'],
        ['admin', 'administrador'],
        ['owner', 'propietario'],
        ['priority', 'prioridad'],
        ['status', 'estado'],
        ['date', 'fecha'],
        ['title', 'titulo'],
        ['description', 'descripcion']
    ];

    function setupContentTranslation() {
        var toggle = document.querySelector('[data-content-translation-toggle]');
        var targets = Array.prototype.slice.call(document.querySelectorAll('[data-content-translatable="true"]'));
        if (!toggle || !targets.length) {
            return;
        }
        var translated = false;
        var translateLabel = i18n('content-translate', 'Translate content');
        var originalLabel = i18n('content-original', 'Show original');

        toggle.textContent = translateLabel;
        toggle.setAttribute('aria-pressed', 'false');
        targets.forEach(function (target) {
            target.setAttribute('data-content-original', target.textContent || '');
        });

        toggle.addEventListener('click', function () {
            translated = !translated;
            targets.forEach(function (target) {
                var original = target.getAttribute('data-content-original') || '';
                target.textContent = translated ? translateUserContent(original) : original;
            });
            toggle.textContent = translated ? originalLabel : translateLabel;
            toggle.setAttribute('aria-pressed', String(translated));
        });
    }

    function translateUserContent(text) {
        var lang = (document.documentElement.getAttribute('lang') || 'en').toLowerCase();
        if (lang.indexOf('es') === 0) {
            return translateWithGlossary(text, contentTranslationsToSpanish, contentGlossaryToSpanish);
        }
        return translateWithGlossary(text, reverseDictionary(contentTranslationsToSpanish), reverseGlossary(contentGlossaryToSpanish));
    }

    function translateWithGlossary(text, dictionary, glossary) {
        var value = String(text || '');
        if (!value.trim()) {
            return value;
        }
        if (dictionary[value]) {
            return dictionary[value];
        }
        var translated = value;
        Object.keys(dictionary)
            .sort(function (left, right) {
                return right.length - left.length;
            })
            .forEach(function (source) {
                translated = translated.replace(new RegExp(escapeRegExp(source), 'g'), dictionary[source]);
            });
        glossary.forEach(function (entry) {
            translated = translated.replace(new RegExp('\\b' + escapeRegExp(entry[0]) + '\\b', 'g'), entry[1]);
        });
        return translated;
    }

    function reverseDictionary(dictionary) {
        var reversed = {};
        Object.keys(dictionary).forEach(function (key) {
            reversed[dictionary[key]] = key;
        });
        return reversed;
    }

    function reverseGlossary(glossary) {
        return glossary.map(function (entry) {
            return [entry[1], entry[0]];
        });
    }

    function escapeRegExp(value) {
        return String(value).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
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
        setupContentTranslation();
    });
})();
