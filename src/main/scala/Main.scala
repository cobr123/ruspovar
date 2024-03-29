import org.scalajs.dom
import org.scalajs.dom.{Element, document, window}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{JSExportTopLevel, JSGlobal}

object Main {
  def main(args: Array[String]): Unit = {
    document.addEventListener("DOMContentLoaded", { (_: dom.Event) =>
      document.getElementById("show_plus_minus")
        .addEventListener("click", { (e: dom.MouseEvent) =>
          if (!togglePlusMinus()) {
            e.preventDefault()
          }
        })
    })
  }

  @JSExportTopLevel("showMenu")
  def showMenu(): Unit = {
    Option(getRawMenuText())
      .map(_.trim)
      .filter(_.nonEmpty)
      .map(showMenu)
      .getOrElse {
        getTextFromClipboard().toFuture.foreach { text =>
          showMenu(text)
        }
      }
  }

  def togglePlusMinus(): Boolean = {
    val moreThenOneQtyExists = menu.exists {
      case e: EdibleMenuItem if e.quantity > 1 => true
      case _ => false
    }
    val showPlusMinus = getShowPlusMinus()
    if (moreThenOneQtyExists && !showPlusMinus && !window.confirm("Сбросить количество до 1?")) {
      return false
    }
    if (menu.nonEmpty) {
      renderMenu(showPlusMinus)
    } else {
      showMenu()
    }
    true
  }

  var menu: Array[MenuItem] = Array.empty

  def showMenu(text: String): Unit = {
    if (text.trim.nonEmpty) {
      val selectedExists = menu.exists {
        case e: EdibleMenuItem if e.quantity > 0 => true
        case _ => false
      }
      if (selectedExists && !window.confirm("Сбросить заказ?")) {
        return
      }
      menu = Menu.parse(text)
      val showPlusMinus = menu.exists {
        case e: EdibleMenuItem if e.quantity > 1 => true
        case _ => false
      }
      renderMenu(showPlusMinus || getShowPlusMinus())
    }
  }

  def getShowPlusMinus(): Boolean = document.querySelectorAll("input[type=checkbox][id='show_plus_minus']:checked").nonEmpty

  def renderMenu(showPlusMinus: Boolean): Unit = {
    val menuNode = document.getElementById("menu")
    while (menuNode.hasChildNodes()) {
      menuNode.removeChild(menuNode.lastChild)
    }
    val orderNode = document.getElementById("order")
    while (orderNode.hasChildNodes()) {
      orderNode.removeChild(orderNode.lastChild)
    }

    menu.foreach { item =>
      renderMenuItem(menuNode, item, showPlusMinus)
    }

    if (menu.nonEmpty) {
      renderTotal(menuNode)
    }
    val qtyExists = menu.exists {
      case e: EdibleMenuItem if e.quantity > 0 => true
      case _ => false
    }
    if (qtyExists) {
      updateTotal()
    }
  }

