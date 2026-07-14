(() => {
    "use strict";

    const POLL_INTERVAL_MS = 3000;
    const FILTER_STORAGE_KEY = "webondBuyerOrderFilter";

    document.addEventListener("DOMContentLoaded", () => {
        const cards = Array.from(
            document.querySelectorAll(".buyer-order-card")
        );

        initializeSummary(cards);
        initializeFilters(cards);
        initializeDetailToggles();
        initializePaymentForms();
        initializeCancelModal();
        initializeCountdowns();
        initializeOrderPolling(cards);
    });

    // =========================================================
    // 摘要
    // =========================================================

    function initializeSummary(cards) {
        setText("buyerCountAll", cards.length);
        setText("buyerCountPendingConfirm", countByStatus(cards, 0));
        setText("buyerCountPendingPayment", countByStatus(cards, 1));
        setText("buyerCountConfirmed", countByStatus(cards, 2));
        setText("buyerCountCompleted", countByStatus(cards, 3));
    }

    function countByStatus(cards, status) {
        return cards.filter(
            card => Number(card.dataset.orderStatus) === status
        ).length;
    }

    function setText(id, value) {
        const element = document.getElementById(id);

        if (element) {
            element.textContent = String(value);
        }
    }

    // =========================================================
    // 篩選
    // =========================================================

    function initializeFilters(cards) {
        const buttons = Array.from(
            document.querySelectorAll(".buyer-filter-button")
        );

        const emptyState =
            document.getElementById("buyerFilterEmptyState");

        if (buttons.length === 0 || cards.length === 0) {
            return;
        }

        const storedFilter =
            localStorage.getItem(FILTER_STORAGE_KEY) || "all";

        const initialButton =
            buttons.find(
                button => button.dataset.filter === storedFilter
            ) || buttons[0];

        applyFilter(
            initialButton.dataset.filter,
            buttons,
            cards,
            emptyState
        );

        buttons.forEach(button => {
            button.addEventListener("click", () => {
                const filter = button.dataset.filter || "all";

                localStorage.setItem(
                    FILTER_STORAGE_KEY,
                    filter
                );

                applyFilter(
                    filter,
                    buttons,
                    cards,
                    emptyState
                );
            });
        });
    }

    function applyFilter(filter, buttons, cards, emptyState) {
        let visibleCount = 0;

        buttons.forEach(button => {
            button.classList.toggle(
                "active",
                button.dataset.filter === filter
            );
        });

        cards.forEach(card => {
            const status = Number(card.dataset.orderStatus);
            const visible = matchesFilter(status, filter);

            card.hidden = !visible;

            if (visible) {
                visibleCount += 1;
            }
        });

        if (emptyState) {
            emptyState.hidden = visibleCount !== 0;
        }
    }

    function matchesFilter(status, filter) {
        switch (filter) {
            case "pending-confirm":
                return status === 0;

            case "pending-payment":
                return status === 1;

            case "confirmed":
                return status === 2;

            case "completed":
                return status === 3;

            case "cancelled":
                return [4, 5, 6, 7].includes(status);

            default:
                return true;
        }
    }

    // =========================================================
    // 詳情展開
    // =========================================================

    function initializeDetailToggles() {
        document
            .querySelectorAll(".buyer-detail-toggle")
            .forEach(button => {
                button.addEventListener("click", () => {
                    const card =
                        button.closest(".buyer-order-card");

                    const details =
                        card?.querySelector(".buyer-order-details");

                    if (!details) {
                        return;
                    }

                    const expanded =
                        button.getAttribute("aria-expanded") === "true";

                    button.setAttribute(
                        "aria-expanded",
                        String(!expanded)
                    );

                    details.hidden = expanded;

                    const label =
                        button.querySelector("span:first-child");

                    if (label) {
                        label.textContent =
                            expanded
                                ? "查看訂單詳情"
                                : "收合訂單詳情";
                    }
                });
            });
    }

    // =========================================================
    // 付款
    // =========================================================

    function initializePaymentForms() {
        document
            .querySelectorAll(".buyer-payment-form")
            .forEach(form => {
                form.addEventListener("submit", event => {
                    const confirmed =
                        window.confirm(
                            "目前是測試付款流程，確定要完成這筆付款嗎？"
                        );

                    if (!confirmed) {
                        event.preventDefault();
                        return;
                    }

                    disableSubmitButton(form);
                });
            });
    }

    // =========================================================
    // 取消訂單 Modal
    // =========================================================

    function initializeCancelModal() {
        const modal =
            document.getElementById("buyerCancelModal");

        const form =
            document.getElementById("buyerCancelForm");

        const serviceName =
            document.getElementById("buyerCancelOrderName");

        const reason =
            document.getElementById("buyerCancelReason");

        if (!modal || !form) {
            return;
        }

        document
            .querySelectorAll(".js-open-cancel-modal")
            .forEach(button => {
                button.addEventListener("click", () => {
                    const actionUrl =
                        button.dataset.actionUrl;

                    if (!actionUrl) {
                        return;
                    }

                    form.action = actionUrl;

                    if (serviceName) {
                        serviceName.textContent =
                            `你即將取消「${
                                button.dataset.serviceName
                                || "這筆服務"
                            }」的訂單。`;
                    }

                    if (reason) {
                        reason.value = "";
                    }

                    openModal(modal);

                    window.setTimeout(
                        () => reason?.focus(),
                        30
                    );
                });
            });

        bindModalClose(modal);

        form.addEventListener("submit", event => {
            const confirmed =
                window.confirm(
                    "確定要取消這筆訂單嗎？退款金額將由後端規則計算。"
                );

            if (!confirmed) {
                event.preventDefault();
                return;
            }

            disableSubmitButton(form);
        });
    }

    function bindModalClose(modal) {
        modal
            .querySelectorAll(
                ".buyer-modal-close, .buyer-modal-cancel"
            )
            .forEach(button => {
                button.addEventListener(
                    "click",
                    () => closeModal(modal)
                );
            });

        modal.addEventListener("click", event => {
            if (event.target === modal) {
                closeModal(modal);
            }
        });

        document.addEventListener("keydown", event => {
            if (
                event.key === "Escape"
                && !modal.hidden
            ) {
                closeModal(modal);
            }
        });
    }

    function openModal(modal) {
        modal.hidden = false;
        document.body.classList.add("buyer-modal-open");
    }

    function closeModal(modal) {
        modal.hidden = true;
        document.body.classList.remove("buyer-modal-open");
    }

    function disableSubmitButton(form) {
        const button =
            form.querySelector("button[type='submit']");

        if (!button) {
            return;
        }

        button.disabled = true;
        button.textContent = "處理中…";
    }

    // =========================================================
    // 倒數
    // =========================================================

    function initializeCountdowns() {
        const countdowns = Array.from(
            document.querySelectorAll(
                ".buyer-countdown[data-expire-at]"
            )
        );

        if (countdowns.length === 0) {
            return;
        }

        const update = () => {
            const now = Date.now();

            countdowns.forEach(element => {
                const expireAt =
                    parseDate(element.dataset.expireAt);

                if (!expireAt) {
                    element.textContent = "--:--";
                    return;
                }

                const remainingMs =
                    expireAt.getTime() - now;

                const box =
                    element.closest(".buyer-countdown-box");

                if (remainingMs <= 0) {
                    element.textContent = "已到期";
                    box?.classList.remove("is-urgent");

                    markCardExpired(element);
                    return;
                }

                element.textContent =
                    formatCountdown(remainingMs);

                box?.classList.toggle(
                    "is-urgent",
                    remainingMs <= 10000
                );
            });
        };

        update();
        window.setInterval(update, 250);
    }

    function parseDate(value) {
        if (!value || !value.trim()) {
            return null;
        }

        const date = new Date(value.trim());

        return Number.isNaN(date.getTime())
            ? null
            : date;
    }

    function formatCountdown(milliseconds) {
        const totalSeconds =
            Math.max(
                0,
                Math.ceil(milliseconds / 1000)
            );

        const hours =
            Math.floor(totalSeconds / 3600);

        const minutes =
            Math.floor((totalSeconds % 3600) / 60);

        const seconds =
            totalSeconds % 60;

        if (hours > 0) {
            return `${
                String(hours).padStart(2, "0")
            }:${
                String(minutes).padStart(2, "0")
            }:${
                String(seconds).padStart(2, "0")
            }`;
        }

        return `${
            String(minutes).padStart(2, "0")
        }:${
            String(seconds).padStart(2, "0")
        }`;
    }

    function markCardExpired(countdownElement) {
        const card =
            countdownElement.closest(".buyer-order-card");

        if (!card || card.classList.contains("is-expired")) {
            return;
        }

        card.classList.add("is-expired");

        const status = Number(card.dataset.orderStatus);

        if (status === 1) {
            card
                .querySelectorAll(
                    ".buyer-payment-form button, " +
                    ".buyer-payment-form select"
                )
                .forEach(control => {
                    control.disabled = true;
                });
        }
    }

    // =========================================================
    // 訂單狀態輪詢
    //
    // 現有 WebSocket 只推播時段狀態，沒有推播訂單狀態。
    // 因此買方頁每 3 秒檢查賣家接受、拒絕與逾期結果。
    // =========================================================

    function initializeOrderPolling(cards) {
        const needsPolling =
            cards.some(card => {
                const status =
                    Number(card.dataset.orderStatus);

                return status === 0 || status === 1;
            });

        if (!needsPolling) {
            return;
        }

        window.setInterval(async () => {
            if (document.visibilityState !== "visible") {
                return;
            }

            if (
                document.body.classList.contains(
                    "buyer-modal-open"
                )
            ) {
                return;
            }

            if (isUserEditing()) {
                return;
            }

            await reloadWhenOrderChanged();

        }, POLL_INTERVAL_MS);
    }

    function isUserEditing() {
        const active = document.activeElement;

        if (!active) {
            return false;
        }

        return active.matches(
            "textarea, input, select"
        );
    }

    async function reloadWhenOrderChanged() {
        try {
            const response = await fetch(
                window.location.href,
                {
                    method: "GET",
                    credentials: "same-origin",
                    cache: "no-store",
                    headers: {
                        "X-Requested-With": "XMLHttpRequest"
                    }
                }
            );

            if (!response.ok) {
                return;
            }

            const html = await response.text();
            const parser = new DOMParser();

            const documentCopy =
                parser.parseFromString(
                    html,
                    "text/html"
                );

            const currentSignature =
                buildOrderSignature(document);

            const nextSignature =
                buildOrderSignature(documentCopy);

            if (currentSignature !== nextSignature) {
                window.location.reload();
            }

        } catch (error) {
            console.debug(
                "暫時無法更新買方訂單狀態：",
                error
            );
        }
    }

    function buildOrderSignature(root) {
        return Array.from(
            root.querySelectorAll(".buyer-order-card")
        )
            .map(card => [
                card.dataset.orderId || "",
                card.dataset.orderStatus || "",
                card.dataset.refundStatus || "",
                card.dataset.payoutStatus || "",
                card.dataset.sellerExpires || "",
                card.dataset.paymentExpires || ""
            ].join(":"))
            .sort()
            .join("|");
    }
})();
