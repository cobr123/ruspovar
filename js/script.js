async function showMenu() {
    let rawMenu = document.getElementById('raw_menu_text').value;
    if (rawMenu === null || rawMenu.trim() === '') {
        rawMenu = await getTextFromClipboard();
    }
    if (rawMenu === null || rawMenu.trim() === '') {
        return;
    }
    const checkboxes = document.querySelectorAll('input[type=checkbox]:checked');
    if (checkboxes.length > 0 && !confirm("Сбросить заказ?")) {
        return;
    }
    const menu = document.getElementById('menu');

    const arr = rawMenu.split('\n')
        .map(s => s.trim())
        .filter(s => s.length > 0);
    console.log(arr);

    while (menu.hasChildNodes()) {
        menu.removeChild(menu.lastChild);
    }
    const order = document.getElementById('order');
    while (order.hasChildNodes()) {
        order.removeChild(order.lastChild);
    }

    let idx = 0;
    for (let i = 0; i < arr.length; i++) {
        const text = arr[i];
        const varianceIdx = findVariance(text);
        if (varianceIdx >= 0) {
            addMenuItem(idx, text.replace(variances[varianceIdx][0], variances[varianceIdx][1]), menu);
            idx += 1;
            addMenuItem(idx, text.replace(variances[varianceIdx][0], variances[varianceIdx][2]), menu);
        } else {
            addMenuItem(idx, text, menu);
        }
        idx += 1;
    }
    if (arr.length > 0) {
        const total = document.createElement("label");
        total.id = "total";
        total.innerText = "Итого: 0 рсд";
        total.style = "font-weight: bold;";

        menu.appendChild(document.createElement("br"));
        menu.appendChild(total);
        menu.appendChild(document.createElement("br"));

        const submit = document.createElement("input");
        submit.type = "submit";
        submit.id = "copy_order";
        submit.onclick = copyOrderToClipboard;
        submit.value = "Скопировать заказ в буфер обмена";
        menu.appendChild(document.createElement("br"));
        menu.appendChild(submit);
        menu.appendChild(document.createElement("br"));
    }
}

const variances = [
    ["(можно без мяса)", "(без мяса)", "(с мясом)"],
    [" св/гов ", " св ", " гов "],
    [" кур/тел ", " кур ", " тел "],
    [" карт/грибы ", " карт ", " грибы "],
    [" гов/карт ", " гов ", " карт "],
    ["(суп для тех кто не ест мясо, курицу добавлю по желанию в порцию для тех, кто ест мясо)", "(без мяса)", "(с мясом)"]
];

function findVariance(text) {
    for (let i = 0; i < variances.length; i++) {
        if (text.indexOf(variances[i][0]) >= 0) {
            return i;
        }
    }
    return -1;
}

function addMenuItem(idx, text, menu) {
    const cdId = 'item_' + idx;
    const cb = document.createElement("input");
    cb.type = "checkbox";
    cb.id = cdId;

    const label = document.createElement("label");
    label.innerText = text;
    label.id = 'label_' + cdId;

    if (text.includes("рсд")) {
        cb.onclick = updateTotal;

        label.setAttribute("for", cdId);
        label.onclick = updateTotal;
    } else {
        cb.disabled = true;
    }
    menu.appendChild(cb);
    menu.appendChild(label);
    menu.appendChild(document.createElement("br"));
}

async function getTextFromClipboard() {
    let text = '';
    try {
        const permission = await navigator.permissions.query({name: 'clipboard-read'});
        if (permission.state === 'denied') {
            throw new Error('Not allowed to read clipboard.');
        }
        const clipboardContents = await navigator.clipboard.read();
        for (const item of clipboardContents) {
            if (!item.types.includes('text/plain')) {
                throw new Error('Clipboard contains non-text data: ' + item.types);
            }
            const blob = await item.getType('text/plain');
            text = await blob.text();
        }
    } catch (error) {
        console.error(error.message);
    }
    return text;
}

function updateTotal() {
    const order = document.getElementById('order');
    while (order.hasChildNodes()) {
        order.removeChild(order.lastChild);
    }
    let sum = 0;
    const checkboxes = document.querySelectorAll('input[type=checkbox]:checked');

    for (let i = 0; i < checkboxes.length; i++) {
        const cbLabel = document.getElementById('label_' + checkboxes[i].id);

        if (!cbLabel.innerText.match(/^\d+\..*$/)) {
            const idx = parseInt(checkboxes[i].id.match(/item_(\d+)/)[1]);
            for (let k = idx - 1; k >= 0; k--) {
                const disabledCbLabel = document.getElementById('label_item_' + k);
                if (disabledCbLabel.innerText.match(/^\d+\.[^:]*:$/)) {
                    const disabledLabel = document.createElement("label");
                    disabledLabel.innerText = disabledCbLabel.innerText;

                    order.appendChild(disabledLabel);
                    order.appendChild(document.createElement("br"));
                    break;
                }
            }
        }

        const label = document.createElement("label");
        label.innerText = cbLabel.innerText;

        order.appendChild(label);
        order.appendChild(document.createElement("br"));

        const priceArr = cbLabel.innerText.match(/\s+(\d+)\s+рсд/i);
        const price = parseInt(priceArr[1]);
        sum += price;
    }
    const total = document.getElementById('total');
    total.innerText = 'Итого: ' + sum + ' рсд';
}

function copyOrderToClipboard() {
    const order = document.getElementById('order');
    const text = order.innerText;
    console.log(text);
    navigator.clipboard.writeText(text).then(function () {
        console.log('Async: Copying to clipboard was successful!');
        notifyCopy('Заказ скопирован');
    }, function (err) {
        console.error('Async: Could not copy text: ', err);
        notifyCopy(err);
    });
}

function notifyCopy(msg) {
    const btn = document.getElementById('copy_order');
    const tmp = btn.value;
    btn.disabled = true;
    btn.value = msg;
    setTimeout(() => {
        btn.value = tmp;
        btn.disabled = false;
    }, 1000);
}