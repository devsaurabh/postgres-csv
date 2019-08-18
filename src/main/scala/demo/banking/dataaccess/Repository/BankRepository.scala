package demo.banking.dataaccess.Repository

import cats.implicits._
import demo.banking.dataaccess.model.BankInformation
import demo.banking.dataaccess.provider.DBProvider
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.sqlstate
import doobie.util.update.Update

import scala.concurrent.Future

class BankRepositoryImpl(provider: DBProvider) extends BankRepository {
  def bulkInsertBankInfo(bankInfos: List[BankInformation]): Int = {
    val data: List[(Int,String)] = bankInfos.map(t => (t.identifier, t.name))
    val sql = "insert into bank (identifier, name) values (?, ?)"
    val res = Update[(Int,String)](sql).updateMany(data)
    provider.mapToResult(res).unsafeRunSync()
  }

  def getBankInfo(id: Int): Future[Option[BankInformation]] = {
    val c = sql"select identifier, name from bank where identifier = $id"
      val res = c.query[BankInformation].option
      provider.mapToResult(res).unsafeToFuture()
  }

  def safeInsertBankInfo(bankInfo: BankInformation): Future[Either[String, BankInformation]] = {
    val res: ConnectionIO[BankInformation] =
      sql"insert into bank (identifier, name) values (${bankInfo.identifier}, ${bankInfo.name})"
        .update
        .withUniqueGeneratedKeys("identifier", "name")

    provider.mapToResult(res.attemptSomeSqlState {
      case sqlstate.class23.UNIQUE_VIOLATION => "Oops! Duplicate record"
    }).unsafeToFuture()
  }
}

trait BankRepository {
  def getBankInfo(id: Int): Future[Option[BankInformation]]
  def safeInsertBankInfo(bankInfo: BankInformation): Future[Either[String, BankInformation]]
}