  def renderMenuItem(menuNode: Element, item: MenuItem, showPlusMinus: Boolean): Unit = {
    val inputStyle = "padding-left: 7px; padding-right: 7px; padding-top: 2px; padding-bottom: 2px; margin: 5px;"

    val qtyLabel = document.createElement("label")
    qtyLabel.setAttribute("style", "background-color: #e2e2e2; " + inputStyle)

    val btnMinus = document.createElement("input")
    btnMinus.setAttribute("type", "button")
    btnMinus.setAttribute("value", "-")
    btnMinus.setAttribute("style", inputStyle)
    btnMinus.addEventListener("click", { (_: dom.MouseEvent) =>
      item match {
        case it: EdibleMenuItem if it.quantity > 0 =>
          it.quantity -= 1
          qtyLabel.innerText = s"${it.quantity}"
          updateTotal()
        case _ =>
      }
    })

    val btnPlus = document.createElement("input")
    btnPlus.setAttribute("type", "button")
    btnPlus.setAttribute("value", "+")
    btnPlus.setAttribute("style", inputStyle)
    btnPlus.addEventListener("click", { (_: dom.MouseEvent) =>
      item match {
        case it: EdibleMenuItem =>
          it.quantity += 1
          qtyLabel.innerText = s"${it.quantity}"
          updateTotal()
        case _ =>
      }
    })

    val cb = document.createElement("input")
    cb.setAttribute("type", "checkbox")
    cb.id = s"cb_${menuNode.childNodes.length}"
    cb.addEventListener("change", { (_: dom.MouseEvent) =>
      item match {
        case it: EdibleMenuItem =>
          if (it.quantity > 0) {
            it.quantity = 0
          } else {
            it.quantity = 1
          }
          updateTotal()
        case _ =>
      }
    })

    val label = document.createElement("label")
    label.innerText = item.line

    item match {
      case it: EdibleMenuItem =>
        qtyLabel.innerText = s"${it.quantity}"
        if (!showPlusMinus && it.quantity > 0) {
          cb.setAttribute("checked", "true")
          it.quantity = 1
        }
      case _: SubTitleMenuItem =>
        qtyLabel.innerHTML = "&nbsp;&nbsp;"
        btnMinus.setAttribute("disabled", "true")
        btnPlus.setAttribute("disabled", "true")
        cb.setAttribute("disabled", "true")
    }

    if (showPlusMinus) {
      menuNode.appendChild(btnMinus)
      menuNode.appendChild(qtyLabel)
      menuNode.appendChild(btnPlus)
    } else {
      menuNode.appendChild(cb)
      label.setAttribute("for", cb.id)
    }
    menuNode.appendChild(label)
    menuNode.appendChild(document.createElement("br"))
  }

  def renderTotal(menuNode: Element): Unit = {
    val total = document.createElement("label")
    total.id = "total"
    total.innerText = "Итого: 0 рсд"
    total.setAttribute("style", "font-weight: bold;")

    menuNode.appendChild(document.createElement("br"))
    menuNode.appendChild(total)
    menuNode.appendChild(document.createElement("br"))

    val submit = document.createElement("input")
    submit.setAttribute("type", "submit")
    submit.id = "copy_order"
    submit.addEventListener("click", { (e: dom.MouseEvent) =>
      copyOrderToClipboard()
    })
    submit.setAttribute("value", "Скопировать заказ в буфер обмена")
    menuNode.appendChild(document.createElement("br"))
    menuNode.appendChild(submit)
    menuNode.appendChild(document.createElement("br"))
  }

  def updateTotal(): Unit = {
    val orderNode = document.getElementById("order")
    while (orderNode.hasChildNodes()) {
      orderNode.removeChild(orderNode.lastChild)
    }
    var sum = 0

    menu.filter {
      case e: EdibleMenuItem if e.quantity > 0 => true
      case _ => false
    }.foreach { item =>
      val label = document.createElement("label")
      item match {
        case EdibleMenuItem(line, subTitleOpt, quantity, price) =>
          subTitleOpt.foreach { subTitle =>
            val subTitleLabel = document.createElement("label")
            subTitleLabel.innerText = subTitle
            orderNode.appendChild(subTitleLabel)
            orderNode.appendChild(document.createElement("br"))
          }

          if (quantity > 1) {
            label.innerHTML = s"<b>$quantity x</b> $line"
          } else {
            label.innerText = line
          }
          sum += price * quantity
        case _ =>
      }

      orderNode.appendChild(label)
      orderNode.appendChild(document.createElement("br"))
    }
    val total = document.getElementById("total")
    total.innerText = s"Итого: $sum рсд"
  }

  @js.native
  @JSGlobal("copyOrderToClipboard")
  def copyOrderToClipboard(): Unit = js.native

  @js.native
  @JSGlobal("getRawMenuText")
  def getRawMenuText(): String = js.native

  @js.native
  @JSGlobal("getTextFromClipboard")
  def getTextFromClipboard(): Promise[String] = js.native
}