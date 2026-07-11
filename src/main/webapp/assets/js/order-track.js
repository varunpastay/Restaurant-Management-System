/* Polls /order/status for the current order and updates the timeline UI without a full page reload. */
(function () {
    var orderNo = window.ORDER_NO;
    var statusOrder = ['PENDING', 'ACCEPTED', 'PREPARING', 'READY', 'SERVED', 'COMPLETED'];

    function applyStatus(data) {
        var currentIndex = statusOrder.indexOf(data.status);
        document.querySelectorAll('.status-timeline li').forEach(function (li) {
            var stepIndex = statusOrder.indexOf(li.dataset.status);
            li.classList.remove('done', 'current');
            if (data.status === 'CANCELLED') {
                return;
            }
            if (stepIndex < currentIndex) {
                li.classList.add('done');
            }
            if (stepIndex === currentIndex) {
                li.classList.add('current');
            }
        });
        var banner = document.getElementById('statusBanner');
        var icon = document.getElementById('statusIcon');
        if (banner) {
            var cancelled = data.status === 'CANCELLED';
            banner.textContent = cancelled ? 'Cancelled' : data.status;
            banner.style.backgroundColor = cancelled ? '#6c757d' : 'var(--brand)';
        }
        if (icon) {
            icon.className = data.status === 'CANCELLED'
                ? 'bi bi-x-circle-fill text-secondary'
                : data.status === 'SERVED' || data.status === 'COMPLETED'
                    ? 'bi bi-check-circle-fill brand-text'
                    : 'bi bi-hourglass-split brand-text';
            icon.style.fontSize = '3.2rem';
        }
    }

    function poll() {
        fetch(window.APP_CONTEXT_PATH + '/order/status?orderNo=' + encodeURIComponent(orderNo))
            .then(function (res) {
                return res.json();
            })
            .then(function (data) {
                if (!data.error) {
                    applyStatus(data);
                }
            })
            .catch(function () {
                /* transient network hiccup - next poll will retry */
            });
    }

    if (orderNo) {
        poll();
        setInterval(poll, 8000);
    }
})();
