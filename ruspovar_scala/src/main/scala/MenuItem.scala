

sealed abstract class MenuItem(val line: String)

final case class EdibleMenuItem(override val line: String, subTitle: Option[String], var quantity: Int, price: Int) extends MenuItem(line)

final case class SubTitleMenuItem(override val line: String) extends MenuItem(line)
