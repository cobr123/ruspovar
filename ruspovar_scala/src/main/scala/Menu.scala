import scala.util.matching.Regex

object Menu {

  def parse(text: String): Array[MenuItem] = {
    val lines = text.split('\n')
      .map(_.trim)
      .filter(_.nonEmpty)

    val isMenu = lines.exists(line => line != removeLineNumber(line))

    if (isMenu) {
      parseMenu(lines)
    } else {
      parseOrder(lines)
    }
  }

  val priceRegExp: Regex = """\s*(\d+)\s*рсд""".r

  private def parseMenu(lines: Array[String]): Array[MenuItem] = {
    var subTitle = ""

    lines.map { line =>
      priceRegExp.findFirstMatchIn(line.toLowerCase) match {
        case Some(value) =>
          val withoutLineNumber = removeLineNumber(line)
          val price = value.group(1).toInt
          val startsWithNumber = withoutLineNumber != line
          EdibleMenuItem(
            line = withoutLineNumber,
            subTitle = if (startsWithNumber) None else Some(subTitle).filter(_.nonEmpty),
            quantity = 0,
            price = price
          )
        case None =>
          val withoutLineNumber = removeLineNumber(line)
          subTitle = withoutLineNumber
          SubTitleMenuItem(withoutLineNumber)
      }
    }
  }

  val lineNumberPattern: String = """^\d+\."""

  private def removeLineNumber(line: String): String = line.replaceAll(lineNumberPattern, "").trim

  private def parseOrder(lines: Array[String]): Array[MenuItem] = {
    var subTitle = ""

    lines.map { line =>
      priceRegExp.findFirstMatchIn(line.toLowerCase) match {
        case Some(value) =>
          val (qty, lineWithoutQty) = removeQuantity(line)
          val price = value.group(1).toInt
          val startsWithQty = lineWithoutQty != line
          EdibleMenuItem(
            line = lineWithoutQty,
            subTitle = Some(subTitle).filter(_.nonEmpty),
            quantity = if (startsWithQty) qty else 1,
            price = price
          )
        case None =>
          val withoutLineNumber = removeLineNumber(line)
          subTitle = withoutLineNumber
          SubTitleMenuItem(withoutLineNumber)
      }
    }
  }

  val qtyRegExp: Regex = """^(\d+)\s+x\s+(.*)$""".r

  private def removeQuantity(line: String): (Int, String) = {
    qtyRegExp.findFirstMatchIn(line) match {
      case Some(value) =>
        val qty = value.group(1).toInt
        val lineWithoutQty = value.group(2)
        (qty, lineWithoutQty)
      case None => (0, line)
    }
  }
}