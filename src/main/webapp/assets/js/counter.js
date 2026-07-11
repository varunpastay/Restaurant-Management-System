/* Polls /counter/orders for the billing queue (orders served but not yet
   paid), chimes on a newly-arrived order, and links each card to the bill
   detail page where the actual payment/cancel/print actions happen. */
(function () {
    var grid = document.getElementById('ordersGrid');
    var emptyMsg = document.getElementById('emptyQueueMessage');
    var knownOrderIds = null;

    function escapeHtml(str) {
        var div = document.createElement('div');
        div.textContent = str == null ? '' : str;
        return div.innerHTML;
    }

    function money(value) {
        return window.CURRENCY_SYMBOL + Number(value).toFixed(2);
    }

    function timeAgo(isoString) {
        var then = new Date(isoString);
        var seconds = Math.max(0, Math.floor((Date.now() - then.getTime()) / 1000));
        if (seconds < 60) {
            return seconds + 's ago';
        }
        var minutes = Math.floor(seconds / 60);
        if (minutes < 60) {
            return minutes + 'm ago';
        }
        var hours = Math.floor(minutes / 60);
        return hours + 'h ' + (minutes % 60) + 'm ago';
    }

    function orderCard(order, index) {
        var billUrl = window.APP_CONTEXT_PATH + '/counter/bill?orderId=' + order.orderId;
        var itemLabel = order.itemCount + (order.itemCount === 1 ? ' item' : ' items');
        return '<div class="col-sm-6 col-lg-4 col-xl-3">'
            + '<a href="' + billUrl + '" class="text-decoration-none">'
            + '<div class="card h-100 counter-order-card status-served animate-in" style="animation-delay:' + ((index % 8) * 0.04) + 's">'
            + '<div class="card-body">'
            + '<div class="d-flex justify-content-between align-items-start">'
            + '<div><strong class="fs-6">' + escapeHtml(order.orderNo) + '</strong><div class="small text-muted-brand"><i class="bi bi-grid-3x3-gap"></i> Table '
            + escapeHtml(order.tableNo) + '</div></div>'
            + '<span class="status-chip status-served">Served</span>'
            + '</div>'
            + '<div class="mt-2 text-muted-brand small">' + itemLabel + '</div>'
            + '<div class="fs-5 fw-bold mt-1 brand-text">' + money(order.grandTotal) + '</div>'
            + '<div class="small text-muted-brand mt-2"><i class="bi bi-clock-history"></i> ' + timeAgo(order.createdAt) + '</div>'
            + '</div></div></a></div>';
    }

    function render(orders) {
        emptyMsg.classList.toggle('d-none', orders.length > 0);
        grid.innerHTML = orders.map(orderCard).join('');
    }

    function setLiveIndicator(isLive) {
        var indicator = document.getElementById('liveIndicator');
        if (!indicator) {
            return;
        }
        if (isLive) {
            indicator.className = 'badge bg-success';
            indicator.innerHTML = '<i class="bi bi-broadcast"></i> Live';
        } else {
            indicator.className = 'badge bg-danger';
            indicator.innerHTML = '<i class="bi bi-broadcast-pin"></i> Offline';
        }
    }

    function poll() {
        fetch(window.APP_CONTEXT_PATH + '/counter/orders')
            .then(function (res) {
                return res.json();
            })
            .then(function (data) {
                var orders = data.orders || [];
                var currentIds = orders.map(function (o) {
                    return o.orderId;
                });
                if (knownOrderIds !== null) {
                    var hasNewOrder = currentIds.some(function (id) {
                        return knownOrderIds.indexOf(id) === -1;
                    });
                    if (hasNewOrder && window.playNotificationSound) {
                        window.playNotificationSound();
                    }
                }
                knownOrderIds = currentIds;
                render(orders);
                setLiveIndicator(true);
            })
            .catch(function () {
                setLiveIndicator(false);
            });
    }

    var lookupForm = document.getElementById('lookupForm');
    if (lookupForm) {
        lookupForm.addEventListener('submit', function () {
            var value = document.getElementById('lookupOrderNo').value.trim();
            if (value) {
                window.location.href = window.APP_CONTEXT_PATH + '/counter/bill?orderNo=' + encodeURIComponent(value);
            }
        });
    }

    poll();
    setInterval(poll, window.REFRESH_INTERVAL_MS || 5000);
})();
