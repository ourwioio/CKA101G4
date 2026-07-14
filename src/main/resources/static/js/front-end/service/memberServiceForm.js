document.addEventListener('DOMContentLoaded', function () {

    /* ==========================================================
       DOM
       ========================================================== */

    const serviceForm =
        document.getElementById('memberServiceForm');

    const serviceTypeSelect =
        document.getElementById('serviceTypeId');

    const serviceNameInput =
        document.getElementById('serviceName');

    const hourlyRateInput =
        document.getElementById('hourlyRate');

    const serviceCitySelect =
        document.getElementById('serviceCity');

    const serviceDistrictSelect =
        document.getElementById('serviceDistrict');

    const serviceLocationInput =
        document.getElementById('serviceLocation');

    const descriptionInput =
        document.getElementById('description');

    const serviceImageInput =
        document.getElementById('serviceImageFile');

    const serviceImageUpload =
        document.getElementById('serviceImageUpload');

    const serviceImagePreview =
        document.getElementById('serviceImagePreview');

    const removeImageButton =
        document.getElementById('removeServiceImage');

    const summaryImage =
        document.getElementById('summaryImage');

    const summaryServiceType =
        document.getElementById('summaryServiceType');

    const summaryServiceName =
        document.getElementById('summaryServiceName');

    const summaryLocation =
        document.getElementById('summaryLocation');

    const summaryPlace =
        document.getElementById('summaryPlace');

    const summaryDescription =
        document.getElementById('summaryDescription');

    const summaryHourlyRate =
        document.getElementById('summaryHourlyRate');

    const serviceNameCount =
        document.getElementById('serviceNameCount');

    const descriptionCount =
        document.getElementById('descriptionCount');

    const submitButton =
        serviceForm
            ? serviceForm.querySelector('button[type="submit"]')
            : null;

    if (!serviceForm) {
        return;
    }

    const defaultImageUrl =
        serviceForm.dataset.defaultImageUrl ||
        '/images/activity/default-activity.jpg';

    const originalImageUrl =
        serviceForm.dataset.originalImageUrl || '';

    let selectedImageObjectUrl = null;

    /* ==========================================================
       臺灣縣市行政區資料
       ========================================================== */

    const districtMap = {

        '臺北市': [
            '中正區', '大同區', '中山區', '松山區',
            '大安區', '萬華區', '信義區', '士林區',
            '北投區', '內湖區', '南港區', '文山區'
        ],

        '新北市': [
            '板橋區', '三重區', '中和區', '永和區',
            '新莊區', '新店區', '樹林區', '鶯歌區',
            '三峽區', '淡水區', '汐止區', '瑞芳區',
            '土城區', '蘆洲區', '五股區', '泰山區',
            '林口區', '深坑區', '石碇區', '坪林區',
            '三芝區', '石門區', '八里區', '平溪區',
            '雙溪區', '貢寮區', '金山區', '萬里區',
            '烏來區'
        ],

        '桃園市': [
            '桃園區', '中壢區', '平鎮區', '八德區',
            '楊梅區', '蘆竹區', '大溪區', '龍潭區',
            '龜山區', '大園區', '觀音區', '新屋區',
            '復興區'
        ],

        '臺中市': [
            '中區', '東區', '南區', '西區',
            '北區', '西屯區', '南屯區', '北屯區',
            '豐原區', '東勢區', '大甲區', '清水區',
            '沙鹿區', '梧棲區', '后里區', '神岡區',
            '潭子區', '大雅區', '新社區', '石岡區',
            '外埔區', '大安區', '烏日區', '大肚區',
            '龍井區', '霧峰區', '太平區', '大里區',
            '和平區'
        ],

        '臺南市': [
            '中西區', '東區', '南區', '北區',
            '安平區', '安南區', '永康區', '歸仁區',
            '新化區', '左鎮區', '玉井區', '楠西區',
            '南化區', '仁德區', '關廟區', '龍崎區',
            '官田區', '麻豆區', '佳里區', '西港區',
            '七股區', '將軍區', '學甲區', '北門區',
            '新營區', '後壁區', '白河區', '東山區',
            '六甲區', '下營區', '柳營區', '鹽水區',
            '善化區', '大內區', '山上區', '新市區',
            '安定區'
        ],

        '高雄市': [
            '楠梓區', '左營區', '鼓山區', '三民區',
            '鹽埕區', '前金區', '新興區', '苓雅區',
            '前鎮區', '旗津區', '小港區', '鳳山區',
            '林園區', '大寮區', '大樹區', '大社區',
            '仁武區', '鳥松區', '岡山區', '橋頭區',
            '燕巢區', '田寮區', '阿蓮區', '路竹區',
            '湖內區', '茄萣區', '永安區', '彌陀區',
            '梓官區', '旗山區', '美濃區', '六龜區',
            '甲仙區', '杉林區', '內門區', '茂林區',
            '桃源區', '那瑪夏區'
        ],

        '基隆市': [
            '仁愛區', '信義區', '中正區', '中山區',
            '安樂區', '暖暖區', '七堵區'
        ],

        '新竹市': [
            '東區', '北區', '香山區'
        ],

        '嘉義市': [
            '東區', '西區'
        ],

        '新竹縣': [
            '竹北市', '竹東鎮', '新埔鎮', '關西鎮',
            '湖口鄉', '新豐鄉', '芎林鄉', '橫山鄉',
            '北埔鄉', '寶山鄉', '峨眉鄉', '尖石鄉',
            '五峰鄉'
        ],

        '苗栗縣': [
            '苗栗市', '苑裡鎮', '通霄鎮', '竹南鎮',
            '頭份市', '後龍鎮', '卓蘭鎮', '大湖鄉',
            '公館鄉', '銅鑼鄉', '南庄鄉', '頭屋鄉',
            '三義鄉', '西湖鄉', '造橋鄉', '三灣鄉',
            '獅潭鄉', '泰安鄉'
        ],

        '彰化縣': [
            '彰化市', '鹿港鎮', '和美鎮', '線西鄉',
            '伸港鄉', '福興鄉', '秀水鄉', '花壇鄉',
            '芬園鄉', '員林市', '溪湖鎮', '田中鎮',
            '大村鄉', '埔鹽鄉', '埔心鄉', '永靖鄉',
            '社頭鄉', '二水鄉', '北斗鎮', '二林鎮',
            '田尾鄉', '埤頭鄉', '芳苑鄉', '大城鄉',
            '竹塘鄉', '溪州鄉'
        ],

        '南投縣': [
            '南投市', '埔里鎮', '草屯鎮', '竹山鎮',
            '集集鎮', '名間鄉', '鹿谷鄉', '中寮鄉',
            '魚池鄉', '國姓鄉', '水里鄉', '信義鄉',
            '仁愛鄉'
        ],

        '雲林縣': [
            '斗六市', '斗南鎮', '虎尾鎮', '西螺鎮',
            '土庫鎮', '北港鎮', '古坑鄉', '大埤鄉',
            '莿桐鄉', '林內鄉', '二崙鄉', '崙背鄉',
            '麥寮鄉', '東勢鄉', '褒忠鄉', '臺西鄉',
            '元長鄉', '四湖鄉', '口湖鄉', '水林鄉'
        ],

        '嘉義縣': [
            '太保市', '朴子市', '布袋鎮', '大林鎮',
            '民雄鄉', '溪口鄉', '新港鄉', '六腳鄉',
            '東石鄉', '義竹鄉', '鹿草鄉', '水上鄉',
            '中埔鄉', '竹崎鄉', '梅山鄉', '番路鄉',
            '大埔鄉', '阿里山鄉'
        ],

        '屏東縣': [
            '屏東市', '潮州鎮', '東港鎮', '恆春鎮',
            '萬丹鄉', '長治鄉', '麟洛鄉', '九如鄉',
            '里港鄉', '鹽埔鄉', '高樹鄉', '萬巒鄉',
            '內埔鄉', '竹田鄉', '新埤鄉', '枋寮鄉',
            '新園鄉', '崁頂鄉', '林邊鄉', '南州鄉',
            '佳冬鄉', '琉球鄉', '車城鄉', '滿州鄉',
            '枋山鄉', '三地門鄉', '霧臺鄉', '瑪家鄉',
            '泰武鄉', '來義鄉', '春日鄉', '獅子鄉',
            '牡丹鄉'
        ],

        '宜蘭縣': [
            '宜蘭市', '羅東鎮', '蘇澳鎮', '頭城鎮',
            '礁溪鄉', '壯圍鄉', '員山鄉', '冬山鄉',
            '五結鄉', '三星鄉', '大同鄉', '南澳鄉'
        ],

        '花蓮縣': [
            '花蓮市', '鳳林鎮', '玉里鎮', '新城鄉',
            '吉安鄉', '壽豐鄉', '光復鄉', '豐濱鄉',
            '瑞穗鄉', '富里鄉', '秀林鄉', '萬榮鄉',
            '卓溪鄉'
        ],

        '臺東縣': [
            '臺東市', '成功鎮', '關山鎮', '卑南鄉',
            '鹿野鄉', '池上鄉', '東河鄉', '長濱鄉',
            '太麻里鄉', '大武鄉', '綠島鄉', '海端鄉',
            '延平鄉', '金峰鄉', '達仁鄉', '蘭嶼鄉'
        ],

        '澎湖縣': [
            '馬公市', '湖西鄉', '白沙鄉',
            '西嶼鄉', '望安鄉', '七美鄉'
        ],

        '金門縣': [
            '金城鎮', '金湖鎮', '金沙鎮',
            '金寧鄉', '烈嶼鄉', '烏坵鄉'
        ],

        '連江縣': [
            '南竿鄉', '北竿鄉', '莒光鄉', '東引鄉'
        ]
    };

    /* ==========================================================
       縣市行政區連動
       ========================================================== */

    function renderDistrictOptions(city, selectedDistrict) {

        if (!serviceDistrictSelect) {
            return;
        }

        serviceDistrictSelect.innerHTML = '';

        const defaultOption =
            document.createElement('option');

        defaultOption.value = '';

        defaultOption.textContent =
            city
                ? '請選擇行政區'
                : '請先選擇縣市';

        serviceDistrictSelect.appendChild(defaultOption);

        const districts =
            districtMap[city] || [];

        districts.forEach(function (district) {

            const option =
                document.createElement('option');

            option.value = district;
            option.textContent = district;

            if (district === selectedDistrict) {
                option.selected = true;
            }

            serviceDistrictSelect.appendChild(option);
        });

        serviceDistrictSelect.disabled =
            districts.length === 0;
    }

    const initialDistrict =
        serviceDistrictSelect
            ? serviceDistrictSelect.dataset.selectedDistrict || ''
            : '';

    renderDistrictOptions(
        serviceCitySelect ? serviceCitySelect.value : '',
        initialDistrict
    );

    if (serviceCitySelect) {

        serviceCitySelect.addEventListener(
            'change',
            function () {

                renderDistrictOptions(
                    serviceCitySelect.value,
                    ''
                );

                updatePreview();
            }
        );
    }

    if (serviceDistrictSelect) {

        serviceDistrictSelect.addEventListener(
            'change',
            updatePreview
        );
    }

    /* ==========================================================
       即時預覽
       ========================================================== */

    function selectedOptionText(selectElement, fallbackText) {

        if (
            !selectElement ||
            !selectElement.value ||
            selectElement.selectedIndex < 0
        ) {
            return fallbackText;
        }

        return selectElement
            .options[selectElement.selectedIndex]
            .textContent
            .trim();
    }

    function updatePreview() {

        const serviceName =
            serviceNameInput
                ? serviceNameInput.value.trim()
                : '';

        const hourlyRate =
            hourlyRateInput
                ? hourlyRateInput.value.trim()
                : '';

        const serviceCity =
            serviceCitySelect
                ? serviceCitySelect.value.trim()
                : '';

        const serviceDistrict =
            serviceDistrictSelect
                ? serviceDistrictSelect.value.trim()
                : '';

        const serviceLocation =
            serviceLocationInput
                ? serviceLocationInput.value.trim()
                : '';

        const description =
            descriptionInput
                ? descriptionInput.value.trim()
                : '';

        const typeName =
            selectedOptionText(
                serviceTypeSelect,
                '服務類型'
            );

        if (summaryServiceType) {
            summaryServiceType.textContent = typeName;
        }

        if (summaryServiceName) {

            summaryServiceName.textContent =
                serviceName || '服務名稱';
        }

        if (summaryHourlyRate) {

            summaryHourlyRate.textContent =
                hourlyRate || '0';
        }

        if (summaryLocation) {

            summaryLocation.textContent =
                (serviceCity || '縣市') +
                ' ' +
                (serviceDistrict || '行政區');
        }

        if (summaryPlace) {

            summaryPlace.textContent =
                serviceLocation || '詳細地點會顯示在這裡';
        }

        if (summaryDescription) {

            summaryDescription.textContent =
                description || '服務描述會顯示在這裡。';
        }

        updateCharacterCount(
            serviceNameInput,
            serviceNameCount,
            100
        );

        updateCharacterCount(
            descriptionInput,
            descriptionCount,
            1000
        );
    }

    function updateCharacterCount(
        inputElement,
        countElement,
        maximum
    ) {

        if (!inputElement || !countElement) {
            return;
        }

        const currentLength =
            inputElement.value.length;

        countElement.textContent =
            currentLength;

        const countWrapper =
            countElement.closest(
                '.member-service-character-count'
            );

        if (countWrapper) {

            countWrapper.classList.toggle(
                'is-limit',
                currentLength >= maximum
            );
        }
    }

    [
        serviceTypeSelect,
        serviceNameInput,
        hourlyRateInput,
        serviceLocationInput,
        descriptionInput
    ].forEach(function (element) {

        if (!element) {
            return;
        }

        element.addEventListener(
            'input',
            updatePreview
        );

        element.addEventListener(
            'change',
            updatePreview
        );
    });

    /* ==========================================================
       圖片預覽
       ========================================================== */

    function setPreviewImage(imageUrl) {

        if (!imageUrl) {

            if (serviceImagePreview) {
                serviceImagePreview.removeAttribute('src');
            }

            if (summaryImage) {
                summaryImage.src = defaultImageUrl;
            }

            if (serviceImageUpload) {

                serviceImageUpload.classList.remove(
                    'has-image'
                );
            }

            return;
        }

        if (serviceImagePreview) {
            serviceImagePreview.src = imageUrl;
        }

        if (summaryImage) {
            summaryImage.src = imageUrl;
        }

        if (serviceImageUpload) {

            serviceImageUpload.classList.add(
                'has-image'
            );
        }
    }

    if (originalImageUrl) {
        setPreviewImage(originalImageUrl);
    }

    if (serviceImageInput) {

        serviceImageInput.addEventListener(
            'change',
            function () {

                const file =
                    serviceImageInput.files[0];

                if (!file) {
                    return;
                }

                const allowedTypes = [
                    'image/jpeg',
                    'image/png',
                    'image/webp'
                ];

                if (!allowedTypes.includes(file.type)) {

                    alert(
                        '只支援 JPG、PNG 或 WEBP 圖片。'
                    );

                    serviceImageInput.value = '';

                    return;
                }

                const maxSize =
                    5 * 1024 * 1024;

                if (file.size > maxSize) {

                    alert(
                        '圖片大小不可超過 5MB。'
                    );

                    serviceImageInput.value = '';

                    return;
                }

                if (selectedImageObjectUrl) {

                    URL.revokeObjectURL(
                        selectedImageObjectUrl
                    );
                }

                selectedImageObjectUrl =
                    URL.createObjectURL(file);

                setPreviewImage(
                    selectedImageObjectUrl
                );
            }
        );
    }

    if (removeImageButton) {

        removeImageButton.addEventListener(
            'click',
            function () {

                if (serviceImageInput) {
                    serviceImageInput.value = '';
                }

                if (selectedImageObjectUrl) {

                    URL.revokeObjectURL(
                        selectedImageObjectUrl
                    );

                    selectedImageObjectUrl = null;
                }

                /*
                 * 編輯模式：
                 * 取消新選圖片後回到原本圖片。
                 *
                 * 新增模式：
                 * 回到預設圖片。
                 */
                if (originalImageUrl) {

                    setPreviewImage(
                        originalImageUrl
                    );

                } else {

                    setPreviewImage('');
                }
            }
        );
    }

    if (summaryImage) {

        summaryImage.addEventListener(
            'error',
            function () {

                summaryImage.onerror = null;
                summaryImage.src = defaultImageUrl;
            }
        );
    }

    if (serviceImagePreview) {

        serviceImagePreview.addEventListener(
            'error',
            function () {

                serviceImagePreview.onerror = null;

                if (serviceImageUpload) {

                    serviceImageUpload.classList.remove(
                        'has-image'
                    );
                }
            }
        );
    }

    /* ==========================================================
       防止重複送出
       ========================================================== */

    serviceForm.addEventListener(
        'submit',
        function () {

            if (!serviceForm.checkValidity()) {
                return;
            }

            if (submitButton) {

                submitButton.disabled = true;
                submitButton.textContent = '處理中...';
            }
        }
    );

    /* ==========================================================
       初始畫面
       ========================================================== */

    updatePreview();

    window.addEventListener(
        'beforeunload',
        function () {

            if (selectedImageObjectUrl) {

                URL.revokeObjectURL(
                    selectedImageObjectUrl
                );
            }
        }
    );
});