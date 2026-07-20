(function () {
  'use strict';

  var endpoint = '/activity/front/status';
  var polling = false;

  function trackedElements() {
    return Array.from(document.querySelectorAll('[data-live-activity-id]'));
  }

  function normalizeValue(value) {
    return value == null ? '' : String(value);
  }

  function hasChanged(element, current) {
    // API 暫時缺少某筆資料時不刷新，避免空回應造成每秒重新載入。
    // 活動下架／取消仍會由 API 回傳新的 activityStatus，並走下方正常比較。
    if (!current) {
      return false;
    }

    var comparisons = [
      ['data-live-activity-status', 'activityStatus'],
      ['data-live-attendees-count', 'attendeesCount'],
      ['data-live-registration-status', 'registrationStatus'],
      ['data-live-ended', 'ended']
    ];

    return comparisons.some(function (comparison) {
      var attribute = comparison[0];
      var responseKey = comparison[1];
      if (!element.hasAttribute(attribute)) {
        return false;
      }
      return normalizeValue(element.getAttribute(attribute)) !== normalizeValue(current[responseKey]);
    });
  }

  function pollActivityStatuses() {
    if (polling || document.hidden) {
      return;
    }

    var elements = trackedElements();
    if (elements.length === 0) {
      return;
    }

    var activityIds = Array.from(new Set(elements.map(function (element) {
      return element.getAttribute('data-live-activity-id');
    }).filter(Boolean)));

    if (activityIds.length === 0) {
      return;
    }

    polling = true;
    fetch(endpoint + '?activityIds=' + encodeURIComponent(activityIds.join(',')) + '&_=' + Date.now(), {
      cache: 'no-store',
      headers: { 'Accept': 'application/json' }
    })
      .then(function (response) {
        return response.ok ? response.json() : {};
      })
      .then(function (data) {
        var changed = elements.some(function (element) {
          return hasChanged(element, data[element.getAttribute('data-live-activity-id')]);
        });
        if (changed) {
          window.location.reload();
        }
      })
      .catch(function () {})
      .finally(function () {
        polling = false;
      });
  }

  pollActivityStatuses();
  window.setInterval(pollActivityStatuses, 1000);
})();
