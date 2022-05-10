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

    for (let i = 0; i < arr.length; i++) {
        if (arr[i].includes("рсд")) {
            const cdId = 'item_' + i;
            const cb = document.createElement("input");
            cb.type = "checkbox";
            cb.onclick = updateTotal;
            cb.id = cdId;

            const label = document.createElement("label");
            label.setAttribute("for", cdId);
            label.innerText = arr[i];
            label.onclick = updateTotal;
            label.id = 'label_' + cdId;

            menu.appendChild(cb);
            menu.appendChild(label);
            menu.appendChild(document.createElement("br"));
        } else {
            const fillerLabel = document.createElement("label");
            fillerLabel.innerText = arr[i];

            const fillerCb = document.createElement("input");
            fillerCb.type = "checkbox";
            fillerCb.disabled = true;

            menu.appendChild(fillerCb);
            menu.appendChild(fillerLabel);
            menu.appendChild(document.createElement("br"));
        }
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