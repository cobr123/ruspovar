function showMenu(){
    const rawMenu = document.getElementById('raw_menu_text').value;
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

    for(let i = 0; i < arr.length; i++){
        if(arr[i].includes("рсд")){
            var cdId = 'item_' + i;
            var cb = document.createElement("input");
            cb.type = "checkbox";
            cb.onclick = updateTotal;
            cb.id = cdId;

            var label = document.createElement("label");
            label.setAttribute("for", cdId);
            label.innerText = arr[i];
            label.onclick = updateTotal;
            label.id = 'label_' + cdId;

            menu.appendChild(cb);
            menu.appendChild(label);
            menu.appendChild(document.createElement("br"));
        } else {
            var label = document.createElement("label");
            label.innerText = arr[i];

            var cb = document.createElement("input");
            cb.type = "checkbox";
            cb.disabled = true;

            menu.appendChild(cb);
            menu.appendChild(label);
            menu.appendChild(document.createElement("br"));
        }
    }
    if(arr.length > 0){
        var total = document.createElement("label");
        total.id = "total";
        total.innerText = "Итого: 0 рсд";
        total.style = "font-weight: bold;";

        menu.appendChild(document.createElement("br"));
        menu.appendChild(total);
        menu.appendChild(document.createElement("br"));

        var submit = document.createElement("input");
        submit.type = "submit";
        submit.onclick = copyOrderToClipboard;
        submit.value = "Скопировать заказ в буфер обмена";
        menu.appendChild(document.createElement("br"));
        menu.appendChild(submit);
        menu.appendChild(document.createElement("br"));
    }
}

function updateTotal(){
    const order = document.getElementById('order');
    while (order.hasChildNodes()) {
        order.removeChild(order.lastChild);
    }
    var sum = 0;
    var checkboxes = document.querySelectorAll('input[type=checkbox]:checked');

    for (var i = 0; i < checkboxes.length; i++) {
        var cbLabel = document.getElementById('label_' + checkboxes[i].id);

        var label = document.createElement("label");
        label.innerText = cbLabel.innerText;

        order.appendChild(label);
        order.appendChild(document.createElement("br"));

        var priceArr = cbLabel.innerText.match(/\s+(\d+)\s+рсд/i);
        var price = parseInt(priceArr[1]);
        sum += price;
    }
    const total = document.getElementById('total');
    total.innerText = 'Итого: ' + sum + ' рсд';
}

function copyOrderToClipboard(){
    const order = document.getElementById('order');
    var text = order.innerText;
    console.log(text);
    navigator.clipboard.writeText(text).then(function() {
      console.log('Async: Copying to clipboard was successful!');
    }, function(err) {
      console.error('Async: Could not copy text: ', err);
    });
}