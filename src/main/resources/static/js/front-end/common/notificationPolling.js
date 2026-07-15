document.addEventListener("DOMContentLoaded", function () {

    const POLLING_INTERVAL_MS = 3000;

    let pollingTimer = null;


    // =========================================================
    // 尋找通知按鈕
    //
    // 新版、舊版 Navbar 都支援
    // =========================================================

    function findNotificationButton() {

        return document.querySelector(
            "[data-unread-url]"
        );
    }


    // =========================================================
    // 取得未讀數量
    // =========================================================

    async function refreshUnreadNotificationCount() {

        const notificationButton =
            findNotificationButton();

        // 未登入時沒有通知按鈕，直接結束
        if (!notificationButton) {
            return;
        }

        const unreadUrl =
            notificationButton.dataset.unreadUrl;

        if (!unreadUrl) {
            return;
        }

        try {
            const response =
                await fetch(unreadUrl, {
                    method: "GET",

                    headers: {
                        "Accept": "application/json"
                    },

                    // 避免瀏覽器使用舊快取
                    cache: "no-store",

                    // 帶上目前 Session Cookie
                    credentials: "same-origin"
                });

            if (!response.ok) {
                return;
            }

            const responseData =
                await response.json();

            /*
             * 支援後端直接回傳：
             * 3
             *
             * 也支援：
             * { "count": 3 }
             * { "unreadCount": 3 }
             */
            let unreadCount;

            if (typeof responseData === "number") {

                unreadCount =
                    responseData;

            } else if (responseData != null
                    && responseData.unreadCount != null) {

                unreadCount =
                    Number(responseData.unreadCount);

            } else if (responseData != null
                    && responseData.count != null) {

                unreadCount =
                    Number(responseData.count);

            } else {

                unreadCount = 0;
            }

            if (!Number.isFinite(unreadCount)
                    || unreadCount < 0) {

                unreadCount = 0;
            }

            updateNotificationBadge(
                notificationButton,
                unreadCount
            );

        } catch (error) {

            console.error(
                "取得未讀通知數量失敗：",
                error
            );
        }
    }


    // =========================================================
    // 更新導覽列上的通知數字
    // =========================================================

    function updateNotificationBadge(
            notificationButton,
            unreadCount) {

        let badge =
            notificationButton.querySelector(
                ".badge-dot, .icon-badge--unread"
            );

        // 有未讀通知
        if (unreadCount > 0) {

            // 原本沒有 badge，就動態建立
            if (!badge) {

                badge =
                    document.createElement("span");

                /*
                 * 同時加入新版與舊版 class：
                 *
                 * badge-dot：
                 * 新版 navbar.css 外觀
                 *
                 * icon-badge icon-badge--unread：
                 * 相容舊版
                 */
                badge.className =
                    "badge-dot icon-badge icon-badge--unread";

                notificationButton.appendChild(
                    badge
                );
            }

            badge.textContent =
                unreadCount > 99
                    ? "99+"
                    : String(unreadCount);

            return;
        }

        // 沒有未讀通知時移除數字
        if (badge) {
            badge.remove();
        }
    }


    // =========================================================
    // 啟動輪詢
    // =========================================================

    function startNotificationPolling() {

        if (pollingTimer !== null) {
            clearInterval(pollingTimer);
        }

        // 頁面一開啟先立即查一次
        refreshUnreadNotificationCount();

        pollingTimer =
            setInterval(
                refreshUnreadNotificationCount,
                POLLING_INTERVAL_MS
            );
    }


    // =========================================================
    // 切回分頁時立即更新
    // =========================================================

    document.addEventListener(
        "visibilitychange",
        function () {

            if (!document.hidden) {
                refreshUnreadNotificationCount();
            }
        }
    );


    // =========================================================
    // 視窗重新取得焦點時立即更新
    // =========================================================

    window.addEventListener(
        "focus",
        refreshUnreadNotificationCount
    );


    startNotificationPolling();
});