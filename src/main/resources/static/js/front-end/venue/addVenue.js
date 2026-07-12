const addressData = {
    "基隆市": ["仁愛區","信義區","中正區","中山區","安樂區","暖暖區","七堵區"],
    "臺北市": ["中正區","大同區","中山區","松山區","大安區","萬華區","信義區","士林區","北投區","內湖區","南港區","文山區"],
    "新北市": ["板橋區","新莊區","中和區","永和區","土城區","樹林區","三峽區","鶯歌區","三重區","蘆洲區","五股區","泰山區","林口區","八里區","淡水區","三芝區","石門區","金山區","萬里區","汐止區","平溪區","瑞芳區","雙溪區","貢寮區","新店區","深坑區","石碇區","坪林區","烏來區"],
    "桃園市": ["桃園區","中壢區","大溪區","楊梅區","蘆竹區","大園區","龜山區","八德區","龍潭區","平鎮區","新屋區","觀音區","復興區"],
    "新竹市": ["東區","北區","香山區"],
    "新竹縣": ["竹北市","竹東鎮","新埔鎮","關西鎮","湖口鄉","新豐鄉","芎林鄉","橫山鄉","北埔鄉","寶山鄉","峨眉鄉","尖石鄉","五峰鄉"],
    "苗栗縣": ["苗栗市","頭份市","竹南鎮","後龍鎮","通霄鎮","苑裡鎮","卓蘭鎮","造橋鄉","西湖鄉","頭屋鄉","公館鄉","銅鑼鄉","三義鄉","大湖鄉","獅潭鄉","三灣鄉","南庄鄉","泰安鄉"],
    "臺中市": ["中區","東區","南區","西區","北區","北屯區","西屯區","南屯區","太平區","大里區","霧峰區","烏日區","豐原區","后里區","石岡區","東勢區","和平區","新社區","潭子區","大雅區","神岡區","大肚區","沙鹿區","龍井區","梧棲區","清水區","大甲區","外埔區","大安區"],
    "彰化縣": ["彰化市","員林市","和美鎮","鹿港鎮","溪湖鎮","二林鎮","田中鎮","北斗鎮","花壇鄉","芬園鄉","大村鄉","永靖鄉","伸港鄉","線西鄉","福興鄉","秀水鄉","埔心鄉","埔鹽鄉","大城鄉","芳苑鄉","竹塘鄉","社頭鄉","二水鄉","田尾鄉","埤頭鄉","溪州鄉"],
    "南投縣": ["南投市","埔里鎮","草屯鎮","竹山鎮","集集鎮","名間鄉","鹿谷鄉","中寮鄉","魚池鄉","國姓鄉","水里鄉","信義鄉","仁愛鄉"],
    "雲林縣": ["斗六市","斗南鎮","虎尾鎮","西螺鎮","土庫鎮","北港鎮","林內鄉","古坑鄉","大埤鄉","莿桐鄉","褒忠鄉","臺西鄉","崙背鄉","麥寮鄉","東勢鄉","四湖鄉","口湖鄉","水林鄉"],
    "嘉義市": ["東區","西區"],
    "嘉義縣": ["太保市","朴子市","布袋鎮","大林鎮","民雄鄉","溪口鄉","新港鄉","六腳鄉","東石鄉","義竹鄉","鹿草鄉","水上鄉","中埔鄉","竹崎鄉","梅山鄉","番路鄉","大埔鄉","阿里山鄉"],
    "臺南市": ["新營區","鹽水區","白河區","柳營區","後壁區","東山區","麻豆區","下營區","六甲區","官田區","大內區","佳里區","學甲區","西港區","七股區","將軍區","北門區","新化區","善化區","新市區","安定區","山上區","玉井區","楠西區","南化區","左鎮區","仁德區","歸仁區","關廟區","龍崎區","永康區","東區","南區","北區","安南區","安平區","中西區"],
    "高雄市": ["鹽埕區","鼓山區","左營區","楠梓區","三民區","新興區","前金區","苓雅區","前鎮區","旗津區","小港區","鳳山區","林園區","大寮區","大樹區","仁武區","鳥松區","岡山區","橋頭區","燕巢區","田寮區","阿蓮區","路竹區","湖內區","茄萣區","永安區","彌陀區","梓官區","旗山區","美濃區","六龜區","甲仙區","杉林區","內門區","茂林區","桃源區","那瑪夏區"],
    "屏東縣": ["屏東市","潮州鎮","東港鎮","恆春鎮","萬丹鄉","長治鄉","麟洛鄉","九如鄉","里港鄉","鹽埔鄉","高樹鄉","萬巒鄉","內埔鄉","竹田鄉","新埤鄉","枋寮鄉","新園鄉","崁頂鄉","林邊鄉","南州鄉","佳冬鄉","琉球鄉","車城鄉","滿州鄉","枋山鄉","三地門鄉","霧台鄉","瑪家鄉","泰武鄉","來義鄉","春日鄉","獅子鄉","牡丹鄉"],
    "臺東縣": ["臺東市","成功鎮","關山鎮","卑南鄉","大武鄉","太麻里鄉","東河鄉","長濱鄉","鹿野鄉","池上鄉","綠島鄉","蘭嶼鄉","延平鄉","海端鄉","金峰鄉","達仁鄉"],
    "花蓮縣": ["花蓮市","鳳林鎮","玉里鎮","新城鄉","吉安鄉","壽豐鄉","光復鄉","豐濱鄉","瑞穗鄉","富里鄉","秀林鄉","萬榮鄉","卓溪鄉"],
    "澎湖縣": ["馬公市","湖西鄉","白沙鄉","西嶼鄉","望安鄉","七美鄉"],
    "金門縣": ["金城鎮","金湖鎮","金沙鎮","金寧鄉","烈嶼鄉","烏坵鄉"],
    "連江縣": ["南竿鄉","北竿鄉","莒光鄉","東引鄉"]
};

