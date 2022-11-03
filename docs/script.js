
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