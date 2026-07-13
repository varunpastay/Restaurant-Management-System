<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en" data-bs-theme="${restaurant.darkModeDefault ? 'dark' : 'light'}">
<head>
    <title>${fn:escapeXml(restaurant.name)} - Menu</title>
    <%@ include file="/WEB-INF/jsp/common/_assets.jspf" %>
</head>
<body class="pb-5">

<header class="restaurant-header py-2">
    <div class="container d-flex align-items-center justify-content-between">
        <div class="d-flex align-items-center gap-2">
            <img src="${pageContext.request.contextPath}${not empty restaurant.logoPath ? restaurant.logoPath : '/uploads/none.png'}"
                 class="restaurant-logo" alt="${fn:escapeXml(restaurant.name)}">
            <div>
                <h6 class="mb-0 fw-bold">${fn:escapeXml(restaurant.name)}</h6>
                <small class="text-muted-brand">
                    <span class="status-dot ${restaurant.open ? 'open' : 'closed'}"></span>
                    ${restaurant.open ? 'Open now' : 'Currently closed'} &middot; Table ${fn:escapeXml(table.tableNo)}
                </small>
            </div>
        </div>
        <div class="d-flex align-items-center gap-2">
            <c:if test="${not empty lastOrderNo}">
                <a href="${pageContext.request.contextPath}/order/track?orderNo=${lastOrderNo}"
                   class="btn btn-outline-brand btn-sm">
                    <i class="bi bi-receipt"></i><span class="d-none d-sm-inline"> My Order</span>
                </a>
            </c:if>
            <button id="darkModeToggle" type="button" class="btn btn-outline-brand btn-sm" aria-label="Toggle dark mode">
                <i class="bi bi-moon-stars"></i>
            </button>
        </div>
    </div>
</header>

<c:if test="${not empty restaurant.bannerPath}">
    <div class="menu-hero d-none d-sm-block" style="background-image:url('${pageContext.request.contextPath}${restaurant.bannerPath}'); height:130px;">
        <div class="menu-hero-content h-100 d-flex flex-column justify-content-center container">
            <h4 class="mb-0 fw-bold">${fn:escapeXml(restaurant.name)}</h4>
            <small class="opacity-75">Fresh, made-to-order &middot; Scan, browse, order - no app needed</small>
        </div>
    </div>
</c:if>

<c:if test="${!restaurant.open}">
    <div class="alert alert-warning rounded-0 text-center mb-0 py-2">
        <i class="bi bi-clock-history"></i> We're currently closed. Browse the menu, but ordering is temporarily unavailable.
    </div>
</c:if>

<div class="container py-3">
    <div class="mb-3 search-wrap">
        <i class="bi bi-search"></i>
        <input type="search" id="menuSearch" class="form-control form-control-lg" placeholder="Search for a dish...">
    </div>

    <nav class="category-nav mb-3">
        <a href="#" class="nav-link active" data-filter="all">All</a>
        <c:forEach items="${menu}" var="entry">
            <a href="#cat-${entry.key.categoryId}" class="nav-link" data-filter="${entry.key.categoryId}">${fn:escapeXml(entry.key.name)}</a>
        </c:forEach>
    </nav>

    <div id="noResults" class="text-center py-5 d-none">
        <i class="bi bi-search empty-state-icon"></i>
        <p class="mt-2 text-muted-brand">No dishes match your search.</p>
    </div>

    <c:if test="${empty menu}">
        <div class="text-center py-5">
            <i class="bi bi-egg-fried empty-state-icon"></i>
            <p class="mt-2 text-muted-brand">The menu isn't available right now. Please check back soon.</p>
        </div>
    </c:if>

    <c:forEach items="${menu}" var="entry" varStatus="catStatus">
        <section id="cat-${entry.key.categoryId}" class="menu-category mb-4">
            <h5 class="fw-bold brand-text mb-3">${fn:escapeXml(entry.key.name)}</h5>
            <div class="row g-3">
                <c:forEach items="${entry.value}" var="item" varStatus="itemStatus">
                    <div class="col-6 col-md-4 col-lg-3 food-item-col"
                         data-name="${fn:escapeXml(item.name)}" data-category-id="${entry.key.categoryId}">
                        <div class="card food-card h-100 animate-in" data-food-item-id="${item.foodItemId}"
                             style="animation-delay:${(itemStatus.index % 8) * 0.04}s">
                            <div class="food-card-media">
                                <img src="${pageContext.request.contextPath}${not empty item.primaryImagePath ? item.primaryImagePath : '/uploads/none.jpg'}"
                                     class="card-img-top" alt="${fn:escapeXml(item.name)}" loading="lazy">
                                <div class="food-card-badges">
                                    <c:if test="${item.bestseller}">
                                        <span class="badge badge-bestseller text-white"><i class="bi bi-star-fill"></i> Bestseller</span>
                                    </c:if>
                                    <c:if test="${item.recommended}">
                                        <span class="badge badge-recommended text-white"><i class="bi bi-hand-thumbs-up-fill"></i> Recommended</span>
                                    </c:if>
                                </div>
                                <div class="food-card-veg-mark">
                                    <span class="${item.foodType == 'VEG' ? 'veg-dot' : item.foodType == 'EGG' ? 'egg-dot' : 'nonveg-dot'}"
                                          title="${item.foodType}"></span>
                                </div>
                            </div>
                            <div class="card-body d-flex flex-column">
                                <h6 class="card-title mb-1">${fn:escapeXml(item.name)}</h6>
                                <p class="card-text text-muted-brand flex-grow-1">${fn:escapeXml(item.description)}</p>
                                <div class="mb-2">
                                    <c:forEach begin="1"
                                               end="${item.spiceLevel == 'MILD' ? 1 : item.spiceLevel == 'MEDIUM' ? 2 : item.spiceLevel == 'HOT' ? 3 : 4}">
                                        <i class="bi bi-fire text-danger small"></i>
                                    </c:forEach>
                                    <small class="text-muted-brand"><i class="bi bi-clock"></i> ${item.prepTimeMinutes} min</small>
                                </div>
                                <div class="food-card-footer">
                                    <div class="food-card-price">
                                        <c:if test="${item.offerPrice != null}">
                                            <div class="price-strike">${restaurant.currencySymbol}${item.price}</div>
                                        </c:if>
                                        <strong class="fs-6">${restaurant.currencySymbol}${item.effectivePrice}</strong>
                                    </div>
                                    <div class="add-control" data-food-item-id="${item.foodItemId}">
                                        <button type="button" class="btn btn-brand btn-sm add-btn" ${!restaurant.open ? 'disabled' : ''}>
                                            <i class="bi bi-plus-lg"></i> <span class="add-btn-label">Add</span>
                                        </button>
                                        <div class="qty-stepper d-none btn-group btn-group-sm">
                                            <button type="button" class="btn btn-outline-brand dec-btn">-</button>
                                            <span class="btn qty-display" style="pointer-events:none;">0</span>
                                            <button type="button" class="btn btn-outline-brand inc-btn">+</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </section>
    </c:forEach>