document.addEventListener("DOMContentLoaded", function() {
    const citySelect = document.getElementById("citySelect");
    const districtSelect = document.getElementById("districtSelect");
    const streetInput = document.getElementById("streetInput");

    for (const city in addressData) {
        let option = document.createElement("option");
        option.value = city;
        option.textContent = city;
        citySelect.appendChild(option);
    }

    const savedAddress = document.getElementById("addressHidden").value;
    if (savedAddress) {
        for (const city in addressData) {
            if (savedAddress.startsWith(city)) {
                citySelect.value = city;

                districtSelect.innerHTML = '<option value="">請選擇區域</option>';
                addressData[city].forEach(function(district) {
                    let option = document.createElement("option");
                    option.value = district;
                    option.textContent = district;
                    districtSelect.appendChild(option);
                });

                const remaining = savedAddress.substring(city.length);
                for (const district of addressData[city]) {
                    if (remaining.startsWith(district)) {
                        districtSelect.value = district;
                        streetInput.value = remaining.substring(district.length);
                        break;
                    }
                }
                break;
            }
        }
    }

    districtSelect.addEventListener("change", updateAddress);
    streetInput.addEventListener("input", updateAddress);

    /* ---- 開始/結束時間連動 ---- */
    const startHourSelect = document.getElementById("startHour");
    const endHourSelect = document.getElementById("endHour");

    if (startHourSelect && endHourSelect) {
        startHourSelect.addEventListener("change", updateEndHourOptions);
        // 頁面載入時（例如表單驗證失敗回填 savedStartHour）也要先套用一次篩選
        updateEndHourOptions();
    }
});

function updateEndHourOptions() {
    const startHourSelect = document.getElementById("startHour");
    const endHourSelect = document.getElementById("endHour");

    if (!startHourSelect.value) {
        return; // 尚未選擇開始時間，結束時間選項維持原樣
    }

    const startHour = parseInt(startHourSelect.value, 10);
    const options = Array.from(endHourSelect.querySelectorAll("option"));

    let firstValidOption = null;
    let currentIsValid = false;

    options.forEach(function(option) {
        if (option.value === "") {
            return; // 保留「請選擇」之類的預設選項（如果有的話）
        }
        const value = parseInt(option.value, 10);
        const isValid = value > startHour;

        option.hidden = !isValid;
        option.disabled = !isValid;

        if (isValid) {
            if (!firstValidOption) {
                firstValidOption = option;
            }
            if (option.selected) {
                currentIsValid = true;
            }
        }
    });

    // 若目前選到的結束時間已經不合法（<= 開始時間），自動跳到開始時間之後最近的一個時段
    if (!currentIsValid && firstValidOption) {
        endHourSelect.value = firstValidOption.value;
    }
}

