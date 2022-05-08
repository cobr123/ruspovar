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
    if(arr.length > 0){
        var h1 = document.createElement("h1");
        h1.value = arr[0];
        menu.appendChild(h1);
        menu.appendChild(document.createElement("br"));
    }

    for(let i = 1; i < arr.length; i++){
        var cdId = 'item_' + i;
        var cb = document.createElement("input");
        cb.type = "checkbox";
        cb.id = cdId;

        var label = document.createElement("label");
        label.setAttribute("for", cdId);
        label.innerText = arr[i];

        menu.appendChild(cb);
        menu.appendChild(label);
        menu.appendChild(document.createElement("br"));
        menu.appendChild(document.createElement("br"));
    }
    if(arr.length > 0){
        var submit = document.createElement("input");
        submit.type = "submit";
        submit.onclick = copyOrderToClipboard;
        submit.value = "Скопировать заказ в буфер обмена";
        menu.appendChild(document.createElement("br"));
        menu.appendChild(document.createElement("br"));
        menu.appendChild(submit);
    }
}

function copyOrderToClipboard(){
    var text = "Example text to appear on clipboard";
    navigator.clipboard.writeText(text).then(function() {
      console.log('Async: Copying to clipboard was successful!');
    }, function(err) {
      console.error('Async: Could not copy text: ', err);
    });
}