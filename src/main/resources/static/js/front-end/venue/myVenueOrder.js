function openReviewModal(btn) {
    var orderId = btn.getAttribute('data-order-id');
    var rating = btn.getAttribute('data-rating');
    var comment = btn.getAttribute('data-comment') || '';

    document.getElementById('reviewOrderId').value = orderId;
    document.getElementById('reviewComment').value = (comment === 'null') ? '' : comment;

    document.querySelectorAll('input[name="venueRating"]').forEach(function (el) {
        el.checked = false;
    });

    if (rating && rating !== 'null') {
        var starEl = document.getElementById('star' + rating);
        if (starEl) starEl.checked = true;
    }

    document.getElementById('reviewModalOverlay').style.display = 'flex';
}

function closeReviewModal() {
    document.getElementById('reviewModalOverlay').style.display = 'none';
}

document.getElementById('reviewModalOverlay').addEventListener('click', function (e) {
    if (e.target === this) closeReviewModal();
});
