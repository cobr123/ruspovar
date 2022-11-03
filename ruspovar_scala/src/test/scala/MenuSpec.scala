import org.scalatest.funspec.AnyFunSpec


class MenuSpec extends AnyFunSpec {

  it("parse menu") {
    val expected = Array(
      SubTitleMenuItem("К заказу на понедельник:"),
      EdibleMenuItem("Овощная нарезка - 120 рсд", None, 0, BigDecimal.valueOf(120)),
      EdibleMenuItem("Капустный с огурцом - 140 рсд", None, 0, BigDecimal.valueOf(140)),
      SubTitleMenuItem("Гарнир:"),
      EdibleMenuItem("гречка - 130 рсд", Some("Гарнир:"), 0, BigDecimal.valueOf(130)),
      EdibleMenuItem("пюре - 130 рсд", Some("Гарнир:"), 0, BigDecimal.valueOf(130)),
      EdibleMenuItem("Вареники картошка 12 шт - 250  рсд", None, 0, BigDecimal.valueOf(250)),
      SubTitleMenuItem("Десерт:"),
      EdibleMenuItem("сырники - 190 рсд", Some("Десерт:"), 0, BigDecimal.valueOf(190)),
    )
    val text =
      """К заказу на понедельник:
        |
        |1. Овощная нарезка - 120 рсд
        |
        |2. Капустный с огурцом - 140 рсд
        |
        |5. Гарнир:
        |гречка - 130 рсд
        |пюре - 130 рсд
        |
        |9. Вареники картошка 12 шт - 250  рсд
        |
        |10. Десерт:
        |сырники - 190 рсд""".stripMargin

    val items = Menu.parse(text)
    assert(items === expected)
  }

  it("parse subTitle") {
    val expected = Array(
      SubTitleMenuItem("Десерт:"),
      EdibleMenuItem("сырники - 190 рсд", Some("Десерт:"), 0, BigDecimal.valueOf(190)),
    )
    val text =
      """
        |10. Десерт:
        |сырники - 190 рсд""".stripMargin

    val items = Menu.parse(text)
    assert(items === expected)
  }

}