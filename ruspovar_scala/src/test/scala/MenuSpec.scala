import org.scalatest.funspec.AnyFunSpec


class MenuSpec extends AnyFunSpec {

  it("parse menu") {
    val expected = Array(
      SubTitleMenuItem("К заказу на понедельник:"),
      EdibleMenuItem("Овощная нарезка120 рсд", None, 0, 120),
      EdibleMenuItem("Капустный с огурцом - 140рсд", None, 0, 140),
      SubTitleMenuItem("Гарнир:"),
      EdibleMenuItem("гречка - 130 рсд", Some("Гарнир:"), 0, 130),
      EdibleMenuItem("пюре - 150рсд", Some("Гарнир:"), 0, 150),
      EdibleMenuItem("Вареники картошка 12 шт - 250  рсд", None, 0, 250),
      SubTitleMenuItem("Десерт:"),
      EdibleMenuItem("сырники - 190 рсд", Some("Десерт:"), 0, 190),
    )
    val text =
      """К заказу на понедельник:
        |
        |1. Овощная нарезка120 рсд
        |
        |2. Капустный с огурцом - 140рсд
        |
        |5. Гарнир:
        |гречка - 130 рсд
        |пюре - 150рсд
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
      EdibleMenuItem("сырники - 190 рсд", Some("Десерт:"), 0, 190),
    )
    val text =
      """
        |10. Десерт:
        |сырники - 190 рсд""".stripMargin

    val items = Menu.parse(text)
    assert(items === expected)
  }

  it("parse order") {
    val expected = Array(
      EdibleMenuItem("Капустный с огурцом - 140 рсд", None, 1, 140),
      SubTitleMenuItem("Гарнир:"),
      EdibleMenuItem("гречка - 130 рсд", Some("Гарнир:"), 1, 130),
      SubTitleMenuItem("Десерт:"),
      EdibleMenuItem("сырники - 190 рсд", Some("Десерт:"), 2, 190),
    )
    val text =
      """Капустный с огурцом - 140 рсд
        |Гарнир:
        |гречка - 130 рсд
        |Десерт:
        |2 x сырники - 190 рсд""".stripMargin

    val items = Menu.parse(text)
    assert(items === expected)
  }
}