function updateDistrict() {
    const citySelect = document.getElementById("citySelect");
    const districtSelect = document.getElementById("districtSelect");
    const city = citySelect.value;

    districtSelect.innerHTML = '<option value="">請選擇區域</option>';

    if (city && addressData[city]) {
        addressData[city].forEach(function(district) {
            let option = document.createElement("option");
            option.value = district;
            option.textContent = district;
            districtSelect.appendChild(option);
        });
    }

    updateAddress();
}

function updateAddress() {
    const city = document.getElementById("citySelect").value;
    const district = document.getElementById("districtSelect").value;
    const street = document.getElementById("streetInput").value;
    document.getElementById("addressHidden").value = city + district + street;
}

// 目前選擇要上傳的照片（用陣列自己管理，才能支援刪除單張）
let selectedFiles = [];
let coverIndex = 0;

function previewImages() {
    const input = document.getElementById('upFiles');
    const newFiles = Array.from(input.files);

    // 累加而不是取代：原生 file input 每次選檔都會整個換成「這次選的檔案」，
    // 所以要自己把新選的檔案併進既有的 selectedFiles，才能一張一張慢慢加
    const wasEmpty = selectedFiles.length === 0;
    selectedFiles = selectedFiles.concat(newFiles);

    if (wasEmpty) {
        coverIndex = 0; // 第一批照片，預設第一張當封面
    }

    renderPreviews();
}

function renderPreviews() {
    const previewArea = document.getElementById('previewArea');
    previewArea.innerHTML = '';

    selectedFiles.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const wrapper = document.createElement('div');
            wrapper.style.cssText = 'position: relative; cursor: pointer;';
            wrapper.onclick = () => setCover(index);

            const img = document.createElement('img');
            img.src = e.target.result;
            img.style.cssText = 'width: 100px; height: 80px; object-fit: cover; border-radius: 6px; border: 2px solid ' + (index === coverIndex ? '#1d9e75' : '#e0e0e0') + ';';

            wrapper.appendChild(img);

            // 只有封面那一張才建立標籤，不是封面就不要放這個 div，
            // 不然空的 div 還是會因為背景色套用而顯示出一個綠色小方塊
            if (index === coverIndex) {
                const label = document.createElement('div');
                label.className = 'cover-label';
                label.textContent = '封面';
                label.style.cssText = 'position: absolute; top: 2px; left: 2px; background: #1d9e75; color: white; font-size: 10px; padding: 2px 6px; border-radius: 4px;';
                wrapper.appendChild(label);
            }

            const removeBtn = document.createElement('div');
            removeBtn.textContent = '✕';
            removeBtn.title = '不上傳這張';
            removeBtn.style.cssText = 'position: absolute; top: 2px; right: 2px; width: 18px; height: 18px; line-height: 18px; text-align: center; background: rgba(0,0,0,0.6); color: #fff; font-size: 12px; border-radius: 50%; cursor: pointer;';
            removeBtn.onclick = function(e) {
                e.stopPropagation(); // 避免同時觸發設封面
                removeFile(index);
            };

            wrapper.appendChild(removeBtn);
            previewArea.appendChild(wrapper);
        };
        reader.readAsDataURL(file);
    });

    document.getElementById('coverIndex').value = coverIndex;
    syncFileInput();
}

function removeFile(index) {
    selectedFiles.splice(index, 1);

    if (selectedFiles.length === 0) {
        coverIndex = 0;
    } else if (index === coverIndex) {
        coverIndex = 0; // 封面被刪掉了，改用剩下的第一張當封面
    } else if (index < coverIndex) {
        coverIndex -= 1; // 前面被移除一張，原本封面的位置要跟著往前遞補
    }

    renderPreviews();
}

function setCover(index) {
    coverIndex = index;
    renderPreviews();
}

// <input type="file"> 的 files 是唯讀的，不能直接刪除其中一個檔案，
// 所以刪除照片後要用 DataTransfer 重新組一份 FileList 塞回真正的 input，
// 表單送出時才會真的排除掉被刪掉的照片。
function syncFileInput() {
    const input = document.getElementById('upFiles');
    const dataTransfer = new DataTransfer();
    selectedFiles.forEach(file => dataTransfer.items.add(file));
    input.files = dataTransfer.files;
}