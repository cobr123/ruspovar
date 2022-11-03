

object Menu {

  def parse(text: String): Array[MenuItem] = {
    var subTitle = ""
    text.split('\n')
      .map(_.trim)
      .filter(_.nonEmpty)
      .map { line =>
        """\s+(\d+)\s+рсд""".r.findFirstMatchIn(line.toLowerCase) match {
          case Some(value) =>
            val withoutLineNumber = removeLineNumber(line)
            val price = BigDecimal(value.group(1))
            EdibleMenuItem(
              line = withoutLineNumber,
              subTitle = if (line.matches("""^\d+\..*$""")) None else Some(subTitle),
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

  def removeLineNumber(line: String): String = line.replaceAll("""^\d+\.""", "").trim
}