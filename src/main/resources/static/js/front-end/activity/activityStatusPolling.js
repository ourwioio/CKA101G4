(function () {
  'use strict';

  var endpoint = '/activity/front/status';
  var polling = false;

  function trackedElements() {
    return Array.from(document.querySelectorAll('[data-live-activity-id]'));
  }

  function hasChanged(element, current) {
    if (!current) {
      return true;
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
      return String(element.getAttribute(attribute)) !== String(current[responseKey]);
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
