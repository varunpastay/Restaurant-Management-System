/* Polls /kitchen/orders on an interval, diff-updates the order queue (so
   unchanged cards are never torn down - no flicker, no animation replay,
   hover/focus state survives), plays a chime when a previously-unseen order
   appears, and posts one-step status advances. No page reloads. */
(function () {
    var grid = document.getElementById('ordersGrid');
    var emptyMsg = document.getElementById('emptyQueueMessage');
    var knownOrderIds = null; // null until the first successful poll, so we never chime on initial load

    var STATUS_LABELS = {PENDING: 'New', ACCEPTED: 'Accepted', PREPARING: 'Preparing', READY: 'Ready'};
    var NEXT_STATUS = {PENDING: 'ACCEPTED', ACCEPTED: 'PREPARING', PREPARING: 'READY', READY: 'SERVED'};
    var NEXT_ACTION_LABEL = {
        PENDING: '<i class="bi bi-check2"></i> Accept Order',
        ACCEPTED: '<i class="bi bi-fire"></i> Start Preparing',
        PREPARING: '<i class="bi bi-bell"></i> Mark Ready',
        READY: '<i class="bi bi-box-seam"></i> Mark Served'
    };

    function escapeHtml(str) {
        var div = document.createElement('div');
        div.textContent = str == null ? '' : str;
        return div.innerHTML;
    }

    function isStale(isoString) {
        return (Date.now() - new Date(isoString).getTime()) / 1000 > 600;
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

    function orderCard(order, isNew) {
        var statusClass = 'status-' + order.status.toLowerCase();
        var itemsHtml = order.items.map(function (item) {
            var note = item.specialInstructions
                ? '<div class="small text-muted-brand ms-4 ps-1">&#8618; ' + escapeHtml(item.specialInstructions) + '</div>'
                : '';
            return '<li class="d-flex align-items-center mb-1"><span class="order-item-qty">' + item.quantity + '</span>'
                + '<span>' + escapeHtml(item.name) + '</span></li>' + note;
        }).join('');

        var orderNoteHtml = order.customerNote
            ? '<div class="small text-muted-brand mt-1 pt-2 border-top"><i class="bi bi-sticky"></i> ' + escapeHtml(order.customerNote) + '</div>'
            : '';

        var actionLabel = NEXT_ACTION_LABEL[order.status] || '';
        var actionBtn = actionLabel
            ? '<button type="button" class="btn btn-brand w-100 mt-2 advance-btn" data-order-id="' + order.orderId
              + '" data-status="' + order.status + '">' + actionLabel + '</button>'
            : '';

        return '<div class="col-sm-6 col-lg-4 col-xl-3" data-order-card="' + order.orderId + '">'
            + '<div class="card h-100 kitchen-order-card ' + statusClass + (isNew ? ' is-new' : '') + '">'
            + '<div class="card-body d-flex flex-column">'
            + '<div class="d-flex justify-content-between align-items-start mb-1">'
            + '<div><strong class="fs-6">' + escapeHtml(order.orderNo) + '</strong><div class="small text-muted-brand"><i class="bi bi-grid-3x3-gap"></i> Table '
            + escapeHtml(order.tableNo) + '</div></div>'
            + '<span class="status-chip ' + statusClass + '">' + (STATUS_LABELS[order.status] || order.status) + '</span>'
            + '</div>'
            + '<ul class="list-unstyled small my-2 flex-grow-1">' + itemsHtml + '</ul>'
            + orderNoteHtml
            + '<div class="small text-muted-brand mt-2"><i class="bi bi-clock-history"></i> <span class="order-age'
            + (isStale(order.createdAt) ? ' stale' : '') + '" data-created-at="'
            + order.createdAt + '">' + timeAgo(order.createdAt) + '</span></div>'
            + actionBtn
            + '</div></div></div>';
    }

    function cardSignature(order) {
        return order.status + '|' + order.items.length + '|' + (order.customerNote || '') + '|'
            + order.items.map(function (i) { return i.quantity + ':' + i.specialInstructions; }).join(',');
    }

    function render(orders) {
        emptyMsg.classList.toggle('d-none', orders.length > 0);
        var seenIds = {};
        var previousSibling = null;

        orders.forEach(function (order) {
            var id = String(order.orderId);
            seenIds[id] = true;
            var signature = cardSignature(order);
            var existing = grid.querySelector('[data-order-card="' + id + '"]');
            var el;

            if (existing && existing.dataset.signature === signature) {
                el = existing;
            } else {
                var isNew = knownOrderIds !== null && knownOrderIds.indexOf(order.orderId) === -1;
                var wrapper = document.createElement('div');
                wrapper.innerHTML = orderCard(order, isNew && !existing);
                el = wrapper.firstElementChild;
                el.dataset.signature = signature;
                if (existing) {
                    existing.replaceWith(el);
                } else {
                    grid.appendChild(el);
                }
            }

            var desiredAfter = previousSibling ? previousSibling.nextElementSibling : grid.firstElementChild;
            if (desiredAfter !== el) {
                grid.insertBefore(el, desiredAfter);
            }
            previousSibling = el;
        });

        Array.prototype.slice.call(grid.children).forEach(function (card) {
            if (!seenIds[card.dataset.orderCard]) {
                card.remove();
            }
        });
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
        fetch(window.APP_CONTEXT_PATH + '/kitchen/orders')
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

    document.addEventListener('click', function (e) {
        var btn = e.target.closest('.advance-btn');
        if (!btn) {
            return;
        }
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Updating...';
        var orderId = btn.dataset.orderId;
        var nextStatus = NEXT_STATUS[btn.dataset.status];

        fetch(window.APP_CONTEXT_PATH + '/kitchen/orders/status', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: new URLSearchParams({orderId: orderId, status: nextStatus}).toString()
        }).then(poll);
    });

    setInterval(function () {
        document.querySelectorAll('.order-age').forEach(function (el) {
            var seconds = Math.floor((Date.now() - new Date(el.dataset.createdAt).getTime()) / 1000);
            el.textContent = timeAgo(el.dataset.createdAt);
            el.classList.toggle('stale', seconds > 600);
        });
    }, 15000);

    poll();
    setInterval(poll, window.REFRESH_INTERVAL_MS || 5000);
})();
