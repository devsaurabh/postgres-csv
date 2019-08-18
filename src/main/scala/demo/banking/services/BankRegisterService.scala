package demo.banking.services

import java.nio.file.Path

import demo.banking.dataaccess.model.BankInformation

import scala.concurrent.Future

trait BankRegisterService {
  /**
    * Registers/Saves all the bank information present in the csv file
    * @param csvFile Path of the file to import
    * @return Count of all the inserted records
    */
  def registerBanks(csvFile: Path): Future[Int]

  /**
    * Gets bank information for the given identifier
    * @param bankIdentifier bank identifier
    * @return Future option on bank information
    */
  def getBankInfo(bankIdentifier: Int): Future[Option[BankInformation]]
}