</div>

<div id="cartBar" class="cart-bar p-3 d-none">
    <div class="container d-flex justify-content-between align-items-center">
        <div><i class="bi bi-bag-check-fill"></i> <span id="cartItemCountLabel">0 items</span> &middot; <span id="cartSubtotal">${restaurant.currencySymbol}0.00</span></div>
        <button type="button" class="btn" data-bs-toggle="offcanvas" data-bs-target="#cartDrawer">
            View Cart <i class="bi bi-arrow-right"></i>
        </button>
    </div>
</div>

<div class="offcanvas offcanvas-end" tabindex="-1" id="cartDrawer">
    <div class="offcanvas-header border-bottom">
        <h5 class="offcanvas-title"><i class="bi bi-cart3 brand-text"></i> Your Order &middot; Table ${fn:escapeXml(table.tableNo)}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button>
    </div>
    <div class="offcanvas-body d-flex flex-column">
        <div id="cartItemsList"></div>
        <div id="cartEmptyMessage" class="text-center py-5 flex-grow-1 d-flex flex-column justify-content-center">
            <i class="bi bi-cart-x empty-state-icon"></i>
            <p class="mt-2 text-muted-brand">Your cart is empty</p>
        </div>
        <div class="mt-3">
            <label class="form-label small fw-semibold">Order note (optional)</label>
            <textarea id="customerNote" class="form-control form-control-sm" rows="2" placeholder="e.g. no onions in anything"></textarea>
        </div>
        <div class="mt-2">
            <label class="form-label small fw-semibold">Discount code (optional)</label>
            <input type="text" id="discountCode" class="form-control form-control-sm" placeholder="Enter code">
        </div>
        <div class="d-flex justify-content-between align-items-center mt-3 pt-2 border-top">
            <strong>Subtotal</strong>
            <strong id="drawerSubtotal" class="fs-5 brand-text">${restaurant.currencySymbol}0.00</strong>
        </div>
        <small class="text-muted-brand">Tax and service charge are calculated at checkout.</small>
        <button id="placeOrderBtn" type="button" class="btn btn-brand w-100 mt-3" ${!restaurant.open ? 'disabled' : ''}>
            <i class="bi bi-check2-circle"></i> Place Order
        </button>
        <div id="orderError" class="text-danger small mt-2 d-none"></div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
<script>
    window.APP_CONTEXT_PATH = "${pageContext.request.contextPath}";
    window.CURRENCY_SYMBOL = "${restaurant.currencySymbol}";
    window.RESTAURANT_OPEN = ${restaurant.open};
</script>
<script src="${pageContext.request.contextPath}/assets/js/menu.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cart.js"></script>
</body>
</html>
