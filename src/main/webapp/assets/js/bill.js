/* Handles the two counter actions available from the bill/invoice detail page: Mark Paid and Cancel Order. */
(function () {
    function showError(message) {
        var el = document.getElementById('billActionError');
        if (!el) {
            return;
        }
        el.textContent = message;
        el.classList.remove('d-none');
    }

    var markPaidBtn = document.getElementById('markPaidBtn');
    if (markPaidBtn) {
        var markPaidBtnDefaultHtml = markPaidBtn.innerHTML;
        markPaidBtn.addEventListener('click', function () {
            markPaidBtn.disabled = true;
            markPaidBtn.innerHTML = '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Processing...';
            var orderId = markPaidBtn.dataset.orderId;
            var method = document.getElementById('paymentMethod').value;

            fetch(window.APP_CONTEXT_PATH + '/counter/orders/pay', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: new URLSearchParams({orderId: orderId, method: method}).toString()
            }).then(function (res) {
                return res.json().then(function (data) {
                    return {ok: res.ok, data: data};
                });
            }).then(function (result) {
                if (!result.ok) {
                    showError(result.data.error || 'Could not mark this order paid.');
                    markPaidBtn.disabled = false;
                    markPaidBtn.innerHTML = markPaidBtnDefaultHtml;
                    return;
                }
                window.location.reload();
            }).catch(function () {
                showError('Network error - please try again.');
                markPaidBtn.disabled = false;
                markPaidBtn.innerHTML = markPaidBtnDefaultHtml;
            });
        });
    }

    var cancelBtn = document.getElementById('cancelBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', function () {
            if (!window.confirm('Cancel this order? This cannot be undone.')) {
                return;
            }
            cancelBtn.disabled = true;
            var orderId = cancelBtn.dataset.orderId;

            fetch(window.APP_CONTEXT_PATH + '/counter/orders/cancel', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: new URLSearchParams({orderId: orderId}).toString()
            }).then(function (res) {
                return res.json().then(function (data) {
                    return {ok: res.ok, data: data};
                });
            }).then(function (result) {
                if (!result.ok) {
                    showError(result.data.error || 'Could not cancel this order.');
                    cancelBtn.disabled = false;
                    return;
                }
                window.location.reload();
            }).catch(function () {
                showError('Network error - please try again.');
                cancelBtn.disabled = false;
            });
        });
    }
})();
