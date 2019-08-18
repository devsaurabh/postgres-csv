package demo.banking.dataaccess.model

case class BankInformation(identifier: Int, name: String) {
  require(identifier > 0, "Bank identifier is required")
  require(!name.isBlank, "Bank name is required")
}
