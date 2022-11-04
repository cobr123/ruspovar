import org.scalajs.dom
import org.scalajs.dom.{Element, document, window}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{JSExportTopLevel, JSGlobal}

object Main {
  def main(args: Array[String]): Unit = {}

  @JSExportTopLevel("showMenu")
  def showMenu(): Unit = {
    Option(document.getElementById("raw_menu_text").textContent)
      .map(_.trim)
      .filter(_.nonEmpty)
      .map(showMenu)
      .getOrElse {
        getTextFromClipboard().toFuture.foreach { text =>
          showMenu(text)
        }
      }
  }

  @JSExportTopLevel("showPlusMinus")
  def showPlusMinus(): Unit = {
    val moreThenOneQtyExists = menu.exists {
      case e: EdibleMenuItem if e.quantity > 1 => true
      case _ => false
    }
    if (moreThenOneQtyExists && !getShowPlusMinus() && !window.confirm("Сбросить количество до 1?")) {
      return
    }
    renderMenu()
    updateTotal()
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
      renderMenu()
    }
  }

  def getShowPlusMinus(): Boolean = document.querySelectorAll("input[type=checkbox][id='show_plus_minus']:checked").nonEmpty

  def renderMenu(): Unit = {
    val menuNode = document.getElementById("menu")
    while (menuNode.hasChildNodes()) {
      menuNode.removeChild(menuNode.lastChild)
    }
    val orderNode = document.getElementById("order")
    while (orderNode.hasChildNodes()) {
      orderNode.removeChild(orderNode.lastChild)
    }

    val showPlusMinus = getShowPlusMinus()
    menu.foreach { item =>
      renderMenuItem(menuNode, item, showPlusMinus)
    }

    if (menu.nonEmpty) {
      renderTotal(menuNode)
    }
  }

  def renderMenuItem(menuNode: Element, item: MenuItem, showPlusMinus: Boolean): Unit = {
    val inputQty = document.createElement("input")
    inputQty.setAttribute("size", "2")
    inputQty.setAttribute("disabled", "true")

    val btnMenus = document.createElement("input")
    btnMenus.setAttribute("type", "button")
    btnMenus.setAttribute("value", "-")
    btnMenus.addEventListener("click", { (e: dom.MouseEvent) =>
      item match {
        case it: EdibleMenuItem if it.quantity > 0 =>
          it.quantity -= 1
          inputQty.setAttribute("value", s"${it.quantity}")
          updateTotal()
        case _ =>
      }
    })

    val btnPlus = document.createElement("input")
    btnPlus.setAttribute("type", "button")
    btnPlus.setAttribute("value", "+")
    btnPlus.addEventListener("click", { (e: dom.MouseEvent) =>
      item match {
        case it: EdibleMenuItem =>
          it.quantity += 1
          inputQty.setAttribute("value", s"${it.quantity}")
          updateTotal()
        case _ =>
      }
    })

    val cb = document.createElement("input")
    cb.setAttribute("type", "checkbox")
    cb.id = s"cb_${menuNode.childNodes.length}"
    cb.addEventListener("change", { (e: dom.MouseEvent) =>
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
        inputQty.setAttribute("value", s"${it.quantity}")
        if (!showPlusMinus && it.quantity > 0) {
          cb.setAttribute("checked", "true")
          it.quantity = 1
        }
      case _: SubTitleMenuItem =>
        inputQty.setAttribute("value", "")
        btnMenus.setAttribute("disabled", "true")
        btnPlus.setAttribute("disabled", "true")
        cb.setAttribute("disabled", "true")
    }

    if (showPlusMinus) {
      menuNode.appendChild(btnMenus)
      menuNode.appendChild(inputQty)
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
    var sum = BigDecimal.valueOf(0)

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
            label.innerText = s"$quantity x $line"
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
  @JSGlobal("getTextFromClipboard")
  def getTextFromClipboard(): Promise[String] = js.native
}